/*
 * API Gateway
 * Copyright 2010-2015 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p/>
 * This software is protected under international copyright law. All use of this software is
 * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
 * separately entered into in writing between you and MuleSoft. If such an agreement is not
 * in place, you may not use the software.
 */

package com.mulesoft.performance.setup.instance;

import java.io.Serializable;

public class Clock implements Serializable {

    final long timeInMillis;

    private Clock(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public static Clock getInstance(long timeInMillis) {
        return new Clock(timeInMillis);
    }

    public static Clock now() {
        return new Clock(System.currentTimeMillis());
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }
}
