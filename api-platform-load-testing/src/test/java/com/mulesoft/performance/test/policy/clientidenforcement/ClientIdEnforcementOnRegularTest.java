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
import com.mulesoft.performance.jmeter.JMeterTestPlan;
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

    private static final String APP_NAME_PREFIX = "pTest ClientID AppPerf ";

    private GrantType[] grantTypes = {GrantType.AUTHORIZATION_CODE, GrantType.IMPLICIT, GrantType.PASSWORD};

    // TODO(nahuel): For non federated orgs, consider using no grant types also.

    @BeforeClass
    public void setup() {
//        endpoint = "http://23.21.206.49:8887/api";//"http://0.0.0.0:8081/gateway/clientidenforcement/path1");
        endpoint = "http://54.83.54.127:8887/api";//"http://0.0.0.0:8081/gateway/clientidenforcement/path1");
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

        waitUntilEndpointCondition(endpoint, HttpStatus.SC_FORBIDDEN);
        waitUntilSuccessByQuery(endpoint, appData);
        checkSuccessByQuery(endpoint, appData);
        final JMeterTestPlan jmeter = new JMeterTestPlan(1, 1, 10, 8887, "54.83.54.127", "GET");
        try {
            jmeter.runJmeter(appData.getClientId(), appData.getClientSecretId());
        } catch (Exception e) {
            System.err.println("JmeterException: " + e.getMessage());
        }

        platformManager.revokeApplicationContract(apiData, appData.getContractId());
        waitUntilFailByQuery(endpoint, appData);
        checkForbiddenByQuery(endpoint, appData);
    }

    @Test(description= "Check Client ID enforcement policy passing client ID and Secret as Basic Auth credentials")
    public void clientIdEnforcementAsBasicAuth() {
        logResponseAndGetContent(verifyCall(policyManager.applyClientIdEnforcementPolicyAsBasicAuth(apiData), HttpStatus.SC_CREATED, "Create Policy"));

        final ApplicationData appData = platformManager.createApplicationClientWithPrefixAndRegister(APP_NAME_PREFIX, REDIRECT_URI, grantTypes, apiData);
        waitUntilEndpointRestricted(endpoint);
        waitUntilSuccessByBasicAuth(endpoint, appData);
        checkSuccessByBasicAuth(endpoint, appData);
        final JMeterTestPlan jmeter = new JMeterTestPlan(1, 1, 10, 8887, "54.83.54.127", "GET");
        try {
            jmeter.runJmeter(appData.getClientId(), appData.getClientSecretId());
        } catch (Exception e) {
            System.err.println("JmeterException: " + e.getMessage());
        }
        platformManager.revokeApplicationContract(apiData, appData.getContractId());
        waitUntilFailByBasicAuth(endpoint, appData);
        checkFailByBasicAuth(endpoint, appData);
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

        LOG.info("About to check contract #" + applications.indexOf(applications.get(applications.size() - 1)) + ". Application: " + applications.get(applications.size() - 1));
        final JMeterTestPlan jmeter = new JMeterTestPlan(1, 1, 10, 8887, "54.83.54.127", "GET");
        try {
            jmeter.runJmeter(applications.get(applications.size() - 1).getClientId(), applications.get(applications.size() - 1).getClientSecretId());
        } catch (Exception e) {
            System.err.println("JmeterException: " + e.getMessage());
        }

        for (ApplicationData application : applications) {
            LOG.info("About to revoke contract #" + applications.indexOf(application) + ". Application: " + application);
            platformManager.revokeApplicationContract(apiData, application.getContractId());
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
