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
import java.util.HashMap;
import java.util.List;

import static com.mulesoft.anypoint.client.util.FunctionalUtil.logResponseAndGetContent;
import static com.mulesoft.anypoint.client.util.MiscUtil.verifyCall;
import static com.mulesoft.performance.misc.FunctionalUtil.*;


@Test(singleThreaded=true)
public class ClientIdEnforcementOnRegularTest extends ClientIdEnforcementBase {

    private String endpoint;
    private ApiData apiData;

    private static final String APP_NAME_PREFIX = "pTest ClientID AppPerf ";

    private GrantType[] grantTypes = {GrantType.AUTHORIZATION_CODE, GrantType.IMPLICIT, GrantType.PASSWORD};

    @BeforeClass
    public void setup() {
//        endpoint = "http://23.21.206.49:8887/api";
        endpoint = "http://54.83.54.127:8887/api";
        apiData = ApiData.getApiCredentials(platformEnvironment, "PabloTestAPI", "1.0");
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
        runJmeterusingQuery(appData.getClientId(), appData.getClientSecretId(), "default");
        platformManager.revokeApplicationContract(apiData, appData.getContractId());
        waitUntilFailByQuery(endpoint, appData);
        checkForbiddenByQuery(endpoint, appData);
    }

    @Test(description= "Check Client ID enforcement policy passing client ID and Secret as Basic Auth credentials to more than 100 client apps")
    public void clientIdEnforcementWithLotsOfContracts() {
        final String clientIdExpression = "#[message.inboundProperties['client_id']]";
        final String clientSecretExpression = "#[message.inboundProperties['client_secret']]";

        logResponseAndGetContent(verifyCall(policyManager.applyClientIdEnforcementPolicy(apiData, clientIdExpression, clientSecretExpression), HttpStatus.SC_CREATED, "Create Policy"));

        final List<ApplicationData> applications = new ArrayList<>();
        // Create *more than* 100 contracts.
        final int appsToCreateAndRegister = 2;
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
        runJmeterusingHeaders(applications.get(applications.size() - 1).getClientId(), applications.get(applications.size() - 1).getClientSecretId(), "contracts");
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

    private void runJmeterusingQuery(String clientId, String clientSecretId, String test) {
        final JMeterTestPlan jmeterEngine = new JMeterTestPlan("/usr/local/Cellar/jmeter/3.1/libexec");
        jmeterEngine.setHTTPParameters(8887, "54.83.54.127", "GET", "/api?client_id=" + clientId + "&client_secret=" + clientSecretId);
        jmeterEngine.setThreadGroupParameters(10, 1, 25);
        jmeterEngine.setHeader("Content-Type", "text/plain");
        jmeterEngine.setLoggingParameters("log/" + test + ".results.jtl", "log/" + test + ".testPlan.jmx");
        try {
            HashMap<String, Double> results = jmeterEngine.runJmeter();
            checkSLA(results);
        } catch (Exception e) {
            System.err.println("JmeterException: " + e.getMessage());
        }
    }

    private void runJmeterusingHeaders(String clientId, String clientSecret, String test) {
        final JMeterTestPlan jmeterEngine = new JMeterTestPlan("/usr/lib/apache-jmeter-3.1");
        jmeterEngine.setHTTPParameters(8887, "54.83.54.127", "GET", "/api");
        jmeterEngine.setThreadGroupParameters(10, 1, 25);
        jmeterEngine.setHeader("Content-Type", "text/plain");
        jmeterEngine.setHeader("client_id", clientId);
        jmeterEngine.setHeader("client_secret", clientSecret);
        jmeterEngine.setLoggingParameters("log/" + test + ".results.jtl", "log/" + test + ".testPlan.jmx");
        try {
            HashMap<String, Double> results = jmeterEngine.runJmeter();
            checkSLA(results);
        } catch (Exception e) {
            System.err.println("JmeterException: " + e.getMessage());
        }
    }


    private void checkSLA(HashMap<String, Double> results) {
        HashMap<String, Double> slas = new HashMap<String, Double>();
        slas.put("TotalTime" , ((double) 180));
        slas.put("ExpectedThreads" , ((double) 10));
        slas.put("StartedThreads" , ((double) 10));
        slas.put("StoppedThreads" , ((double) 10));
        slas.put("Requests" , ((double) 1200));
        slas.put("Throughput" , ((double) 50));
        slas.put("BytesPerSecond" , ((double) 1));
        slas.put("BytesPerRequestAvg " , ((double) 1));
        slas.put("Error% " , 0.1);
        slas.put("AvgLatency " , ((double) 50));
        slas.put("MinLatency " , ((double) 1));
        slas.put("50thPercentile " , ((double) 2));
        slas.put("90thPercentile " , ((double) 3));
        slas.put("95thPercentile " , ((double) 4));
        slas.put("99thPercentile " , ((double) 5));
        slas.put("MaxLatency " , ((double) 6));

    for (String key : slas.keySet()) {
        System.out.println("INFO: " + key);
        if (slas.get(key) > results.get(key)) {
            LOG.error("SLAs for " + key + " is not met, got " + results.get(key) + " while the SLA was " + slas.get(key));
        }
    }
    }

}
