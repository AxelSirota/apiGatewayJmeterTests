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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public enum RuntimeLocation {
    LOCAL("local") {
        @Override
        public String getBaseHttpUrl() {
            String port = System.getProperty(KEY_PORT_HTTP);
            if ((port == null) || (port.trim().isEmpty())) {
                port = "8887";
            }
            return "http://127.0.0.1:" + port;
        }

        @Override
        public String getBaseHttpsUrl() {
            String port = System.getProperty(KEY_PORT_HTTPS);
            if ((port == null) || (port.trim().isEmpty())) {
                port = "8887";
            }
            return "https://127.0.0.1:" + port;
        }
    },
    REMOTE("remote") {
        @Override
        public String getBaseHttpUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }

        @Override
        public String getBaseHttpsUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }
    },
    REMOTE_USING_ARM("remote_using_arm") {
        @Override
        public String getBaseHttpUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }

        @Override
        public String getBaseHttpsUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }
    },
    REMOTE_CLUSTER_PRIMARY("remote_cluster_primary") {
        @Override
        public String getBaseHttpUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }

        @Override
        public String getBaseHttpsUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }
    },
    REMOTE_CLUSTER_PRIMARY_USING_ARM("remote_cluster_primary_using_arm") {
        @Override
        public String getBaseHttpUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }

        @Override
        public String getBaseHttpsUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }
    },
    REMOTE_CLUSTER_SECONDARY("remote_cluster_secondary") {
        @Override
        public String getBaseHttpUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }

        @Override
        public String getBaseHttpsUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }
    },
    REMOTE_CLUSTER_SECONDARY_USING_ARM("remote_cluster_secondary_using_arm") {
        @Override
        public String getBaseHttpUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }

        @Override
        public String getBaseHttpsUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }
    },
    REMOTE_CLUSTER_SINGLE_NODE("remote_cluster_single") {
        @Override
        public String getBaseHttpUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }

        @Override
        public String getBaseHttpsUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }
    },
    REMOTE_CLUSTER_SINGLE_NODE_USING_ARM("remote_cluster_single_using_arm") {
        @Override
        public String getBaseHttpUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }

        @Override
        public String getBaseHttpsUrl() {
            throw new UnsupportedOperationException("Please, get On Prem util variables from OnPremiseHelper.");
        }
    },
    REMOTE_QA_LAB_LOAD_BALANCER("qa_lab") {
        @Override
        public String getBaseHttpUrl() {
            return "http://qa-lab.mulesoft.net";
        }

        @Override
        public String getBaseHttpsUrl() {
            return "https://qa-lab.mulesoft.net";
        }
    },
    REMOTE_QA_LAB_NODE_MASTER("qa_lab_master") {
        @Override
        public String getBaseHttpUrl() {
            return "http://ec2-35-164-98-19.us-west-2.compute.amazonaws.com:8081";
        }

        @Override
        public String getBaseHttpsUrl() {
            return "https://ec2-35-164-98-19.us-west-2.compute.amazonaws.com:8082";
        }
    },
    REMOTE_QA_LAB_NODE_SLAVE("qa_lab_slave") {
        @Override
        public String getBaseHttpUrl() {
            return "http://ec2-35-163-199-70.us-west-2.compute.amazonaws.com:8081";
        }

        @Override
        public String getBaseHttpsUrl() {
            return "https://ec2-35-163-199-70.us-west-2.compute.amazonaws.com:8082";
        }
    };

    private static Map<String, RuntimeLocation> environmentMap = new HashMap<>();

    static {
        for (RuntimeLocation environment : RuntimeLocation.values()) {
            environmentMap.put(environment.getEnvironment(), environment);
        }
    }

    private static Logger LOG = Logger.getLogger(RuntimeLocation.class.getName());
    private String environment;

    public static final String KEY_PORT_HTTP = "app.port.http";
    public static final String KEY_PORT_HTTPS = "app.port.https";

    RuntimeLocation(String environment) {
        this.environment = environment;
    }

    public static RuntimeLocation getDefaultEnvironment() {
        return LOCAL;
    }

    public static RuntimeLocation fromString(String candidateEnvironment) {
        if (candidateEnvironment == null) {
            LOG.info("Environment not found on command line. Defaulting to: " + getDefaultEnvironment().getEnvironment());
            return getDefaultEnvironment();
        }
        final RuntimeLocation environment = environmentMap.get(candidateEnvironment.trim());
        if (environment == null) {
            throw new TypeNotPresentException("Expected match between environment and candidate, but found: '" + candidateEnvironment + "'.", null);
        }
        return environment;
    }

    public abstract String getBaseHttpUrl();

    public abstract String getBaseHttpsUrl();

    public String getEnvironment() {
        return environment;
    }

    @Override
    public String toString() {
        try {
            return String.format("HTTP URL: %s, HTTPs URL: %s", getBaseHttpUrl(), getBaseHttpsUrl());
        } catch (UnsupportedOperationException e) {
            return "Environment: " + this.name() + ". No base HTTP(s) URL";
        }
    }

    public boolean isManagedThroughRuntimeManager() {
        switch (this) {
            case REMOTE:
            case REMOTE_USING_ARM:
            case REMOTE_CLUSTER_PRIMARY:
            case REMOTE_CLUSTER_PRIMARY_USING_ARM:
            case REMOTE_CLUSTER_SECONDARY:
            case REMOTE_CLUSTER_SECONDARY_USING_ARM:
            case REMOTE_CLUSTER_SINGLE_NODE:
            case REMOTE_CLUSTER_SINGLE_NODE_USING_ARM:
                return true;
            default:
                return false;
        }
    }

    public boolean isManagedUsingArm() {
        switch (this) {
            case REMOTE:
            case REMOTE_CLUSTER_PRIMARY:
            case REMOTE_CLUSTER_SECONDARY:
            case REMOTE_CLUSTER_SINGLE_NODE:
                return false;
            case REMOTE_USING_ARM:
            case REMOTE_CLUSTER_PRIMARY_USING_ARM:
            case REMOTE_CLUSTER_SECONDARY_USING_ARM:
            case REMOTE_CLUSTER_SINGLE_NODE_USING_ARM:
                return true;
            default:
                return false;
        }
    }
}
