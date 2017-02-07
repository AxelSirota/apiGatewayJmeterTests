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

import static com.mulesoft.performance.misc.FunctionalUtil.waitFor;

public class TimeFrame {
    private final long periodInMillis;
    private long currentStartTimeInMillis;
    private final long deltaInMillis;

    public static TimeFrame getInstance(Clock startTime, long periodInMillis, long deltaInMillis) {
        return new TimeFrame(startTime, periodInMillis, deltaInMillis);
    }

    private TimeFrame(Clock startTime, long periodInMillis, long deltaInMillis) {
        this.currentStartTimeInMillis = startTime.getTimeInMillis();
        this.periodInMillis = periodInMillis;
        this.deltaInMillis = deltaInMillis;
    }

    public Clock getRemainingTime() {
        final long currentTime = System.currentTimeMillis();
        currentStartTimeInMillis = updateStartTime(currentStartTimeInMillis, currentTime);
        return Clock.getInstance(currentStartTimeInMillis + periodInMillis - currentTime);

    }

    public void waitUntilStartOfNewWindow() {
        final long remainingTime = getRemainingTime().getTimeInMillis();
        waitFor(remainingTime, "Wait until next time window");
    }

    private boolean isInRange(long currentStart, long currentTime) {
        return currentStart <= currentTime && (currentStart + periodInMillis - deltaInMillis) >= currentTime;

    }

    private long updateStartTime(long currentStart, long time) {
        long start = currentStart;
        if (!isInRange(start, time)) {
            start = updateStartTime(currentStart + periodInMillis, time);
        }
        return start;
    }
}
