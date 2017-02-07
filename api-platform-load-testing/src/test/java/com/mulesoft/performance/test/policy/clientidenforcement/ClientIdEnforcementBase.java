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

import com.jayway.restassured.specification.RequestSpecification;
import com.mulesoft.anypoint.client.entity.ApplicationData;
import com.mulesoft.anypoint.client.util.MuleResponse;
import com.mulesoft.performance.test.MasterResource;
import org.apache.http.HttpStatus;
import static com.jayway.restassured.RestAssured.given;
import static com.mulesoft.anypoint.client.util.FunctionalUtil.logResponseAndGetContent;
import static com.mulesoft.anypoint.client.util.MiscUtil.verifyCall;
import static com.mulesoft.performance.misc.FunctionalUtil.waitForOneCycle;
import static org.testng.Assert.fail;

public class ClientIdEnforcementBase extends MasterResource {

    public static void waitUntilSuccessByQuery(String endpoint, ApplicationData appData) {
        final long timeout = System.currentTimeMillis() + TIMEOUT_IN_MILLIS;
        boolean isSuccess = false;
        while (System.currentTimeMillis() < timeout) {
            final MuleResponse responseNegative = MuleResponse.getInstance(given().log().all()
                    .get(endpoint));
            final MuleResponse response = MuleResponse.getInstance(given().log().all()
                    .queryParam("client_id", appData.getClientId())
                    .queryParam("client_secret", appData.getClientSecretId())
                    .get(endpoint));
            final int statusCode = response.getStatusCode();
            logResponseAndGetContent(response);
            if ((responseNegative.getStatusCode() >= HTTP_CODE_WARNINGS_START) && (statusCode == HttpStatus.SC_OK)) {
                LOG.info(endpoint + " endpoint meets expected condition.");
                isSuccess = true;
                break;
            }
            waitForOneCycle();
            waitForOneCycle();
            LOG.warn(String.format("%s endpoint does not meet expected condition (expected HTTP code: 200 , but found: %d). Re attempting!..", endpoint, statusCode));
        }
        if (!isSuccess) {
            fail(String.format("%s endpoint does not meet expected condition (expected HTTP code: 200).", endpoint));
        }
    }

    static void waitUntilSuccessByHeader(String endpoint, ApplicationData appData) {
        final long timeout = System.currentTimeMillis() + TIMEOUT_IN_MILLIS;
        boolean isSuccess = false;
        while (System.currentTimeMillis() < timeout) {
            final MuleResponse responseNegative = MuleResponse.getInstance(given().log().all()
                    .get(endpoint));
            final MuleResponse response = MuleResponse.getInstance(given().log().all()
                    .header("client_id", appData.getClientId())
                    .header("client_secret", appData.getClientSecretId())
                    .get(endpoint));
            final int statusCode = response.getStatusCode();
            logResponseAndGetContent(response);
            if ((responseNegative.getStatusCode() >= 300) && (statusCode == HttpStatus.SC_OK)) {
                LOG.info(endpoint + " endpoint meets expected condition.");
                isSuccess = true;
                break;
            }
            waitForOneCycle();
            waitForOneCycle();
            LOG.warn(String.format("%s endpoint does not meet expected condition (expected HTTP code: 200 , but found: %d). Re attempting!..", endpoint, statusCode));
        }
        if (!isSuccess) {
            fail(String.format("%s endpoint does not meet expected condition (expected HTTP code: 200).", endpoint));
        }
    }

    static void waitUntilSuccessByBasicAuth(String endpoint, ApplicationData appData) {
        final long timeout = System.currentTimeMillis() + TIMEOUT_IN_MILLIS;
        boolean isSuccess = false;
        while (System.currentTimeMillis() < timeout) {
            final MuleResponse responseNegative = MuleResponse.getInstance(given().log().all()
                    .get(endpoint));
            final MuleResponse response = MuleResponse.getInstance(given().log().all()
                    .relaxedHTTPSValidation(TLS)
                    .authentication()
                    .preemptive().basic(appData.getClientId(), appData.getClientSecretId())
                    .get(endpoint));
            final int statusCode = response.getStatusCode();
            logResponseAndGetContent(response);
            if ((responseNegative.getStatusCode() >= 300) && (statusCode == HttpStatus.SC_OK)) {
                LOG.info(endpoint + " endpoint meets expected condition.");
                isSuccess = true;
                break;
            }
            waitForOneCycle();
            waitForOneCycle();
            LOG.warn(String.format("%s endpoint does not meet expected condition (expected HTTP code: 200 , but found: %d). Re attempting!..", endpoint, statusCode));
        }
        if (!isSuccess) {
            fail(String.format("%s endpoint does not meet expected condition (expected HTTP code: 200).", endpoint));
        }
    }

    static void waitUntilFailByQuery(String endpoint, ApplicationData appData) {
        final long timeout = System.currentTimeMillis() + TIMEOUT_IN_MILLIS;
        boolean isSuccess = false;
        MuleResponse response = null;
        while (System.currentTimeMillis() < timeout) {
             response = MuleResponse.getInstance(given().log().all()
                    .queryParam("client_id", appData.getClientId())
                    .queryParam("client_secret", appData.getClientSecretId())
                    .get(endpoint));
            final int statusCode = response.getStatusCode();
            logResponseAndGetContent(response);
            if (statusCode != HttpStatus.SC_OK) {
                LOG.info(endpoint + " endpoint meets expected condition.");
                isSuccess = true;
                break;
            }
            waitForOneCycle();
            LOG.warn(String.format("%s endpoint is not restricted (expected HTTP code different from: %d , but found: 200). Re attempting!..", endpoint, statusCode));
        }
        if (!isSuccess) {
            fail(String.format("%s endpoint does not meet expected condition (expected HTTP code != 200). Last response: %s", endpoint, response));
        }
    }

