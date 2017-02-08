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
import java.util.Set;
import java.util.logging.Logger;

public enum Characteristic {
    FEDERATED_ORGANIZATION("federated_organization"),
    PING_FEDERATE_ORGANIZATION("ping_federate_organization"),
    QA("qa"),
    STG("stg"),
    PROD("prod");

    // TODO(nahuel): Remove qa, stg and prod from here.

    private static Logger LOG = Logger.getLogger(Characteristic.class.getName());

    private String value;

    Characteristic(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static String getAsText(Set<Characteristic> characteristics) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (Characteristic characteristic : characteristics) {
            stringBuilder
                    .append(characteristic.getValue())
                    .append(" ");
        }
        return stringBuilder.toString();
    }

    private static Map<String, Characteristic> environmentMap = new HashMap<>();

    static {
        for (Characteristic characteristic : Characteristic.values()) {
            environmentMap.put(characteristic.getValue(), characteristic);
        }
    }

    public static Characteristic fromString(String candidateCharacteristic) {
        if (candidateCharacteristic == null) {
            throw new NullPointerException("Error when trying to instantiate organization Characteristic, expected candidate characteristic, but found null");
        }
        final Characteristic characteristic = environmentMap.get(candidateCharacteristic.trim());
        if (characteristic == null) {
            throw new TypeNotPresentException("Expected match between characteristic and value, but found: '" + candidateCharacteristic + "'.", null);
        }
        LOG.info("Organization Characteristic: " + characteristic);
        return characteristic;
    }
}
