/*
 * API Gateway
 * Copyright 2010-2015 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p/>
 * This software is protected under international copyright law. All use of this software is
 * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
 * separately entered into in writing between you and MuleSoft. If such an agreement is not
 * in place, you may not use the software.
 */

package com.mulesoft.performance.test.policy.clientidenforcement;

import com.mulesoft.anypoint.client.entity.ApiData;
import com.mulesoft.anypoint.client.entity.ApplicationData;
import com.mulesoft.anypoint.client.entity.GrantType;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.mulesoft.anypoint.client.util.FunctionalUtil.logResponseAndGetContent;
import static com.mulesoft.anypoint.client.util.MiscUtil.verifyCall;
import static com.mulesoft.performance.misc.FunctionalUtil.*;


@Test(singleThreaded=true, groups = { "smoke_qa" , "smoke_stg", "smoke_prod" })
public class ClientIdEnforcementOnRegularTest extends ClientIdEnforcementBase {

    private String endpoint;
    private ApiData apiData;

    private static final String APP_NAME_PREFIX = "zTest ClientID AppR ";

    private GrantType[] grantTypes = {GrantType.AUTHORIZATION_CODE, GrantType.IMPLICIT, GrantType.PASSWORD};

    // TODO(nahuel): For non federated orgs, consider using no grant types also.

    @BeforeClass
    public void setup() {
        endpoint = "http://23.21.206.49:8887/api";//"http://0.0.0.0:8081/gateway/clientidenforcement/path1");
        apiData = ApiData.getApiCredentials(platformEnvironment, "PabloTestAPI", "1.0");//, API_NAME, API_VERSION_REGULAR);
        platformManager.resetApi(apiData);
        platformManager.removeAllApplicationsWithPrefix(APP_NAME_PREFIX);

        waitUntilEndpointCondition(endpoint, HttpStatus.SC_OK);
    }

    @AfterMethod
    public void tearDownTest() {
        policyManager.removeAllPoliciesFromApiVersion(apiData);
        waitUntilEndpointCondition(endpoint, HttpStatus.SC_OK);
    }

    /**
     * AP-102 AP-88 AP-87 AP-86 AP-85 AP-84
     */
    @Test(description= "Check Client ID enforcement with client ID and client secret provided as query params, on HTTP endpoint")
    public void clientIdEnforcementPolicyOnRegularEndpointUsingGet() {
        final String clientIdExpression = "#[message.inboundProperties['http.query.params']['client_id']]";
        final String clientSecretExpression = "#[message.inboundProperties['http.query.params']['client_secret']]";

        logResponseAndGetContent(verifyCall(policyManager.applyClientIdEnforcementPolicy(apiData, clientIdExpression, clientSecretExpression), HttpStatus.SC_CREATED, "Create Policy"));

        final ApplicationData appData = platformManager.createApplicationClientWithPrefixAndRegister(APP_NAME_PREFIX, REDIRECT_URI, grantTypes, apiData);
        final ApplicationData appDataNotRegistered = platformManager.createApplicationClientWithPrefix(apiData.getOrganizationId(), APP_NAME_PREFIX, REDIRECT_URI, grantTypes);

        waitUntilEndpointCondition(endpoint, HttpStatus.SC_FORBIDDEN);
        waitUntilSuccessByQuery(endpoint, appData);

        checkForbiddenByQuery(endpoint, ApplicationData.getInstance(appData.getClientId(), null));
        checkForbiddenByQuery(endpoint, ApplicationData.getInstance(null, appData.getClientSecretId()));
        checkForbiddenByQuery(endpoint, ApplicationData.getInstance(appData.getClientId(), "bla1233211321321"));
        checkForbiddenByQuery(endpoint, ApplicationData.getInstance("bla1233211321321", appData.getClientSecretId()));
        checkForbiddenByQuery(endpoint, ApplicationData.getInstance("bla1233211321321", "bla1233211321321"));
        checkForbiddenByQuery(endpoint, ApplicationData.getInstanceEmpty());

        checkForbiddenByQuery(endpoint, appDataNotRegistered);
        checkForbiddenByQuery(endpoint, ApplicationData.getInstance(appDataNotRegistered.getClientId(), null));
        checkForbiddenByQuery(endpoint, ApplicationData.getInstance(null, appDataNotRegistered.getClientSecretId()));
        checkForbiddenByQuery(endpoint, ApplicationData.getInstance(appDataNotRegistered.getClientId(), "bla1233211321321"));
        checkForbiddenByQuery(endpoint, ApplicationData.getInstance("bla1233211321321", appDataNotRegistered.getClientSecretId()));

        checkSuccessByQuery(endpoint, appData);

        platformManager.revokeApplicationContract(apiData, appData.getContractId());
        waitUntilFailByQuery(endpoint, appData);
        checkForbiddenByQuery(endpoint, appData);
        checkForbiddenByQuery(endpoint, appDataNotRegistered);

        // TODO(nahuel): Create another API and check that although the same app is registered there, is is not affected by the policy applied on the other API.

        // Create not registered app.
        final ApplicationData appAnotherClient = platformManager.createApplicationClientWithPrefix(apiData.getOrganizationId(), APP_NAME_PREFIX, REDIRECT_URI, grantTypes);

        checkForbiddenByQuery(endpoint, appAnotherClient);

        verifyCall(platformManager.restoreApplicationContract(apiData, appData.getContractId()), HttpStatus.SC_CREATED, "Restore application contract");
        waitUntilSuccessByQuery(endpoint, appData);

        checkSuccessByQuery(endpoint, appData);
        checkForbiddenByQuery(endpoint, appDataNotRegistered);
        checkForbiddenByQuery(endpoint, appAnotherClient);

        logResponseAndGetContent(verifyCall(platformManager.registerApplication(apiData.getOrganizationId(), apiData.getApiVersionId(), appAnotherClient.getId()), HttpStatus.SC_CREATED, "Register Application"));

        waitUntilSuccessByQuery(endpoint, appAnotherClient);

        checkSuccessByQuery(endpoint, appAnotherClient);

        verifyCall(platformManager.revokeApplicationContract(apiData, appData.getContractId()), HttpStatus.SC_CREATED, "Revoke application contract");
        waitUntilFailByQuery(endpoint, appData);

        checkSuccessByQuery(endpoint, appAnotherClient);
        checkForbiddenByQuery(endpoint, appData);
        checkForbiddenByQuery(endpoint, appDataNotRegistered);
        checkForbiddenByQuery(endpoint, appAnotherClient);

        verifyCall(platformManager.removeApplicationContract(apiData, appData.getContractId()), HttpStatus.SC_NO_CONTENT, "Remove application contract");
        checkSuccessByQuery(endpoint, appAnotherClient);
        checkForbiddenByQuery(endpoint, appData);
        checkForbiddenByQuery(endpoint, appDataNotRegistered);
        checkForbiddenByQuery(endpoint, appAnotherClient);
    }

