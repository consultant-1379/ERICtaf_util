package com.ericsson.cifwk.taf.testng;

import org.testng.ISuite;

public class SuiteReader {

    static final String PARAMETER_START_DELAY = "taf.start-delay";
    static final String PARAMETER_REPEAT_UNTIL = "taf.repeat-until";
    static final String PARAMETER_REPEAT_COUNT = "taf.repeat-count";
    static final String PARAMETER_THINK_TIME = "taf.think-time";
    static final String PARAMETER_VUSERS = "taf.vusers";
    static final String PARAMETER_CRON_EXPRESSION = "taf.cron";

    static final int DEFAULT_START_DELAY = 0;
    public static final long DEFAULT_REPEAT_UNTIL = Long.MAX_VALUE;
    public static final int DEFAULT_REPEAT_COUNT = 1;
    static final int DEFAULT_THINK_TIME = 0;
    static final int DEFAULT_VUSERS = 1;
    static final String DEFAULT_CRON_EXPRESSION = null;

    private final ISuite suite;

    public SuiteReader(ISuite suite) {
        this.suite = suite;
    }

    public String getName() {
        return suite.getName();
    }

    public long getStartDelay() {
        String startDelay = suite.getParameter(PARAMETER_START_DELAY);
        if (startDelay == null || startDelay.trim().isEmpty()) {
            return DEFAULT_START_DELAY;
        }
        return Long.parseLong(startDelay) * 1000;
    }

    protected boolean isRepeatUntilSet(){
        String repeatCount = suite.getParameter(PARAMETER_REPEAT_UNTIL);
        if(repeatCount == null || repeatCount.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    public long getRepeatUntil() {
        if(isRepeatUntilSet()) {
            return Long.parseLong(suite.getParameter(PARAMETER_REPEAT_UNTIL)) * 1000 + System.currentTimeMillis();
        }
        return DEFAULT_REPEAT_UNTIL;
    }

    protected boolean isRepeatCountSet(){
        String repeatCount = suite.getParameter(PARAMETER_REPEAT_COUNT);
        if(repeatCount == null || repeatCount.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    public int getRepeatCount() {
        if (isRepeatCountSet()) {
            return Integer.parseInt(suite.getParameter(PARAMETER_REPEAT_COUNT));
        }
        return DEFAULT_REPEAT_COUNT;
    }

    public int getThinkTime() {
        String thinkTime = suite.getParameter(PARAMETER_THINK_TIME);
        if (thinkTime == null || thinkTime.trim().isEmpty()) {
            return DEFAULT_THINK_TIME;
        }
        return Integer.parseInt(thinkTime);
    }

    public int getVusers() {
        String vusers = suite.getParameter(PARAMETER_VUSERS);
        if (vusers == null || vusers.trim().isEmpty()) {
            return DEFAULT_VUSERS;
        }
        return Integer.parseInt(vusers);
    }

    public String getCronExpression() {
        String expression = suite.getParameter(PARAMETER_CRON_EXPRESSION);
        if (expression == null || expression.trim().isEmpty()) {
            return DEFAULT_CRON_EXPRESSION;
        }
        return expression;
    }

}
