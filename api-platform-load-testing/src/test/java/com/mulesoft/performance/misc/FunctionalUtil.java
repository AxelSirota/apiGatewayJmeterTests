/*
 * API Gateway
 * Copyright 2010-2015 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p/>
 * This software is protected under international copyright law. All use of this software is
 * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
 * separately entered into in writing between you and MuleSoft. If such an agreement is not
 * in place, you may not use the software.
 */

package com.mulesoft.performance.misc;

import com.mulesoft.anypoint.client.exception.FunctionalException;
import com.mulesoft.anypoint.client.util.MuleResponse;
import de.svenjacobs.loremipsum.LoremIpsum;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.semver.Version;

import javax.net.ssl.SSLContext;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.jayway.restassured.RestAssured.given;
import static com.mulesoft.anypoint.client.util.FunctionalUtil.logResponseAndGetContent;
import static com.mulesoft.performance.test.MasterResource.HTTP_CODE_ERRORS_START;
import static com.mulesoft.performance.test.MasterResource.TLS;
import static org.testng.Assert.fail;

public final class FunctionalUtil {
    public static final String HOST_TO_TEST = "hostToTest_env";
    private static final Logger LOG = Logger.getLogger("Message");
    public final static String NEW_LINE = System.getProperty("line.separator");
    private static final Random RANDOM = new Random();
    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    private static final long TIMEOUT_IN_MILLIS = 60000;
    public static final long WAIT_STATE_BOUNDARY_IN_MILLIS = 1000;

    private FunctionalUtil() {
    }

    public static String getHostToTest(){
        return HOST_TO_TEST;
    }

    /**
     * If test is run on Cloudhub, localhost:8081 is replaced by actual full Cloudhub URL.
     */

    /**
     * @param currentVersion is equal or greater version than @param referenceVersion
     */
    private static boolean isEqualOrGreaterVersion(String referenceVersion, String currentVersion) {
        final Version reference = Version.parse(referenceVersion);
        final Version candidate = Version.parse(currentVersion);
        final int result = candidate.compareTo(reference);
        return result == 1 || result == 0;
    }

    private static boolean isPreviousVersion(String referenceVersion, String currentVersion) {
        final Version reference = Version.parse(referenceVersion);
        final Version candidate = Version.parse(currentVersion);
        final int result = candidate.compareTo(reference);
        return result == -1;
    }

    public static String getHostToTestOnInstance(String publicIp, String canonicalEndpointUrl) {
        if (canonicalEndpointUrl.contains("0.0.0.0:8082") && (!canonicalEndpointUrl.startsWith("https"))) { // Update protocol.
            canonicalEndpointUrl = canonicalEndpointUrl.replaceFirst("http", "https");
        }
        // Update host and port.
        return canonicalEndpointUrl.replaceFirst("0.0.0.0", publicIp);
    }

