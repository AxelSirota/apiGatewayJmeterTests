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

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.mulesoft.performance.setup.environment.Characteristic.*;
import static com.mulesoft.performance.setup.environment.Scenario.*;

public enum Runtime {
    QA_GATEWAY_132(QA, "1.3.2", "qa2_gateway_132", "API-GATEWAY-1.3.2", null, CONNECTOR, "v4", Raml.v0_8),
    QA_GATEWAY_202(QA, "2.0.2", "qa2_gateway_203", "2.0.2-Gateway-R44-CI-SNAPSHOT", "1.3.1", LISTENER, "v5", Raml.v0_8),
    QA_GATEWAY_204(QA, "2.0.4", "qa2_gateway_204", "2.0.4-Gateway-R44-CI-SNAPSHOT", "1.3.1", LISTENER_NO_CREATE, "v5", Raml.v0_8),
    QA_GATEWAY_210(QA, "2.1.0", "qa2_gateway_210", "2.1.0-Gateway-R44-CI-SNAPSHOT", "1.3.1", LISTENER, "v6", Raml.v0_8),
    QA_GATEWAY_211(QA, "2.1.1", "qa2_gateway_211", "2.1.1-Gateway-R44-CI-SNAPSHOT", "1.3.1", LISTENER, "v6", Raml.v0_8),
    QA_GATEWAY_212(QA, "2.1.2", "qa2_gateway_212", "API-GATEWAY-2.1.2", "1.3.1", LISTENER, "v6", Raml.v0_8),
    QA_GATEWAY_220(QA, "2.2.0", "qa2_gateway_220", "2.2.0-Gateway-R44-CI-SNAPSHOT", "1.3.1", LISTENER, "v7", Raml.v0_8),
    QA_GATEWAY_221(QA, "2.2.1-SNAPSHOT", "qa2_gateway_221", "API-GATEWAY-2.2.1", "1.4.1", LISTENER, "v7", Raml.v0_8),
    QA_GATEWAY_380(QA, "3.8.0", "qa2_gateway_380", "3.8.0-R50-CI-SNAPSHOT", "1.4.1", LISTENER_UNIFIED, "v8", Raml.v0_8),
    QA_GATEWAY_380_RAML_v1_0 (QA, "3.8.0", "qa2_gateway_380_raml_v1_0", "3.8.0-R50-CI-SNAPSHOT", "1.4.1", LISTENER_UNIFIED_RAML_v1_0, "v8", Raml.v1_0),
    QA_GATEWAY_381(QA, "3.8.1", "qa2_gateway_381", "3.8.1-R50-CI-SNAPSHOT", "1.4.1", LISTENER_UNIFIED, "v9", Raml.v0_8),
    QA_GATEWAY_381_RAML_v1_0(QA, "3.8.1", "qa2_gateway_381_raml_v1_0", "3.8.1-R50-CI-SNAPSHOT", "1.4.2", LISTENER_UNIFIED_RAML_v1_0, "v9", Raml.v1_0),
    QA_GATEWAY_382(QA, "3.8.2", "qa2_gateway_382", "3.8.2-R50-CI-SNAPSHOT", "1.5.0", LISTENER_UNIFIED, "v9", Raml.v0_8),
    QA_GATEWAY_382_RAML_v1_0(QA, "3.8.2", "qa2_gateway_382_raml_v1_0", "3.8.2-R50-CI-SNAPSHOT", "1.5.0", LISTENER_UNIFIED_RAML_v1_0, "v9", Raml.v1_0),
    QA_GATEWAY_383(QA, "3.8.3", "qa2_gateway_383", "3.8.3-R50-CI-SNAPSHOT", "1.5.0", LISTENER_UNIFIED, "v9", Raml.v0_8),
    QA_GATEWAY_383_RAML_v1_0(QA, "3.8.3", "qa2_gateway_383_raml_v1_0", "3.8.3-R50-CI-SNAPSHOT", "1.5.0", LISTENER_UNIFIED_RAML_v1_0, "v9", Raml.v1_0),
    QA_GATEWAY_384_RAML_v1_0(QA, "3.8.4-SNAPSHOT", "qa2_gateway_384_raml_v1_0", "3.8.4", "1.5.0", LISTENER_UNIFIED_RAML_v1_0, "v9", Raml.v1_0),
    STG_GATEWAY_132(STG,"1.3.2", "stg_gateway_132", "API-GATEWAY-1.3.2", null, CONNECTOR, "v4", Raml.v0_8),
    STG_GATEWAY_203(STG, "2.0.3", "stg_gateway_203", "Gateway-2.0.3-R44-CI-SNAPSHOT", "1.3.1", LISTENER, "v5", Raml.v0_8),
    STG_GATEWAY_204(STG, "2.0.4", "stg_gateway_204", "Gateway-2.0.4-R44-CI-SNAPSHOT", "1.3.0", LISTENER_NO_CREATE, "v5", Raml.v0_8),
    STG_GATEWAY_210(STG, "2.1.0", "stg_gateway_210", "Gateway-2.1.0-R44-CI-SNAPSHOT", "1.3.0", LISTENER, "v6", Raml.v0_8),
    STG_GATEWAY_211(STG, "2.1.1", "stg_gateway_211", "Gateway-2.1.1-R44-CI-SNAPSHOT", "1.3.1", LISTENER, "v6", Raml.v0_8),
    STG_GATEWAY_220(STG, "2.2.0", "stg_gateway_220", "API Gateway 2.2.0", "1.3.0", LISTENER, "v7", Raml.v0_8),
    STG_GATEWAY_380(STG, "3.8.0", "stg_gateway_380", "3.8.0-HF1", "1.4.1", LISTENER_UNIFIED, "v8", Raml.v0_8),
    STG_GATEWAY_380_RAML_v1_0(STG, "3.8.0", "stg_gateway_380_raml_v1_0", "3.8.0-HF1", "1.4.1", LISTENER_UNIFIED_RAML_v1_0, "v8", Raml.v1_0),
    STG_GATEWAY_381(STG, "3.8.1", "stg_gateway_381", "3.8.1", "1.4.1", LISTENER_UNIFIED, "v9", Raml.v0_8),
    STG_GATEWAY_381_RAML_v1_0(STG, "3.8.1", "stg_gateway_381_raml_v1_0", "3.8.1", "1.4.2", LISTENER_UNIFIED_RAML_v1_0, "v9", Raml.v1_0),
    STG_GATEWAY_382(STG, "3.8.2", "stg_gateway_382", "3.8.2", "1.5.0", LISTENER_UNIFIED, "v9", Raml.v0_8),
    STG_GATEWAY_382_RAML_v1_0(STG, "3.8.2", "stg_gateway_382_raml_v1_0", "3.8.2", "1.5.0", LISTENER_UNIFIED_RAML_v1_0, "v9", Raml.v1_0),
    STG_GATEWAY_383(STG, "3.8.3", "stg_gateway_383", "3.8.3", "1.5.0", LISTENER_UNIFIED, "v9", Raml.v0_8),
    STG_GATEWAY_383_RAML_v1_0(STG, "3.8.3", "stg_gateway_383_raml_v1_0", "3.8.3", "1.5.0", LISTENER_UNIFIED_RAML_v1_0, "v9", Raml.v1_0),
    STG_GATEWAY_384_RAML_v1_0(STG, "3.8.4-SNAPSHOT", "stg_gateway_384_raml_v1_0", "3.8.4", "1.5.0", LISTENER_UNIFIED_RAML_v1_0, "v9", Raml.v1_0),
    PROD_GATEWAY_132(PROD, "1.3.2", "prod_gateway_132", "API Gateway 1.3.2", Agent.VERSION_DEFAULT, CONNECTOR, "v4", Raml.v0_8),
    PROD_GATEWAY_203(PROD, "2.0.3", "prod_gateway_203", "API Gateway 2.0.3", Agent.VERSION_DEFAULT, LISTENER, "v5", Raml.v0_8),
    PROD_GATEWAY_204(PROD, "2.0.4", "prod_gateway_204", "API Gateway 2.0.4", Agent.VERSION_DEFAULT, LISTENER_NO_CREATE, "v5", Raml.v0_8),
    PROD_GATEWAY_210(PROD, "2.1.0", "prod_gateway_210", "API Gateway 2.1.0", Agent.VERSION_DEFAULT, LISTENER, "v6", Raml.v0_8),
    PROD_GATEWAY_211(PROD, "2.1.1", "prod_gateway_211", "API Gateway 2.1.1", Agent.VERSION_DEFAULT, LISTENER, "v6", Raml.v0_8),
    PROD_GATEWAY_212(PROD, "2.1.2-SNAPSHOT", "prod_gateway_212", "API Gateway 2.1.2", Agent.VERSION_DEFAULT, LISTENER, "v6", Raml.v0_8),
    PROD_GATEWAY_220(PROD, "2.2.0", "prod_gateway_220", "API Gateway 2.2.0", Agent.VERSION_DEFAULT, LISTENER, "v7", Raml.v0_8),
    PROD_GATEWAY_221(PROD, "2.2.1-SNAPSHOT", "prod_gateway_221", "API Gateway 2.2.1", Agent.VERSION_DEFAULT, LISTENER, "v7", Raml.v0_8),
    PROD_GATEWAY_380(PROD, "3.8.0", "prod_gateway_380", "3.8.0", Agent.VERSION_DEFAULT, LISTENER_UNIFIED, "v8", Raml.v0_8),
    PROD_GATEWAY_380_RAML_v1_0(PROD, "3.8.0", "prod_gateway_380_raml_v1_0", "3.8.0", Agent.VERSION_DEFAULT, LISTENER_UNIFIED_RAML_v1_0, Raml.v0_8, Raml.v1_0),
    PROD_GATEWAY_381(PROD, "3.8.1", "prod_gateway_381", "3.8.1", Agent.VERSION_DEFAULT, LISTENER_UNIFIED, "v9", Raml.v0_8),
    PROD_GATEWAY_381_RAML_v1_0(PROD, "3.8.1", "prod_gateway_381_raml_v1_0", "3.8.1", Agent.VERSION_DEFAULT, LISTENER_UNIFIED_RAML_v1_0, "v9", Raml.v1_0),
    PROD_GATEWAY_382(PROD, "3.8.2", "prod_gateway_382", "3.8.2", Agent.VERSION_DEFAULT, LISTENER_UNIFIED, "v9", Raml.v0_8),
    PROD_GATEWAY_382_RAML_v1_0(PROD, "3.8.2", "prod_gateway_382_raml_v1_0", "3.8.2", Agent.VERSION_DEFAULT, LISTENER_UNIFIED_RAML_v1_0, "v9", Raml.v1_0),
    PROD_GATEWAY_383(PROD, "3.8.3", "prod_gateway_383", "3.8.3", Agent.VERSION_DEFAULT, LISTENER_UNIFIED, "v9", Raml.v0_8),
    PROD_GATEWAY_383_RAML_v1_0(PROD, "3.8.3", "prod_gateway_383_raml_v1_0", "3.8.3", Agent.VERSION_DEFAULT, LISTENER_UNIFIED_RAML_v1_0, "v9", Raml.v1_0),
    PROD_GATEWAY_384(PROD, "3.8.4-SNAPSHOT", "prod_gateway_384", "3.8.4", Agent.VERSION_DEFAULT, LISTENER_UNIFIED, "v9", Raml.v0_8),
    PROD_GATEWAY_384_RAML_v1_0(PROD, "3.8.4-SNAPSHOT", "prod_gateway_384_raml_v1_0", "3.8.4", Agent.VERSION_DEFAULT, LISTENER_UNIFIED_RAML_v1_0, "v9", Raml.v1_0),
    NONE(null, "none", "none", "N/A", "none", LISTENER, "none", "none");

