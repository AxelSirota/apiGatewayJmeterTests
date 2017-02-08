/*
 * API Gateway
 * Copyright 2010-2016 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p/>
 * This software is protected under international copyright law. All use of this software is
 * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
 * separately entered into in writing between you and MuleSoft. If such an agreement is not
 * in place, you may not use the software.
 */

package com.mulesoft.performance.test;

import com.mulesoft.anypoint.client.*;
import com.mulesoft.anypoint.client.entity.ApiData;
import com.mulesoft.anypoint.client.exception.FunctionalException;
import com.mulesoft.performance.setup.environment.Characteristic;
import com.mulesoft.performance.setup.environment.Platform;
import com.mulesoft.performance.setup.environment.Runtime;
import com.mulesoft.performance.setup.environment.RuntimeLocation;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterSuite;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;


public abstract class MasterResource {
    protected static final Logger LOG = Logger.getLogger("Message");

    public static final String KEY_PLATFORM = "env.portal";
    public static final String KEY_RUNTIME_LOCATION = "env.gateway";
    public static final String KEY_RUNTIME = "env.cloudhub"; // TODO(nahuel): Refactor the key values to reflect actual runtime names.


    // Used to obfuscate passwords and sensitive information.
    private static final String ENCRYPTED_LDAP_PASS = "LdAzpG2Eky8r5q1aWU2w4A==";
    private static final char[] PASS_ENCRYPT_PBKEY = "enfldsgbnlsngdlksdsgm".toCharArray();
    private static final byte[] PASS_ENCRYPT_SALT = {
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
    };

    protected static final long TIMEOUT_IN_MILLIS = 120000; // Standard timeout.

    protected static Client client;
    protected static PlatformManager platformManager;
    protected static PolicyManager policyManager;
    protected static CustomPolicyManager customPolicyManager;
    protected static PlatformEnvironment platformEnvironment;
    private static RuntimeLocation runtimeLocation;
    protected static Runtime runtime;
    protected static String ldapPass = "";

    protected static String organizationId;

    private static final String AUTOMATION_REPORT_PRODUCT_AGW_ID = "7";
    private static final String KEY_AUTOMATION_REPORT_ENABLED = "qa_dashboard_publish";
    private static final String KEY_AUTOMATION_REPORT_PRODUCT = "results.api.productId";
    private static final String KEY_AUTOMATION_REPORT_SUB_PRODUCT = "results.api.subProduct";
    private static final String AUTOMATION_REPORT_SUB_PRODUCT = "agw";
    private static final String KEY_AUTOMATION_REPORT_ENV = "results.api.env";


    private static final String AUTOMATION_REPORT_ENV_DEV = "DEV";
    private static final String AUTOMATION_REPORT_ENV_QA = "QA";
    private static final String AUTOMATION_REPORT_ENV_STG = "STG";
    private static final String AUTOMATION_REPORT_ENV_PROD = "PROD";

    protected static final String API_VERSION_REGULAR = "Regular";

    public static final int HTTP_CODE_WARNINGS_START = 300;
    public static final int HTTP_CODE_ERRORS_START = 400;

    private static boolean isCluster = false;
    protected static boolean isMasterOrStandaloneNode = true; // This is used to solve a but on Throttling algorithm that misses the count by one when using clustering slave node that it won't be fixed.

    protected static int INITIAL_COUNT_AFTER_WAIT;

    protected static final String CANONICAL_REDIRECT_URL = "http://0.0.0.0:8081/gateway/oauth/redirect";
    private static final List<String> CANONICAL_REDIRECT_URLS = new ArrayList<>();
    protected static List<String> REDIRECT_URI;

    public static final String TLS = "TLSv1.2"; // Used by calls performed within the tests against a HTTPs endpoint.