    public static String getHostToTestOnCloudhub(String fullDomain, String canonicalEndpointUrl) {
        if (canonicalEndpointUrl.contains("0.0.0.0:8082") && (!canonicalEndpointUrl.startsWith("https"))) { // Update protocol.
            canonicalEndpointUrl = canonicalEndpointUrl.replaceFirst("http", "https");
        }
        // Update host and port.
        return canonicalEndpointUrl.replaceFirst("0.0.0.0:(\\d+)", fullDomain);
    }
    /**
     *  Call the provided endpoint using GET until specified HTTP status code is returned or timeout is reached.
     */
    public static void waitUntilEndpointCondition(String endpoint, int expectedStatusCode) {
        LOG.info(String.format("Call endpoint until %s meets expected condition (HTTP code: '%d')..", endpoint, expectedStatusCode));
        boolean isSuccess = false;
        final long timeout = System.currentTimeMillis() + TIMEOUT_IN_MILLIS;
        int statusCode = -1;
        while (System.currentTimeMillis() < timeout) {
            final MuleResponse response;
            try {
                response = MuleResponse.getInstance(given().log().all().relaxedHTTPSValidation(TLS).get(endpoint));
            } catch (Exception e) {
                LOG.warn(String.format("Problem when calling endpoint %s. Re attempting!..", endpoint), e);
                waitForOneCycle();
                continue;
            }
            logResponseAndGetContent(response);
            statusCode = response.getStatusCode();
            if (statusCode == expectedStatusCode) {
                LOG.info(endpoint + NEW_LINE + " endpoint meets expected condition.");
                isSuccess = true;
                break;
            } else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) { // HTTP code is in the 5xx range!
                LOG.error(String.format("HTTP code: %d. Irrecoverable error while while calling endpoint: %s %s. Response: %s", statusCode, endpoint, NEW_LINE, response));
            } else { // HTTP code in the 2xx, 3xx and 4xx range (with the exclusion of 200).
                LOG.warn(String.format("%s endpoint does not meet condition (expected HTTP code: %d , but found: %d). Re attempting!..", endpoint, expectedStatusCode, statusCode));
            }
            waitForOneCycle();
        }
        if (!isSuccess) {
            fail(String.format("Timeout waiting for condition: %s endpoint does not meet condition. Expected HTTP code: %d, but found %d.", endpoint, expectedStatusCode, statusCode));
        }
    }

    /**
     *  Call the provided endpoint using GET until a HTTP code different from 200 is returned or timeout is reached.
     */
    public static void waitUntilEndpointRestricted(String endpoint) {
        LOG.info("Call endpoint until restricted (HTTP code != 200). Endpoint: " + endpoint);
        boolean isSuccess = false;
        final long timeout = System.currentTimeMillis() + TIMEOUT_IN_MILLIS;
        while (System.currentTimeMillis() < timeout) {
            final MuleResponse response = MuleResponse.getInstance(given().log().all().relaxedHTTPSValidation(TLS).get(endpoint));
            logResponseAndGetContent(response);
            final int statusCode = response.getStatusCode();
            if (statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR) { // HTTP code is in the 5xx range!
                LOG.error(String.format("HTTP code: %d. Unexpected error while calling endpoint: %s %s. Response: %s", statusCode, endpoint, NEW_LINE, response));
                waitForOneCycle();
                continue;
            }
            if ((statusCode >= HTTP_CODE_ERRORS_START) && (statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR)) { // HTTP code is in the 4xx range!
                LOG.warn("Endpoint: " + endpoint + " is restricted!");
                isSuccess = true;
                break;
            } else { // HTTP code in 2xx and 3xx!
                LOG.warn("Endpoint: " + endpoint + " is not restricted yet..");
                waitForOneCycle();
            }
        }
        if (!isSuccess) {
            fail("Timeout waiting for condition: Endpoint is not successfully restricted.");
        }
    }

    public static void waitFor(long millis, String reason) {
        if ((millis > WAIT_STATE_BOUNDARY_IN_MILLIS) && (reason == null || reason.isEmpty())) {
            throw new FunctionalException("When specifying long time waits, reason is mandatory");
        }
        LOG.warn("Wait state. Reason: " + reason);
        waitFor(millis);
    }

    public static void waitForOneCycle() {
        waitFor(WAIT_STATE_BOUNDARY_IN_MILLIS);
    }

    public static void waitFor(long millis) {
        if (millis > WAIT_STATE_BOUNDARY_IN_MILLIS) {
            LOG.warn("Long wait condition: " + millis + " millis!");
            final long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            final long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
            if (minutes != 0) {
                for (int i = 1; i <= minutes; i++) {
                    if (seconds == 0) {
                        LOG.warn(String.format("Remaining time: %d of %d minutes", i, minutes));
                    } else {
                        LOG.warn(String.format("Remaining time: %d of %d minutes, %d seconds", i, minutes, seconds));
                    }
                    sleep(60000);
                }
            }
            if (seconds != 0) {
                LOG.warn(String.format("Remaining time: %d seconds to continue", seconds));
                sleep(1000 * seconds);
            }
        } else {
            sleep(millis);
        }
    }

    private static void sleep (long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Do nothing.
        }
    }

    public static String getUniqueName(String prefix, int maxLength) {
        final int idLength = maxLength - prefix.length();
        if (idLength < 1) {
            throw new RuntimeException("Name cannot be created because of prefix is too big: " + prefix.length());
        }
        return (prefix + getUniqueId(idLength));
    }

    /**
     * @return a unique Id composed by letters only.
     */
    public static String getUniqueId(int length) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = CHARS[RANDOM.nextInt(CHARS.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    public static String getLoremIpsumText(int paragraphCount, int cropAtSize) {
        if (cropAtSize == 0) {
            return "";
        }
        final LoremIpsum loremIpsum = new LoremIpsum();
        String candidate = loremIpsum.getParagraphs(paragraphCount);
        if (candidate.length() < cropAtSize) {
            candidate += loremIpsum.getWords(cropAtSize - candidate.length());
        }
        return candidate.substring(0, cropAtSize);
    }

    public static HttpClient getHttpClient() {
        return getHttpClient(null);
    }

    public static HttpClient getHttpClient(RequestConfig requestConfig) {
        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        } catch (Exception e) {
            // Do nothing.
        }
        httpClientBuilder.setSSLContext(sslContext);
        httpClientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
        final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, new String[]{TLS}, null, new NoopHostnameVerifier());
        final Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        final PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingHttpClientConnectionManager.setMaxTotal(500);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(50);
        httpClientBuilder.setConnectionManager(poolingHttpClientConnectionManager);

        if (requestConfig != null) {
            return httpClientBuilder.setDefaultRequestConfig(requestConfig).build();
        } else {
            return httpClientBuilder.build();
        }
    }

    public static boolean isEndpointResponding(HttpClient client, String endpoint) {
        final HttpGet get = new HttpGet(endpoint);
        final HttpResponse response;
        try {
            response = client.execute(get);
            EntityUtils.consumeQuietly(response.getEntity()); // Solves ConnectionPoolTimeoutException.
        } catch (Exception e) {
            LOG.warn("Problems when trying to ping endpoint " + endpoint, e);
            return false;
        }
        return response.getStatusLine().getStatusCode() < HTTP_CODE_ERRORS_START;
    }

    public static boolean isEqualOrGreaterPolicyVersion(String referenceVersion, String targetVersion) {
        final int referenceVersionInt = Integer.valueOf(referenceVersion.replace("v", ""));
        final int targetVersionInt = Integer.valueOf(targetVersion.replace("v", ""));
        return targetVersionInt >= referenceVersionInt;
    }
}
