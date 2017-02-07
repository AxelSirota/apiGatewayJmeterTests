/*
 * API Gateway
 * Copyright 2010-2015 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p/>
 * This software is protected under international copyright law. All use of this software is
 * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
 * separately entered into in writing between you and MuleSoft. If such an agreement is not
 * in place, you may not use the software.
 */

package com.mulesoft.performance.setup.environment;

import com.mulesoft.anypoint.client.entity.OAuthParams;
import com.mulesoft.anypoint.client.entity.UserData;
import com.mulesoft.anypoint.client.exception.FunctionalException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

// TODO(nahuel):  Create enum for the different organization types and remove most of boolean methods here.

public enum Platform {
    QA("qa") {
        @Override
        public UserData getOrganizationOwner() {
            return new UserData("david_banner", "Mule1234");
        }

        @Override
        public String getApiPlatformBaseUrl() {
            return QAX_API_PLATFORM_BASE_URL;
        }

        @Override
        public String getCoreServicesBaseUrl() {
            return QAX_CORE_SERVICES_BASE_URL;
        }

        @Override
        public String getContractServiceUrl(){
            return QAX_CONTRACT_CACHE_SERVICE_BASE_URL;
        }

        @Override
        public String getAnalyticsBaseUrl() {
            // Original: "https://analytics-ingest.qax.anypoint.mulesoft.com"
            return QAX_ANALYTICS_PROXY_URL;
        }

        @Override
        public String getCloudhubBaseUrl() {
            return QAX_CLOUDHUB_BASE_URL;
        }

        @Override
        protected final void initEnvCharacteristics() {
            final Set<Characteristic> envCharacteristics = getCharacteristics();
            envCharacteristics.add(Characteristic.QA);
            envCharacteristics.add(Characteristic.ANALYTICS);
            envCharacteristics.add(Characteristic.MOCKED_ANALYTICS);
        }

        @Override
        public OAuthParams getOAuthParams() {
            throw new UnsupportedOperationException("Only for Federated Orgs");
        }

        @Override
        public String getDomainFragment() {
            return CH_DOMAIN_FRAGMENT_QA;
        }
    },
    STG("stg") {
        @Override
        public UserData getOrganizationOwner() {
            return new UserData("parker2", "Mule1234");
        }

        @Override
        public String getApiPlatformBaseUrl() {
            return STGX_API_PLATFORM_BASE_URL;
        }

        @Override
        public String getCoreServicesBaseUrl() {
            return STGX_CORE_SERVICES_BASE_URL;
        }

        @Override
        public String getContractServiceUrl(){
            return STGX_CONTRACT_CACHE_SERVICE_BASE_URL;
        }

        @Override
        public String getAnalyticsBaseUrl() {
            return STGX_ANALYTICS_PROXY_URL;
        }

        @Override
        public String getCloudhubBaseUrl() {
            return STGX_CLOUDHUB_BASE_URL;
        }

        @Override
        protected final void initEnvCharacteristics() {
            final Set<Characteristic> envCharacteristics = getCharacteristics();
            envCharacteristics.add(Characteristic.STG);
            envCharacteristics.add(Characteristic.ANALYTICS);
            envCharacteristics.add(Characteristic.MOCKED_ANALYTICS);
        }

        @Override
        public OAuthParams getOAuthParams() {
            throw new UnsupportedOperationException("Only for Federated Orgs");
        }

        @Override
        public String getDomainFragment() {
            return CH_DOMAIN_FRAGMENT_STG;
        }
    },
    PARAMETRIC_ORG("parametric_org") {
        @Override
        public UserData getOrganizationOwner() {
            return new UserData(System.getProperty(KEY_ORGANIZATION_OWNER_USERNAME), System.getProperty(KEY_ORGANIZATION_OWNER_PASSWORD));
        }

        @Override
        public String getApiPlatformBaseUrl() {
            final String result = System.getProperty(KEY_BASE_URL_PLATFORM);
            if ((result == null) || (result.isEmpty())) {
                throw new FunctionalException("No value specified for: " + KEY_BASE_URL_PLATFORM);
            }
            return result;
        }

        @Override
        public String getCoreServicesBaseUrl() {
            final String result = System.getProperty(KEY_BASE_URL_CORE_SERVICES);
            if ((result == null) || (result.isEmpty())) {
                throw new FunctionalException("No value specified for: " + KEY_BASE_URL_CORE_SERVICES);
            }
            return result;
        }

        @Override
        public String getContractServiceUrl(){
            final String result = System.getProperty(KEY_BASE_URL_CONTRACT_CACHE_SERVICES);
            if ((result == null) || (result.isEmpty())) {
                throw new FunctionalException("No value specified for: " + KEY_BASE_URL_CONTRACT_CACHE_SERVICES);
            }
            return result;
        }

        @Override
        public String getAnalyticsBaseUrl() {
            return QAX_ANALYTICS_PROXY_URL;
        }

        @Override
        public String getCloudhubBaseUrl() {
            return "no_url_for_cloudhub";
        }

        @Override
        protected final void initEnvCharacteristics() {
            final String result = System.getProperty(KEY_ORGANIZATION_CHARACTERISTICS);
            final Set<Characteristic> envCharacteristics = getCharacteristics();
            if ((result != null) && (!result.trim().isEmpty())) {
                final String[] values = result.split(",");
                for (String candidateCharacteristic : values) {
                    envCharacteristics.add(Characteristic.fromString(candidateCharacteristic.trim()));
                }
            }
        }

        @Override
        public OAuthParams getOAuthParams() {
            throw new UnsupportedOperationException("Only for Federated Orgs");
        }

        @Override
        public String getDomainFragment() {
            throw new UnsupportedOperationException("Cloudhub not supported 'On Premises");
        }
    },
    STG_FEDERATED("stg_federated") {
        @Override
        public UserData getOrganizationOwner() {
            return new UserData("federated_owner", "Mule1234");
        }

        @Override
        public String getApiPlatformBaseUrl() {
            return STGX_API_PLATFORM_BASE_URL;
        }

        @Override
        public String getCoreServicesBaseUrl() {
            return STGX_CORE_SERVICES_BASE_URL;
        }

        @Override
        public String getContractServiceUrl(){
            return STGX_CONTRACT_CACHE_SERVICE_BASE_URL;
        }

        @Override
        public String getAnalyticsBaseUrl() {
            return STGX_ANALYTICS_PROXY_URL;
        }

        @Override
        public String getCloudhubBaseUrl() {
            return STGX_CLOUDHUB_BASE_URL;
        }

        @Override
        protected final void initEnvCharacteristics() {
            final Set<Characteristic> envCharacteristics = getCharacteristics();
            envCharacteristics.add(Characteristic.STG);
            envCharacteristics.add(Characteristic.ANALYTICS);
            envCharacteristics.add(Characteristic.MOCKED_ANALYTICS);
            envCharacteristics.add(Characteristic.FEDERATED_ORGANIZATION);
            envCharacteristics.add(Characteristic.PING_FEDERATE_ORGANIZATION);
        }

        @Override
        public OAuthParams getOAuthParams() {
            final OAuthParams oAuthParams = new OAuthParams();
            oAuthParams.setHttpsProtocol();
            oAuthParams.setHost("ec2-52-88-144-83.us-west-2.compute.amazonaws.com");
            oAuthParams.setPort("9031");
            oAuthParams.setAuthorizationEndpointPath("as/authorization.oauth2");
            oAuthParams.setAccessTokenEndpointPath("as/token.oauth2");
            // TODO(nahuel): The following data should be checked.
            oAuthParams.setTokenTtlSeconds(7200);
            oAuthParams.setAuthorizationCodeEnabled(true);
            oAuthParams.setEnableRefreshToken(true);
            oAuthParams.setImplicitEnabled(true);
            oAuthParams.setClientCredentialsEnabled(true);
            oAuthParams.setResourceOwnerPasswordCredentialsEnabled(true);
            oAuthParams.setAuthorizationCodeEnabled(true);
            return oAuthParams;
        }

        @Override
        public String getDomainFragment() {
            return CH_DOMAIN_FRAGMENT_STG;
        }
    },
    STG_PAMS("stg_pfpams") {
        @Override
        public UserData getOrganizationOwner() {
            // master pfpams
            // a-level0: david.banner
            // a-level1: peter.parker
            // a-level2: emmet.brown
            // b-level0: tony.stark
            return new UserData("pfpams", "Mule1234");
        }

        @Override
        public String getApiPlatformBaseUrl() {
            return STGX_API_PLATFORM_BASE_URL;
        }

        @Override
        public String getCoreServicesBaseUrl() {
            return STGX_CORE_SERVICES_BASE_URL;
        }

        @Override
        public String getContractServiceUrl(){
            return STGX_CONTRACT_CACHE_SERVICE_BASE_URL;
        }

        @Override
        public String getAnalyticsBaseUrl() {
            // Original: "https://analytics-ingest.qa.anypoint.mulesoft.com"
            return STGX_ANALYTICS_PROXY_URL;
        }

        @Override
        public String getCloudhubBaseUrl() {
            return STGX_CLOUDHUB_BASE_URL;
        }

        @Override
        protected final void initEnvCharacteristics() {
            final Set<Characteristic> envCharacteristics = getCharacteristics();
            envCharacteristics.add(Characteristic.STG);
            envCharacteristics.add(Characteristic.FEDERATED_ORGANIZATION);
            envCharacteristics.add(Characteristic.PING_FEDERATE_ORGANIZATION);
            envCharacteristics.add(Characteristic.PAMS_ORGANIZATION);
            envCharacteristics.add(Characteristic.MOCKED_PAMS);
            envCharacteristics.add(Characteristic.ANALYTICS);
            envCharacteristics.add(Characteristic.MOCKED_ANALYTICS);
        }

        @Override
        public OAuthParams getOAuthParams() {
            final OAuthParams oAuthParams = new OAuthParams();
            oAuthParams.setHttpsProtocol();
            oAuthParams.setHost("ec2-52-88-144-83.us-west-2.compute.amazonaws.com");
            oAuthParams.setPort("9031");
            oAuthParams.setAuthorizationEndpointPath("as/authorization.oauth2");
            oAuthParams.setAccessTokenEndpointPath("as/token.oauth2");
            // TODO(nahuel): The following data should be checked.
            oAuthParams.setTokenTtlSeconds(7200);
            oAuthParams.setAuthorizationCodeEnabled(true);
            oAuthParams.setEnableRefreshToken(true);
            oAuthParams.setImplicitEnabled(true);
            oAuthParams.setClientCredentialsEnabled(true);
            oAuthParams.setResourceOwnerPasswordCredentialsEnabled(true);
            oAuthParams.setAuthorizationCodeEnabled(true);
            return oAuthParams;
        }

        @Override
        public String getDomainFragment() {
            return CH_DOMAIN_FRAGMENT_STG;
        }
    },
    STG_OPEN_AM_11("stg_openam11") {
        @Override
        public UserData getOrganizationOwner() {
            return new UserData("openam_test", "Mule1234");
        }

        @Override
        public String getApiPlatformBaseUrl() {
            return STGX_API_PLATFORM_BASE_URL;
        }

        @Override
        public String getCoreServicesBaseUrl() {
            return STGX_CORE_SERVICES_BASE_URL;
        }

        @Override
        public String getContractServiceUrl(){
            return STGX_CONTRACT_CACHE_SERVICE_BASE_URL;
        }

        @Override
        public String getAnalyticsBaseUrl() {
            // Original: "https://analytics-ingest.qa.anypoint.mulesoft.com"
            return STGX_ANALYTICS_PROXY_URL;
        }

        @Override
        public String getCloudhubBaseUrl() {
            return STGX_CLOUDHUB_BASE_URL;
        }

        @Override
        protected final void initEnvCharacteristics() {
            final Set<Characteristic> envCharacteristics = getCharacteristics();
            envCharacteristics.add(Characteristic.STG);
            envCharacteristics.add(Characteristic.FEDERATED_ORGANIZATION);
            envCharacteristics.add(Characteristic.OPEN_AM_ORGANIZATION);
            envCharacteristics.add(Characteristic.ANALYTICS);
            envCharacteristics.add(Characteristic.MOCKED_ANALYTICS);
        }

        @Override
        public OAuthParams getOAuthParams() {
            final OAuthParams oAuthParams = new OAuthParams();
            oAuthParams.setHttpsProtocol();
            oAuthParams.setHost("ec2-54-149-141-104.us-west-2.compute.amazonaws.com");
            oAuthParams.setPort("8443");
            oAuthParams.setAuthorizationEndpointPath("openam/oauth2/authorize");
            oAuthParams.setAccessTokenEndpointPath("openam/oauth2/access_token");
            // TODO(nahuel): The following data should be checked.
            oAuthParams.setTokenTtlSeconds(7200);
            oAuthParams.setAuthorizationCodeEnabled(true);
            oAuthParams.setEnableRefreshToken(true);
            oAuthParams.setImplicitEnabled(true);
            oAuthParams.setClientCredentialsEnabled(true);
            oAuthParams.setResourceOwnerPasswordCredentialsEnabled(true);
            oAuthParams.setAuthorizationCodeEnabled(true);
            return oAuthParams;
        }

        @Override
        public String getDomainFragment() {
            return CH_DOMAIN_FRAGMENT_STG;
        }
    },
    QA_LAB("qa_lab") {
        @Override
        public UserData getOrganizationOwner() {
            return new UserData("qa_lab", "Mule1234");
        }

        @Override
        public String getApiPlatformBaseUrl() {
            return QAX_API_PLATFORM_BASE_URL;
        }

        @Override
        public String getCoreServicesBaseUrl() {
            return QAX_CORE_SERVICES_BASE_URL;
        }

        @Override
        public String getContractServiceUrl(){
            return QAX_CONTRACT_CACHE_SERVICE_BASE_URL;
        }

        @Override
        public String getAnalyticsBaseUrl() {
            return QAX_ANALYTICS_PROXY_URL;
        }

        @Override
        public String getCloudhubBaseUrl() {
            return QAX_CLOUDHUB_BASE_URL;
        }

        @Override
        protected final void initEnvCharacteristics() {
            final Set<Characteristic> envCharacteristics = getCharacteristics();
            envCharacteristics.add(Characteristic.QA);
            envCharacteristics.add(Characteristic.ANALYTICS);
            envCharacteristics.add(Characteristic.MOCKED_ANALYTICS);
            envCharacteristics.add(Characteristic.FEDERATED_ORGANIZATION);
            envCharacteristics.add(Characteristic.PING_FEDERATE_ORGANIZATION);
        }

        @Override
        public OAuthParams getOAuthParams() {
            final OAuthParams oAuthParams = new OAuthParams();
            oAuthParams.setHttpsProtocol();
            oAuthParams.setHost("ec2-52-88-144-83.us-west-2.compute.amazonaws.com");
            oAuthParams.setPort("9031");
            oAuthParams.setAuthorizationEndpointPath("as/authorization.oauth2");
            oAuthParams.setAccessTokenEndpointPath("as/token.oauth2");
            // TODO(nahuel): The following data should be checked.
            oAuthParams.setTokenTtlSeconds(7200);
            oAuthParams.setAuthorizationCodeEnabled(true);
            oAuthParams.setEnableRefreshToken(true);
            oAuthParams.setImplicitEnabled(true);
            oAuthParams.setClientCredentialsEnabled(true);
            oAuthParams.setResourceOwnerPasswordCredentialsEnabled(true);
            oAuthParams.setAuthorizationCodeEnabled(true);
            return oAuthParams;
        }

        @Override
        public String getDomainFragment() {
            return CH_DOMAIN_FRAGMENT_QA;
        }
    },
    QA_FEDERATED("qa_federated") {
        @Override
        public UserData getOrganizationOwner() {
            return new UserData("pingfederateqa", "Mule1234");
        }

        @Override
        public String getApiPlatformBaseUrl() {
            return QAX_API_PLATFORM_BASE_URL;
        }

        @Override
        public String getCoreServicesBaseUrl() {
            return QAX_CORE_SERVICES_BASE_URL;
        }

        @Override
        public String getContractServiceUrl(){
            return QAX_CONTRACT_CACHE_SERVICE_BASE_URL;
        }

        @Override
        public String getAnalyticsBaseUrl() {
            // Original: "https://analytics-ingest.qa.anypoint.mulesoft.com"
            return QAX_ANALYTICS_PROXY_URL;
        }

        @Override
        public String getCloudhubBaseUrl() {
            return QAX_CLOUDHUB_BASE_URL;
        }

        @Override
        protected final void initEnvCharacteristics() {
            final Set<Characteristic> envCharacteristics = getCharacteristics();
            envCharacteristics.add(Characteristic.QA);
            envCharacteristics.add(Characteristic.FEDERATED_ORGANIZATION);
            envCharacteristics.add(Characteristic.PING_FEDERATE_ORGANIZATION);
            envCharacteristics.add(Characteristic.ANALYTICS);
            envCharacteristics.add(Characteristic.MOCKED_ANALYTICS);
        }

        @Override
        public OAuthParams getOAuthParams() {
            final OAuthParams oAuthParams = new OAuthParams();
            oAuthParams.setHttpsProtocol();
            oAuthParams.setHost("ec2-52-88-144-83.us-west-2.compute.amazonaws.com");
            oAuthParams.setPort("9031");
            oAuthParams.setAuthorizationEndpointPath("as/authorization.oauth2");
            oAuthParams.setAccessTokenEndpointPath("as/token.oauth2");
            // TODO(nahuel): The following data should be checked.
            oAuthParams.setTokenTtlSeconds(7200);
            oAuthParams.setAuthorizationCodeEnabled(true);
            oAuthParams.setEnableRefreshToken(true);
            oAuthParams.setImplicitEnabled(true);
            oAuthParams.setClientCredentialsEnabled(true);
            oAuthParams.setResourceOwnerPasswordCredentialsEnabled(true);
            oAuthParams.setAuthorizationCodeEnabled(true);
            return oAuthParams;
        }

        @Override
        public String getDomainFragment() {
            return CH_DOMAIN_FRAGMENT_QA;
        }
    },
    QA_PAMS("pfpams") {
        @Override
        public UserData getOrganizationOwner() {
            return new UserData("pfpams2", "Mule1234");
        }

        @Override
        public String getApiPlatformBaseUrl() {
            return QAX_API_PLATFORM_BASE_URL;
        }

        @Override
        public String getCoreServicesBaseUrl() {
            return QAX_CORE_SERVICES_BASE_URL;
        }

        @Override
        public String getContractServiceUrl(){
            return QAX_CONTRACT_CACHE_SERVICE_BASE_URL;
        }

        @Override
        public String getAnalyticsBaseUrl() {
            // Original: "https://analytics-ingest.qa.anypoint.mulesoft.com"
            return QAX_ANALYTICS_PROXY_URL;
        }

        @Override
        public String getCloudhubBaseUrl() {
            return QAX_CLOUDHUB_BASE_URL;
        }

        @Override
        protected final void initEnvCharacteristics() {
            final Set<Characteristic> envCharacteristics = getCharacteristics();
            envCharacteristics.add(Characteristic.QA);
            envCharacteristics.add(Characteristic.FEDERATED_ORGANIZATION);
            envCharacteristics.add(Characteristic.PING_FEDERATE_ORGANIZATION);
            envCharacteristics.add(Characteristic.PAMS_ORGANIZATION);
            envCharacteristics.add(Characteristic.MOCKED_PAMS);
            envCharacteristics.add(Characteristic.ANALYTICS);
            envCharacteristics.add(Characteristic.MOCKED_ANALYTICS);
        }

        @Override
        public OAuthParams getOAuthParams() {
            final OAuthParams oAuthParams = new OAuthParams();
            oAuthParams.setHttpsProtocol();
            oAuthParams.setHost("ec2-52-88-144-83.us-west-2.compute.amazonaws.com");
            oAuthParams.setPort("9031");
            oAuthParams.setAuthorizationEndpointPath("as/authorization.oauth2");
            oAuthParams.setAccessTokenEndpointPath("as/token.oauth2");
            // TODO(nahuel): The following data should be checked.
            oAuthParams.setTokenTtlSeconds(7200);
            oAuthParams.setAuthorizationCodeEnabled(true);
            oAuthParams.setEnableRefreshToken(true);
            oAuthParams.setImplicitEnabled(true);
            oAuthParams.setClientCredentialsEnabled(true);
            oAuthParams.setResourceOwnerPasswordCredentialsEnabled(true);
            oAuthParams.setAuthorizationCodeEnabled(true);
            return oAuthParams;
        }

        @Override
        public String getDomainFragment() {
            return CH_DOMAIN_FRAGMENT_QA;
        }
    },
    QA_OPEN_AM_11("qa_openam11") {
        @Override
        public UserData getOrganizationOwner() {
            return new UserData("openam_test", "Mule1234");
        }

        @Override
        public String getApiPlatformBaseUrl() {
            return QAX_API_PLATFORM_BASE_URL;
        }

        @Override
        public String getCoreServicesBaseUrl() {
            return QAX_CORE_SERVICES_BASE_URL;
        }

        @Override
        public String getContractServiceUrl(){
            return QAX_CONTRACT_CACHE_SERVICE_BASE_URL;
        }

        @Override
        public String getAnalyticsBaseUrl() {
            // Original: "https://analytics-ingest.qa.anypoint.mulesoft.com"
            return QAX_ANALYTICS_PROXY_URL;
        }

        @Override
        public String getCloudhubBaseUrl() {
            return QAX_CLOUDHUB_BASE_URL;
        }

        @Override
        protected final void initEnvCharacteristics() {
            final Set<Characteristic> envCharacteristics = getCharacteristics();
            envCharacteristics.add(Characteristic.QA);
            envCharacteristics.add(Characteristic.FEDERATED_ORGANIZATION);
            envCharacteristics.add(Characteristic.OPEN_AM_ORGANIZATION);
            envCharacteristics.add(Characteristic.ANALYTICS);
            envCharacteristics.add(Characteristic.MOCKED_ANALYTICS);
        }

        @Override
        public OAuthParams getOAuthParams() {
            final OAuthParams oAuthParams = new OAuthParams();
            oAuthParams.setHttpsProtocol();
            oAuthParams.setHost("ec2-54-149-141-104.us-west-2.compute.amazonaws.com");
            oAuthParams.setPort("8443");
            oAuthParams.setAuthorizationEndpointPath("openam/oauth2/authorize");
            oAuthParams.setAccessTokenEndpointPath("openam/oauth2/access_token");
            // TODO(nahuel): The following data should be checked.
            oAuthParams.setTokenTtlSeconds(7200);
            oAuthParams.setAuthorizationCodeEnabled(true);
            oAuthParams.setEnableRefreshToken(true);
            oAuthParams.setImplicitEnabled(true);
            oAuthParams.setClientCredentialsEnabled(true);
            oAuthParams.setResourceOwnerPasswordCredentialsEnabled(true);
            oAuthParams.setAuthorizationCodeEnabled(true);
            return oAuthParams;
        }

        @Override
        public String getDomainFragment() {
            return CH_DOMAIN_FRAGMENT_QA;
        }
    },
    QA_OPEN_AM_12("qa_openam12") {
        @Override
        public UserData getOrganizationOwner() {
            return new UserData("openam12_test", "Mule1234");
        }

        @Override
        public String getApiPlatformBaseUrl() {
            return QAX_API_PLATFORM_BASE_URL;
        }

        @Override
        public String getCoreServicesBaseUrl() {
            return QAX_CORE_SERVICES_BASE_URL;
        }

        @Override
        public String getContractServiceUrl(){
            return QAX_CONTRACT_CACHE_SERVICE_BASE_URL;
        }

        @Override
        public String getAnalyticsBaseUrl() {
            // Original: "https://analytics-ingest.qa.anypoint.mulesoft.com"
            return QAX_ANALYTICS_PROXY_URL;
        }

        @Override
        public String getCloudhubBaseUrl() {
            return QAX_CLOUDHUB_BASE_URL;
        }

        @Override
        protected final void initEnvCharacteristics() {
            final Set<Characteristic> envCharacteristics = getCharacteristics();
            envCharacteristics.add(Characteristic.QA);
            envCharacteristics.add(Characteristic.FEDERATED_ORGANIZATION);
            envCharacteristics.add(Characteristic.OPEN_AM_ORGANIZATION);
            envCharacteristics.add(Characteristic.ANALYTICS);
            envCharacteristics.add(Characteristic.MOCKED_ANALYTICS);
        }

        @Override
        public OAuthParams getOAuthParams() {
            final OAuthParams oAuthParams = new OAuthParams();
            oAuthParams.setHttpsProtocol();
            oAuthParams.setHost("ec2-52-10-207-37.us-west-2.compute.amazonaws.com");
            oAuthParams.setPort("8443");
            oAuthParams.setAuthorizationEndpointPath("openam/oauth2/authorize");
            oAuthParams.setAccessTokenEndpointPath("openam/oauth2/access_token");
            // TODO(nahuel): The following data should be checked.
            oAuthParams.setTokenTtlSeconds(7200);
            oAuthParams.setAuthorizationCodeEnabled(true);
            oAuthParams.setEnableRefreshToken(true);
            oAuthParams.setImplicitEnabled(true);
            oAuthParams.setClientCredentialsEnabled(true);
            oAuthParams.setResourceOwnerPasswordCredentialsEnabled(true);
            oAuthParams.setAuthorizationCodeEnabled(true);
            return oAuthParams;
        }

        @Override
        public String getDomainFragment() {
            return CH_DOMAIN_FRAGMENT_QA;
        }
    },
    PROD("prod") {
        @Override
        public UserData getOrganizationOwner() {
            return new UserData("peter_parker", "Mule1234");
        }

        @Override
        public String getApiPlatformBaseUrl() {
            return PROD_API_PLATFORM_BASE_URL;
        }

        @Override
        public String getCoreServicesBaseUrl() {
            return PROD_CORE_SERVICES_BASE_URL;
        }

        @Override
        public String getContractServiceUrl(){
            return PROD_CONTRACT_CACHE_SERVICE_BASE_URL;
        }

        @Override
        public String getAnalyticsBaseUrl() {
            // Original: "https://analytics-ingest.anypoint.mulesoft.com";
            return PROD_ANALYTICS_PROXY_URL;
        }

        @Override
        public String getCloudhubBaseUrl() {
            return PROD_CLOUDHUB_BASE_URL;
        }

        @Override
        protected final void initEnvCharacteristics() {
            final Set<Characteristic> envCharacteristics = getCharacteristics();
            envCharacteristics.add(Characteristic.PROD);
            envCharacteristics.add(Characteristic.ANALYTICS);
            envCharacteristics.add(Characteristic.MOCKED_ANALYTICS);
        }

        @Override
        public OAuthParams getOAuthParams() {
            throw new UnsupportedOperationException("Only for Federated Orgs");
        }

        @Override
        public String getDomainFragment() {
            return CH_DOMAIN_FRAGMENT_PROD;
        }
    },
    PROD_FEDERATED("prod_federated") {
        @Override
        public UserData getOrganizationOwner() {
            return new UserData("federated_owner", "Mule1234");
        }

        @Override
        public String getApiPlatformBaseUrl() {
            return PROD_API_PLATFORM_BASE_URL;
        }

        @Override
        public String getCoreServicesBaseUrl() {
            return PROD_CORE_SERVICES_BASE_URL;
        }

        @Override
        public String getContractServiceUrl(){
            return PROD_CONTRACT_CACHE_SERVICE_BASE_URL;
        }

        @Override
        public String getAnalyticsBaseUrl() {
            //Original: "https://analytics-ingest.anypoint.mulesoft.com";
            return PROD_ANALYTICS_PROXY_URL;
        }

        @Override
        public String getCloudhubBaseUrl() {
            return PROD_CLOUDHUB_BASE_URL;
        }

        @Override
        protected final void initEnvCharacteristics() {
            final Set<Characteristic> envCharacteristics = getCharacteristics();
            envCharacteristics.add(Characteristic.PROD);
            envCharacteristics.add(Characteristic.FEDERATED_ORGANIZATION);
            envCharacteristics.add(Characteristic.PING_FEDERATE_ORGANIZATION);
            envCharacteristics.add(Characteristic.ANALYTICS);
            envCharacteristics.add(Characteristic.MOCKED_ANALYTICS);
        }

        @Override
        public OAuthParams getOAuthParams() {
            final OAuthParams oAuthParams = new OAuthParams();
            oAuthParams.setHttpsProtocol();
            oAuthParams.setHost("ec2-52-88-144-83.us-west-2.compute.amazonaws.com");
            oAuthParams.setPort("9031");
            oAuthParams.setAuthorizationEndpointPath("as/authorization.oauth2");
            oAuthParams.setAccessTokenEndpointPath("as/token.oauth2");
            // TODO(nahuel): The following data should be checked.
            oAuthParams.setTokenTtlSeconds(7200);
            oAuthParams.setAuthorizationCodeEnabled(true);
            oAuthParams.setEnableRefreshToken(true);
            oAuthParams.setImplicitEnabled(true);
            oAuthParams.setClientCredentialsEnabled(true);
            oAuthParams.setResourceOwnerPasswordCredentialsEnabled(true);
            oAuthParams.setAuthorizationCodeEnabled(true);
            return oAuthParams;
        }

        @Override
        public String getDomainFragment() {
            return CH_DOMAIN_FRAGMENT_PROD;
        }
    };