    @Test(description= "Check Client ID enforcement with client ID and client secret provided as TCP headers, on HTTP endpoint")
    public void clientIdEnforcementPolicyOnRegularEndpointUsingGetAndHeaderExpression() {
        final String clientIdExpression = "#[message.inboundProperties['client_id']]";
        final String clientSecretExpression = "#[message.inboundProperties['client_secret']]";

        logResponseAndGetContent(verifyCall(policyManager.applyClientIdEnforcementPolicy(apiData, clientIdExpression, clientSecretExpression), HttpStatus.SC_CREATED, "Create Policy"));

        final ApplicationData appData = platformManager.createApplicationClientWithPrefixAndRegister(APP_NAME_PREFIX, REDIRECT_URI, grantTypes, apiData);
        final ApplicationData appDataNotRegistered = platformManager.createApplicationClientWithPrefix(apiData.getOrganizationId(), APP_NAME_PREFIX, REDIRECT_URI, grantTypes);

        waitUntilEndpointCondition(endpoint, HttpStatus.SC_FORBIDDEN);
        waitUntilSuccessByHeader(endpoint, appData);

        checkForbiddenByHeader(endpoint, ApplicationData.getInstance(appData.getClientId(), null));
        checkForbiddenByHeader(endpoint, ApplicationData.getInstance(null, appData.getClientSecretId()));
        checkForbiddenByHeader(endpoint, ApplicationData.getInstance(appData.getClientId(), "bla1233211321321"));
        checkForbiddenByHeader(endpoint, ApplicationData.getInstance("bla1233211321321", appData.getClientSecretId()));
        checkForbiddenByHeader(endpoint, ApplicationData.getInstance("bla1233211321321", "bla1233211321321"));
        checkForbiddenByHeader(endpoint, ApplicationData.getInstanceEmpty());

        checkForbiddenByHeader(endpoint, appDataNotRegistered);
        checkForbiddenByHeader(endpoint, ApplicationData.getInstance(appDataNotRegistered.getClientId(), null));
        checkForbiddenByHeader(endpoint, ApplicationData.getInstance(null, appDataNotRegistered.getClientSecretId()));
        checkForbiddenByHeader(endpoint, ApplicationData.getInstance(appDataNotRegistered.getClientId(), "bla1233211321321"));
        checkForbiddenByHeader(endpoint, ApplicationData.getInstance("bla1233211321321", appDataNotRegistered.getClientSecretId()));

        checkSuccessByHeader(endpoint, appData);

        platformManager.revokeApplicationContract(apiData, appData.getContractId());
        waitUntilFailByHeader(endpoint, appData);
        checkForbiddenByHeader(endpoint, appData);
        checkForbiddenByHeader(endpoint, appDataNotRegistered);

        // TODO(nahuel): Create another API and check that although the same app is registered there, is is not affected by the policy applied on the other API.

        // Create not registered app.
        final ApplicationData appAnotherClient = platformManager.createApplicationClientWithPrefix(apiData.getOrganizationId(), APP_NAME_PREFIX, REDIRECT_URI, grantTypes);

        checkForbiddenByHeader(endpoint, appAnotherClient);

        verifyCall(platformManager.restoreApplicationContract(apiData, appData.getContractId()), HttpStatus.SC_CREATED, "Restore application contract");
        waitUntilSuccessByHeader(endpoint, appData);

        checkSuccessByHeader(endpoint, appData);
        checkForbiddenByHeader(endpoint, appDataNotRegistered);
        checkForbiddenByHeader(endpoint, appAnotherClient);

        logResponseAndGetContent(verifyCall(platformManager.registerApplication(apiData.getOrganizationId(), apiData.getApiVersionId(), appAnotherClient.getId()), HttpStatus.SC_CREATED, "Register Application"));

        waitUntilSuccessByHeader(endpoint, appAnotherClient);

        checkSuccessByHeader(endpoint, appAnotherClient);

        verifyCall(platformManager.revokeApplicationContract(apiData, appData.getContractId()), HttpStatus.SC_CREATED, "Revoke application contract");
        waitUntilFailByHeader(endpoint, appData);

        checkSuccessByHeader(endpoint, appAnotherClient);
        checkForbiddenByHeader(endpoint, appData);
        checkForbiddenByHeader(endpoint, appDataNotRegistered);
        checkForbiddenByHeader(endpoint, appAnotherClient);

        verifyCall(platformManager.removeApplicationContract(apiData, appData.getContractId()), HttpStatus.SC_NO_CONTENT, "Remove application contract");
        checkSuccessByHeader(endpoint, appAnotherClient);
        checkForbiddenByHeader(endpoint, appData);
        checkForbiddenByHeader(endpoint, appDataNotRegistered);
        checkForbiddenByHeader(endpoint, appAnotherClient);
    }

