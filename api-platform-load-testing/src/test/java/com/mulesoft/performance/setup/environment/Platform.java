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

import com.mulesoft.anypoint.client.entity.UserData;
import com.mulesoft.anypoint.client.exception.FunctionalException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

// TODO(nahuel):  Create enum for the different organization types and remove most of boolean methods here.

public enum Platform {
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
        public String getAnalyticsBaseUrl() {
            return null;
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
        return PARAMETRIC_ORG;
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

    public Set<Characteristic> getCharacteristics() {
        return characteristics;
    }

    protected abstract void initEnvCharacteristics();

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