    static {
        runtimeLocation = getRuntimeLocation();

        initAutomationDashboard();
        initLdapPass();

        final Platform platform = Platform.fromString(System.getProperty(KEY_PLATFORM));

        final List<String> canonicalEndpointUrls = new ArrayList<>();
        canonicalEndpointUrls.add(CANONICAL_REDIRECT_URL);

        runtime = getRuntime();
        client = getPlatformClient(platform, runtime);
        platformManager = client.getPlatformManager();
        policyManager = client.getPolicyManager();
        platformEnvironment = client.getPlatformEnvironment();
        organizationId = getOrganizationId();
        customPolicyManager = client.getCustomPolicyManager(organizationId);

        initRuntimeAndDeployMasterTestApplication(platform, runtimeLocation, canonicalEndpointUrls);

        // Adding multiple redirect URIs, used when creating client applications. Only the one that is not "fake" will be used in the oAuth dance.
        CANONICAL_REDIRECT_URLS.add(CANONICAL_REDIRECT_URL);
        CANONICAL_REDIRECT_URLS.add(CANONICAL_REDIRECT_URL + "/fake1");
        CANONICAL_REDIRECT_URLS.add(CANONICAL_REDIRECT_URL + "/fake2");

        REDIRECT_URI = CANONICAL_REDIRECT_URLS;

        // Hack to solve weird issues when running Rate Limit based tests on Cluster.
        if (RuntimeLocation.REMOTE_QA_LAB_NODE_SLAVE.equals(runtimeLocation) || (runtimeLocation.equals(RuntimeLocation.REMOTE_CLUSTER_SECONDARY_USING_ARM))) {
            isMasterOrStandaloneNode = true;
        }
        INITIAL_COUNT_AFTER_WAIT = isMasterOrStandaloneNode ? 0 : 1; // TODO(nahuel): This should be enabled only for versions < v2.2.0.
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownTestSuite() {
    }

    protected static Client getPlatformClient(Platform platform, Runtime runtime) {
        return Client.getInstance(
                platform.getApiPlatformBaseUrl(),
                platform.getCoreServicesBaseUrl(),
                platform.getAnalyticsBaseUrl(),
                platform.getCloudhubBaseUrl(),
                platform.getContractServiceUrl(),
                platform.getOrganizationOwner(),
                runtime.getPolicySupportedVersion());
    }

    private static void initRuntimeAndDeployMasterTestApplication(Platform platform, RuntimeLocation runtimeLocation, List<String> canonicalEndpointUrls) {
        switch (runtimeLocation) {
            case LOCAL:
                isCluster = false;
                break;
            case REMOTE_QA_LAB_NODE_MASTER:
            case REMOTE_QA_LAB_NODE_SLAVE:
                isCluster = true;
                break;
            default:
                throw new FunctionalException("Unrecognized Gateway Environment: " + runtimeLocation);
        }
    }

    private static void initLdapPass() {
        try {
            ldapPass = decrypt(ENCRYPTED_LDAP_PASS);
        } catch (GeneralSecurityException | IOException e) {
            throw new FunctionalException("Could not decrypt password for LdapTest", e);
        }
    }

//    /**
//     * Overrides any command line environment setup. Use this as a convenience method to run the suite on an IDE.
//     */
//    private static void parametricOrganizationSetup(String platformUrl, String coreServicesUrl, String contractCacheServiceUrl, String username, String password, String orgCharacteristics) {
//        System.setProperty(KEY_ORGANIZATION_OWNER_USERNAME, username);
//        System.setProperty(KEY_ORGANIZATION_OWNER_PASSWORD, password);
//        System.setProperty(KEY_BASE_URL_PLATFORM, platformUrl);
//        System.setProperty(KEY_BASE_URL_CORE_SERVICES, coreServicesUrl);
//        System.setProperty(KEY_BASE_URL_CONTRACT_CACHE_SERVICES, contractCacheServiceUrl);
//        System.setProperty(KEY_ORGANIZATION_CHARACTERISTICS, orgCharacteristics);
//    }
//
//    /**
//     * Overrides any command line environment setup. Use this as a convenience method to run the suite on an IDE.
//     */
//    private static void ideSetup(Platform platform, RuntimeLocation runtimeLocation, Runtime runtime) {
//        System.setProperty(KEY_PLATFORM, platform.getEnvironment());
//        System.setProperty(KEY_RUNTIME_LOCATION, runtimeLocation.getEnvironment());
//        System.setProperty(KEY_RUNTIME, runtime.getEnvironment());
//        System.setProperty(KEY_AUTOMATION_REPORT_ENV, AUTOMATION_REPORT_ENV_DEV);
//    }

    public static String decrypt(String property) throws GeneralSecurityException, IOException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASS_ENCRYPT_PBKEY));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(PASS_ENCRYPT_SALT, 20));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }

    private static byte[] base64Decode(String property) throws IOException {
        return new BASE64Decoder().decodeBuffer(property);
    }

    private static String getOrganizationId() {
        // TODO(nahuel): When API is not created yet, the following call fails.
        // Custom Policies are visible in the suborg where they are created.
        String id;
        try {
            final ApiData apiData = ApiData.getApiCredentials(platformEnvironment, "zTest Custom Policy", API_VERSION_REGULAR);
            id = apiData.getOrganizationId();
        } catch (Exception e) {
            LOG.warn("Unable to get API Credentials", e);
            final List<String> organizationIds = client.getPlatformManager().getAllOrganizationsBelongingToUser();
            // Choose arbitrarily first result.
            id = organizationIds.get(0);
        }
        return id;
    }

    /**
     * Make sure that there are no mismatches between Portal and Gateway, otherwise CH and Remote tests will fail.
     */

    private static void initAutomationDashboard() {
        System.setProperty(KEY_AUTOMATION_REPORT_PRODUCT, AUTOMATION_REPORT_PRODUCT_AGW_ID);
        System.setProperty(KEY_AUTOMATION_REPORT_SUB_PRODUCT, AUTOMATION_REPORT_SUB_PRODUCT);
        final Platform platform = Platform.fromString(System.getProperty(KEY_PLATFORM));
        final String qaDashboardPublish = System.getProperty(KEY_AUTOMATION_REPORT_ENABLED);
        if ((qaDashboardPublish != null) && (qaDashboardPublish.equalsIgnoreCase("true"))) {
            if (System.getProperty(KEY_AUTOMATION_REPORT_ENV) == null) {
                if (platform.getCharacteristics().contains(Characteristic.QA)) {
                    System.setProperty(KEY_AUTOMATION_REPORT_ENV, AUTOMATION_REPORT_ENV_QA);
                } else if (platform.getCharacteristics().contains(Characteristic.STG)) {
                    System.setProperty(KEY_AUTOMATION_REPORT_ENV, AUTOMATION_REPORT_ENV_STG);
                } else {
                    System.setProperty(KEY_AUTOMATION_REPORT_ENV, AUTOMATION_REPORT_ENV_PROD);
                }
            }
        } else {
            System.setProperty(KEY_AUTOMATION_REPORT_ENV, AUTOMATION_REPORT_ENV_DEV); // Publish all results in DEV disregarding any specified environment.
        }
    }

    protected static RuntimeLocation getRuntimeLocation() {
        return RuntimeLocation.fromString(System.getProperty(KEY_RUNTIME_LOCATION));
    }

    public static Runtime getRuntime() {
        return Runtime.fromString(System.getProperty(KEY_RUNTIME)); //"qa2_gateway_382");
    }
}