    @Test(description= "Check Client ID enforcement with client ID provided as TCP header and no client secret, on HTTP endpoint")
    public void clientIdEnforcementPolicyOnRegularEndpointUsingGetAndHeaderExpressionWithoutSecret() {
        final String clientIdExpression = "#[message.inboundProperties['client_id']]";
        final String clientSecretExpression = null;

        logResponseAndGetContent(verifyCall(policyManager.applyClientIdEnforcementPolicy(apiData, clientIdExpression, clientSecretExpression), HttpStatus.SC_CREATED, "Create Policy"));

        final ApplicationData appData = platformManager.createApplicationClientWithPrefixAndRegister(APP_NAME_PREFIX, REDIRECT_URI, grantTypes, apiData);
        waitFor(1000, "Wait to avoid 502 status codes from the platform");
        final ApplicationData appDataNotRegistered = platformManager.createApplicationClientWithPrefix(apiData.getOrganizationId(), APP_NAME_PREFIX, REDIRECT_URI, grantTypes);

        waitUntilEndpointCondition(endpoint, HttpStatus.SC_FORBIDDEN);
        waitUntilSuccessByHeader(endpoint, appData);

        checkSuccessByHeader(endpoint, ApplicationData.getInstance(appData.getClientId(), ""));
        checkSuccessByHeader(endpoint, ApplicationData.getInstance(appData.getClientId(), null));

        checkForbiddenByHeader(endpoint, ApplicationData.getInstance(null, appData.getClientSecretId()));

        checkSuccessByHeader(endpoint, ApplicationData.getInstance(appData.getClientId(), "bla1233211321321"));
        checkForbiddenByHeader(endpoint, ApplicationData.getInstance("bla1233211321321", appData.getClientSecretId()));
        checkForbiddenByHeader(endpoint, ApplicationData.getInstance("bla1233211321321", "bla1233211321321"));
        checkForbiddenByHeader(endpoint, ApplicationData.getInstanceEmpty());

        checkForbiddenByHeader(endpoint, appDataNotRegistered);

        checkForbiddenByHeader(endpoint, ApplicationData.getInstance(appDataNotRegistered.getClientId(), null));
        checkForbiddenByHeader(endpoint, ApplicationData.getInstance(null, appDataNotRegistered.getClientSecretId()));

        checkForbiddenByHeader(endpoint, ApplicationData.getInstance(appDataNotRegistered.getClientId(), "bla1233211321321"));

        checkForbiddenByHeader(endpoint, ApplicationData.getInstance("bla1233211321321", appDataNotRegistered.getClientSecretId()));

        checkSuccessByHeader(endpoint, appData);

        platformManager.revokeApplicationContract(apiData, appData.getContractId());
        waitUntilFailByHeader(endpoint, appData);
        checkForbiddenByHeader(endpoint, appData);
        checkForbiddenByHeader(endpoint, appDataNotRegistered);

        // TODO(nahuel): Create another API and check that although the same app is registered there, is is not affected by the policy applied on the other API.

        // Create not registered app.
        final ApplicationData appAnotherClient = platformManager.createApplicationClientWithPrefix(apiData.getOrganizationId(), APP_NAME_PREFIX, REDIRECT_URI, grantTypes);

        checkForbiddenByHeader(endpoint, appAnotherClient);

        verifyCall(platformManager.restoreApplicationContract(apiData, appData.getContractId()), HttpStatus.SC_CREATED, "Restore application contract");
        waitUntilSuccessByHeader(endpoint, appData);

        checkSuccessByHeader(endpoint, appData);
        checkForbiddenByHeader(endpoint, appDataNotRegistered);
        checkForbiddenByHeader(endpoint, appAnotherClient);

        logResponseAndGetContent(verifyCall(platformManager.registerApplication(apiData.getOrganizationId(), apiData.getApiVersionId(), appAnotherClient.getId()), HttpStatus.SC_CREATED, "Register Application"));

        waitUntilSuccessByHeader(endpoint, appAnotherClient);

        checkSuccessByHeader(endpoint, appAnotherClient);

        verifyCall(platformManager.revokeApplicationContract(apiData, appData.getContractId()), HttpStatus.SC_CREATED, "Revoke application contract");
        waitUntilFailByHeader(endpoint, appData);

        checkSuccessByHeader(endpoint, appAnotherClient);
        checkForbiddenByHeader(endpoint, appData);
        checkForbiddenByHeader(endpoint, appDataNotRegistered);
        checkSuccessByHeader(endpoint, appAnotherClient);

        verifyCall(platformManager.removeApplicationContract(apiData, appData.getContractId()), HttpStatus.SC_NO_CONTENT, "Remove application contract");
        checkSuccessByHeader(endpoint, appAnotherClient);
        checkForbiddenByHeader(endpoint, appData);
        checkForbiddenByHeader(endpoint, appDataNotRegistered);
        checkSuccessByHeader(endpoint, appAnotherClient);
    }