    static void waitUntilFailByHeader(String endpoint, ApplicationData appData) {
        final long timeout = System.currentTimeMillis() + TIMEOUT_IN_MILLIS;
        boolean isSuccess = false;
        MuleResponse response = null;
        while (System.currentTimeMillis() < timeout) {
             response = MuleResponse.getInstance(given().log().all()
                    .header("client_id", appData.getClientId())
                    .header("client_secret", appData.getClientSecretId())
                    .get(endpoint));
            final int statusCode = response.getStatusCode();
            logResponseAndGetContent(response);
            if (statusCode != HttpStatus.SC_OK) {
                LOG.info(endpoint + " endpoint meets expected condition.");
                isSuccess = true;
                break;
            }
            waitForOneCycle();
            LOG.warn(String.format("%s endpoint is not restricted (expected HTTP code different from: %d , but found: 200). Re attempting!..", endpoint, statusCode));
        }
        if (!isSuccess) {
            fail(String.format("%s endpoint does not meet expected condition (expected HTTP code != 200). Last response: %s", endpoint, response));
        }
    }

    static void waitUntilFailByBasicAuth(String endpoint, ApplicationData appData) {
        final long timeout = System.currentTimeMillis() + TIMEOUT_IN_MILLIS;
        boolean isSuccess = false;
        MuleResponse response = null;
        while (System.currentTimeMillis() < timeout) {
            response = MuleResponse.getInstance(given().log().all()
                    .authentication()
                    .preemptive().basic(appData.getClientId(), appData.getClientSecretId())
                    .get(endpoint));
            final int statusCode = response.getStatusCode();
            logResponseAndGetContent(response);
            if (statusCode != HttpStatus.SC_OK) {
                LOG.info(endpoint + " endpoint meets expected condition.");
                isSuccess = true;
                break;
            }
            waitForOneCycle();
            LOG.warn(String.format("%s endpoint is not restricted (expected HTTP code different from: %d , but found: 200). Re attempting!..", endpoint, statusCode));
        }
        if (!isSuccess) {
            fail(String.format("%s endpoint does not meet expected condition (expected HTTP code != 200). Last response: %s", endpoint, response));
        }
    }

    public static void checkForbiddenByQuery(String endpoint, ApplicationData appData) {
        final RequestSpecification requestSpecification = given().log().all();
        if (appData.getClientId() != null) {
            requestSpecification.queryParam("client_id", appData.getClientId());
        }
        if (appData.getClientSecretId() != null) {
            requestSpecification.queryParam("clientSecret", appData.getClientSecretId());
        }
        verifyCall(MuleResponse.getInstance(requestSpecification.get(endpoint)), HttpStatus.SC_FORBIDDEN, "Client Id enforcement::Un-Happy path");
    }

    public static void checkForbiddenByHeader(String endpoint, ApplicationData appData) {
        final RequestSpecification requestSpecification = given().log().all();
        if (appData.getClientId() != null) {
            requestSpecification.header("client_id", appData.getClientId());
        }
        if (appData.getClientSecretId() != null) {
            requestSpecification.header("clientSecret", appData.getClientSecretId());
        }
        verifyCall(MuleResponse.getInstance(requestSpecification.get(endpoint)), HttpStatus.SC_FORBIDDEN, "Client Id enforcement::Un-Happy path");
    }

    static void checkFailByBasicAuth(String endpoint, ApplicationData appData) {
        final RequestSpecification requestSpecification = given().log().all()
                .relaxedHTTPSValidation(TLS);
        verifyCall(MuleResponse.getInstance(requestSpecification
                .authentication()
                .preemptive().basic(appData.getClientId(), appData.getClientSecretId())
                .get(endpoint)), HttpStatus.SC_UNAUTHORIZED, "Client Id enforcement::Un-Happy path");
    }

    public static void checkSuccessByQuery(String endpoint, ApplicationData appData) {
        final MuleResponse response = MuleResponse.getInstance(given().log().all()
                .queryParam("client_id", appData.getClientId())
                .queryParam("client_secret", appData.getClientSecretId())
                .get(endpoint));
        verifyCall(response, HttpStatus.SC_OK, "Client Id enforcement::Happy path");
    }

    static void checkSuccessByHeader(String endpoint, ApplicationData appData) {
        final RequestSpecification requestSpecification = given().log().all()
                .header("client_id", appData.getClientId());
        if (appData.getClientSecretId() != null) {
            requestSpecification.header("client_secret", appData.getClientSecretId());
        }

        final MuleResponse response = MuleResponse.getInstance(requestSpecification.get(endpoint));
        verifyCall(response, HttpStatus.SC_OK, "Client Id enforcement::Happy path");
    }

    static void checkSuccessByBasicAuth(String endpoint, ApplicationData appData) {
        final RequestSpecification requestSpecification = given().log().all()
                .relaxedHTTPSValidation(TLS);
        final MuleResponse response = MuleResponse.getInstance(requestSpecification
                .authentication()
                .preemptive().basic(appData.getClientId(), appData.getClientSecretId())
                .get(endpoint));
        verifyCall(response, HttpStatus.SC_OK, "Client Id enforcement::Happy path");
    }
}
