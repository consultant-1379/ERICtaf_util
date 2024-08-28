package com.ericsson.cifwk.taf.scenario.api;

public class TestFlowResult {
    private String flowName;
    private Throwable exception;
    private int vuserId;

    public TestFlowResult(final String flowName, final int vuserId, final Throwable exception) {
        this.flowName = flowName;
        this.exception = exception;
        this.vuserId = vuserId;
    }

    public static TestFlowResult success(String name, int vuserid){ return new TestFlowResult(name, vuserid, null);}

    public static TestFlowResult failure(String name, int vuserid, Throwable exception){ return new TestFlowResult(name, vuserid, exception);}

    public String getFlowName() {
        return flowName;
    }

    public Throwable getException() {
        return exception;
    }

    public boolean isSuccessful(){
        return exception == null;
    }

    public int getVuserId() {
        return vuserId;
    }
}