    @Test(description= "Check Client ID enforcement policy passing client ID and Secret as Basic Auth credentials")
    public void clientIdEnforcementAsBasicAuth() {
        logResponseAndGetContent(verifyCall(policyManager.applyClientIdEnforcementPolicyAsBasicAuth(apiData), HttpStatus.SC_CREATED, "Create Policy"));

        final ApplicationData appData = platformManager.createApplicationClientWithPrefixAndRegister(APP_NAME_PREFIX, REDIRECT_URI, grantTypes, apiData);
        final ApplicationData appDataNotRegistered = platformManager.createApplicationClientWithPrefix(apiData.getOrganizationId(), APP_NAME_PREFIX, REDIRECT_URI, grantTypes);

        waitUntilEndpointRestricted(endpoint);
        waitUntilSuccessByBasicAuth(endpoint, appData);

        checkFailByBasicAuth(endpoint, ApplicationData.getInstance(appData.getClientId(), "bla1233211321321"));
        checkFailByBasicAuth(endpoint, ApplicationData.getInstance("bla1233211321321", appData.getClientSecretId()));
        checkFailByBasicAuth(endpoint, ApplicationData.getInstance("bla1233211321321", "bla1233211321321"));

        checkFailByBasicAuth(endpoint, appDataNotRegistered);
        checkFailByBasicAuth(endpoint, ApplicationData.getInstance(appDataNotRegistered.getClientId(), "bla1233211321321"));
        checkFailByBasicAuth(endpoint, ApplicationData.getInstance("bla1233211321321", appDataNotRegistered.getClientSecretId()));

        checkSuccessByBasicAuth(endpoint, appData);

        platformManager.revokeApplicationContract(apiData, appData.getContractId());
        waitUntilFailByBasicAuth(endpoint, appData);
        checkFailByBasicAuth(endpoint, appData);
        checkFailByBasicAuth(endpoint, appDataNotRegistered);

        // TODO(nahuel): Create another API and check that although the same app is registered there, is is not affected by the policy applied on the other API.

        // Create not registered app.
        final ApplicationData appAnotherClient = platformManager.createApplicationClientWithPrefix(apiData.getOrganizationId(), APP_NAME_PREFIX, REDIRECT_URI, grantTypes);

        checkFailByBasicAuth(endpoint, appAnotherClient);

        verifyCall(platformManager.restoreApplicationContract(apiData, appData.getContractId()), HttpStatus.SC_CREATED, "Restore application contract");
        waitUntilSuccessByBasicAuth(endpoint, appData);

        checkSuccessByBasicAuth(endpoint, appData);
        checkFailByBasicAuth(endpoint, appDataNotRegistered);
        checkFailByBasicAuth(endpoint, appAnotherClient);

        logResponseAndGetContent(verifyCall(platformManager.registerApplication(apiData.getOrganizationId(), apiData.getApiVersionId(), appAnotherClient.getId()), HttpStatus.SC_CREATED, "Register Application"));

        waitUntilSuccessByBasicAuth(endpoint, appAnotherClient);

        checkSuccessByBasicAuth(endpoint, appAnotherClient);

        verifyCall(platformManager.revokeApplicationContract(apiData, appData.getContractId()), HttpStatus.SC_CREATED, "Revoke application contract");
        waitUntilFailByBasicAuth(endpoint, appData);

        checkSuccessByBasicAuth(endpoint, appAnotherClient);
        checkFailByBasicAuth(endpoint, appData);
        checkFailByBasicAuth(endpoint, appDataNotRegistered);
        checkSuccessByBasicAuth(endpoint, appAnotherClient);

        verifyCall(platformManager.removeApplicationContract(apiData, appData.getContractId()), HttpStatus.SC_NO_CONTENT, "Remove application contract");
        checkSuccessByBasicAuth(endpoint, appAnotherClient);
        checkFailByBasicAuth(endpoint, appData);
        checkFailByBasicAuth(endpoint, appDataNotRegistered);
        checkSuccessByBasicAuth(endpoint, appAnotherClient);
    }