    private static Map<String, Platform> environmentMap = new HashMap<>();

    static {
        for (Platform environment : Platform.values()) {
            environmentMap.put(environment.getEnvironment(), environment);
        }
    }

    private static Logger LOG = Logger.getLogger(Platform.class.getName());
    private String environment;
    private Set<Characteristic> characteristics;

    private static final String CH_DOMAIN_FRAGMENT_QA = ".qax.cloudhub.io";
    private static final String CH_DOMAIN_FRAGMENT_STG = ".stgx.cloudhub.io";
    private static final String CH_DOMAIN_FRAGMENT_PROD = ".cloudhub.io";

    private static final String QAX_API_PLATFORM_BASE_URL = "https://qax.anypoint.mulesoft.com/apiplatform";
    private static final String QAX_CORE_SERVICES_BASE_URL = "https://qax.anypoint.mulesoft.com/accounts";
    private static final String QAX_CONTRACT_CACHE_SERVICE_BASE_URL = "https://qax.anypoint.mulesoft.com/apigateway/ccs";
    private static final String QAX_CLOUDHUB_BASE_URL = "https://qax.anypoint.mulesoft.com/cloudhub";
    private static final String QAX_ANALYTICS_PROXY_URL = "http://analytics-mockv3.cloudhub.io/qa";

