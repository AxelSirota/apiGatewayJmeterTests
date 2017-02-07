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

import com.mulesoft.anypoint.client.exception.NotFoundException;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public enum Scenario {
    CONNECTOR("apip-scenario-connector"),
    LISTENER("apip-scenario-listener"),
    LISTENER_NO_CREATE("apip-scenario-listener-no-create"),
    LISTENER_UNIFIED("apip-scenario-unified"),
    LISTENER_UNIFIED_RAML_v1_0("apip-scenario-unified-raml-1");

    private String name;

    Scenario(String name) {
        this.name = name;
    }

    private static Map<String, Scenario> appMap = new HashMap<>();

    static {
        for (Scenario testAppData : Scenario.values()) {
            appMap.put(testAppData.getApplicationName(), testAppData);
        }
    }

    private static Logger LOG = Logger.getLogger(Scenario.class.getName());

    private static final String TEST_APP_RELATIVE_PATH = "scenario/";
    private static final String TEST_APP_EXTENSION = ".zip";

    public String getApplicationName() {
        return name;
    }

    public String getApplicationFullName() {
        return name + TEST_APP_EXTENSION;
    }

    public String getApplicationFullPath() {
        final String relativeAppPath = TEST_APP_RELATIVE_PATH + getApplicationFullName();
        final String path;
        final URL url = getClass().getClassLoader().getResource(relativeAppPath);
        if (url != null) {
            path = url.getFile();
        } else {
            throw new NotFoundException("Path of test application not found on classpath! Path: " + relativeAppPath);
        }
        return new File(path).getAbsolutePath();
    }

    public String getApplicationPath() {
        final String relativeAppPath = TEST_APP_RELATIVE_PATH + getApplicationFullName();
        final String path;
        final URL url = getClass().getClassLoader().getResource(relativeAppPath);
        if (url != null) {
            path = url.getFile();
        } else {
            throw new NotFoundException("Path of test application not found on classpath! Path: " + relativeAppPath);
        }
        return new File(path).getAbsolutePath().replace(getApplicationFullName(), "");
    }

    public File getApplication() {
        final String relativeAppPath = TEST_APP_RELATIVE_PATH + getApplicationFullName();
        final String path;
        final URL url = getClass().getClassLoader().getResource(relativeAppPath);
        if (url != null) {
            path = url.getFile();
        } else {
            throw new NotFoundException("Path of test application not found on classpath! Path: " + relativeAppPath);
        }
        return new File(path);
    }

    public static Scenario fromString(String candidateTestScenario) {
        if (candidateTestScenario == null) {
            LOG.info("Test App data specification not found at command line. Defaulting to: " + LISTENER.getApplicationName());
            return LISTENER;
        }
        final Scenario scenario = appMap.get(candidateTestScenario.trim());
        if (scenario == null) {
            throw new TypeNotPresentException("Expected match between test application and candidate, but found: '" + candidateTestScenario + "'.", null);
        }
        return scenario;
    }
}