    @Test(description= "Check Client ID enforcement policy passing client ID and Secret as Basic Auth credentials to more than 100 client apps")
    public void clientIdEnforcementWithLotsOfContracts() {
        final String clientIdExpression = "#[message.inboundProperties['client_id']]";
        final String clientSecretExpression = "#[message.inboundProperties['client_secret']]";

        logResponseAndGetContent(verifyCall(policyManager.applyClientIdEnforcementPolicy(apiData, clientIdExpression, clientSecretExpression), HttpStatus.SC_CREATED, "Create Policy"));

        final List<ApplicationData> applications = new ArrayList<>();
        // Create *more than* 100 contracts.
        final int appsToCreateAndRegister = 120;
        LOG.info(String.format("About to create and register %d client applications..", appsToCreateAndRegister));
        for (int i = 1; i <= appsToCreateAndRegister; i++) {
            applications.add(platformManager.createApplicationClientWithPrefixAndRegister(APP_NAME_PREFIX, REDIRECT_URI, grantTypes, apiData));
            LOG.info(String.format("Client application created and registered to the API. Amount of registrations #: %d .", i));
            waitFor(600, "Contract creation time padding"); // To avoid 502 errors.
        }
        LOG.info("Created contracts list: " + getAllContracts(applications));

        waitUntilEndpointCondition(endpoint, HttpStatus.SC_FORBIDDEN);
        waitUntilSuccessByHeader(endpoint, applications.get(applications.size() - 1));

        for (ApplicationData application : applications) {
            LOG.info("About to check contract #" + applications.indexOf(application) + ". Application: " + application);
            checkForbiddenByHeader(endpoint, ApplicationData.getInstance(application.getClientId(), "bla1233211321321"));
            checkSuccessByHeader(endpoint, application);
        }
    }

    private String getAllContracts(List<ApplicationData> applications) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (ApplicationData application : applications) {
            stringBuilder.append(application);
            stringBuilder.append(NEW_LINE);
        }
        return stringBuilder.toString();
    }
}