    private static final String STGX_API_PLATFORM_BASE_URL = "https://stgx.anypoint.mulesoft.com/apiplatform";
    private static final String STGX_CORE_SERVICES_BASE_URL = "https://stgx.anypoint.mulesoft.com/accounts";
    private static final String STGX_CONTRACT_CACHE_SERVICE_BASE_URL = "https://stgx.anypoint.mulesoft.com/apigateway/ccs";
    private static final String STGX_CLOUDHUB_BASE_URL = "https://stgx.anypoint.mulesoft.com/cloudhub";
    private static final String STGX_ANALYTICS_PROXY_URL = "http://analytics-mockv3.cloudhub.io/stg";

    private static final String PROD_API_PLATFORM_BASE_URL = "https://anypoint.mulesoft.com/apiplatform";
    private static final String PROD_CORE_SERVICES_BASE_URL = "https://anypoint.mulesoft.com/accounts";
    private static final String PROD_CONTRACT_CACHE_SERVICE_BASE_URL = "https://anypoint.mulesoft.com/apigateway/ccs";
    private static final String PROD_CLOUDHUB_BASE_URL = "https://anypoint.mulesoft.com/cloudhub";
    private static final String PROD_ANALYTICS_PROXY_URL = "http://analytics-mockv3.cloudhub.io/prod";