    private static final Logger LOG = Logger.getLogger(Runtime.class);

    private static Map<String, Runtime> environmentMap = new HashMap<>();

    static {
        for (Runtime environment : Runtime.values()) {
            environmentMap.put(environment.getEnvironment(), environment);
        }
    }

    private String environment;
    private String version;
    private String cloudhubLabel;
    private String agentVersion;
    private Characteristic environmentType;
    private Scenario scenario;
    private String policySupportedVersion;
    private String ramlVersion;

    Runtime(Characteristic environmentType, String version, String environment, String cloudhubLabel, String agentVersion, Scenario scenario, String policySupportedVersion, String ramlVersion) {
        this.environmentType = environmentType;
        this.environment = environment;
        this.version = version;
        this.cloudhubLabel = cloudhubLabel;
        this.agentVersion =agentVersion;
        this.scenario = scenario;
        this.policySupportedVersion = policySupportedVersion;
        this.ramlVersion = ramlVersion;
    }

    public static Runtime fromString(String candidateEnvironment) {
        if (candidateEnvironment == null) {
            throw new NullPointerException("Error when trying to instantiate Cloudhub environment, expected candidate environment, but found null");
        }
        final Runtime environment = environmentMap.get(candidateEnvironment.trim());
        if (environment == null) {
            throw new TypeNotPresentException("Expected match between environment and candidate, but found: '" + candidateEnvironment + "'.", null);
        }
        LOG.info("Gateway: " + environment);
        return environment;
    }

    public String getVersion() {
        return version;
    }


    public String getAgentVersion() {
        return agentVersion;
    }

    public String getCloudhubLabel() {
        return cloudhubLabel;
    }

    public String getEnvironment() {
        return environment;
    }

    public Characteristic getEnvironmentType() {
        return environmentType;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public String getPolicySupportedVersion() {
        return policySupportedVersion;
    }

    public String getRamlVersion() {
        return ramlVersion;
    }

    @Override
    public String toString() {
        return String.format("Environment: %s, API Gateway version: %s, Agent version : %s, Cloudhub label: %s, Test App: %s working with RAML: %s",
                environmentType == null ? null : environmentType.getValue(),
                getVersion() == null ? "VERSION_DEFAULT" :  getVersion(),
                getAgentVersion(),
                getCloudhubLabel(),
                getScenario().getApplicationFullPath(),
                getRamlVersion());
    }

    private static class Agent {
        private final static String VERSION_DEFAULT = "1.5.3"; // Value "null" means default agent. Only works on Prod for now.
    }

    private static class Raml {
        private final static String v0_8 = "0.8";
        private final static String v1_0 = "1.0";
    }
}
