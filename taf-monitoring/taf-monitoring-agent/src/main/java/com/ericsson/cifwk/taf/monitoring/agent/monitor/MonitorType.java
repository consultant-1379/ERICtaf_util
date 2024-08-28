package com.ericsson.cifwk.taf.monitoring.agent.monitor;

public enum MonitorType {

    OS("OS"),
    PROC("PROC"),
    JMX("JMX"),
    JBOSS("JBOSS");


    private String type;

    MonitorType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