    public static final String KEY_BASE_URL_PLATFORM = "base.url.platform";
    public static final String KEY_BASE_URL_CORE_SERVICES = "base.url.core.services";
    public static final String KEY_BASE_URL_CONTRACT_CACHE_SERVICES = "base.url.contract.cache.service";
    public static final String KEY_ORGANIZATION_OWNER_USERNAME = "base.organization.owner.username";
    public static final String KEY_ORGANIZATION_OWNER_PASSWORD = "base.organization.owner.password";
    public static final String KEY_ORGANIZATION_CHARACTERISTICS = "base.organization.characteristics";

    Platform(String environment) {
        this.environment = environment;
        characteristics = new HashSet<>();
        initEnvCharacteristics();
    }

    public static Platform fromString(String candidateEnvironment) {
        if (candidateEnvironment == null) {
            LOG.info("Environment not found on command line. Defaulting to: " + getDefaultEnvironment().getEnvironment());
            return getDefaultEnvironment();
        }
        final Platform environment = environmentMap.get(candidateEnvironment.trim());
        if (environment == null) {
            throw new TypeNotPresentException("Expected match between environment and candidate, but found: '" + candidateEnvironment + "'.", null);
        }
        return environment;
    }

    public static Platform getDefaultEnvironment() {
        return QA;
    }

    public String getEnvironment() {
        return environment;
    }

    public abstract UserData getOrganizationOwner();

    public abstract String getApiPlatformBaseUrl();

    public abstract String getCoreServicesBaseUrl();

    public abstract String getAnalyticsBaseUrl();

    public abstract String getCloudhubBaseUrl();

    public abstract String getContractServiceUrl();

    public abstract OAuthParams getOAuthParams();

    public Set<Characteristic> getCharacteristics() {
        return characteristics;
    }

    protected abstract void initEnvCharacteristics();

    public abstract String getDomainFragment();

    public Characteristic getEnvironmentType() {
        if (characteristics.contains(Characteristic.QA)) {
            return Characteristic.QA;
        }
        if (characteristics.contains(Characteristic.STG)) {
            return Characteristic.STG;
        }
        if (characteristics.contains(Characteristic.PROD)) {
            return Characteristic.PROD;
        }
        return null;
    }

    @Override
    public String toString() {
        return getEnvironmentType().getValue() + " organization. Organization owner: " + getOrganizationOwner().getUsername();
    }
}
