package com.ericsson.cifwk.taf.monitoring.config;


import com.ericsson.cifwk.taf.monitoring.agent.monitor.MonitorType;

public class MonitorConfig {

    private String monitorName;
    private String address;
    private String[] hosts;
    private MonitorType type;

    public MonitorConfig(String monitorName, MonitorType type, String[] hosts, String address) {
        this.monitorName = monitorName;
        this.address = address;
        this.hosts = hosts;
        this.type = type;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(String monitorName) {
        this.monitorName = monitorName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String[] getHosts() {
        return hosts;
    }

    public void setHosts(String[] hosts) {
        this.hosts = hosts;
    }

    public MonitorType getType() {
        return type;
    }

    public void setType(MonitorType type) {
        this.type = type;
    }
}
