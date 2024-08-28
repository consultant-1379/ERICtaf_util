package com.ericsson.cifwk.taf.monitoring.agent.jmx;

import javax.management.remote.JMXServiceURL;

public class ConnectionDescription {

    private JMXServiceURL url;
    private String beanName;


    public ConnectionDescription(JMXServiceURL url, String beanName) {
        this.url = url;
        this.beanName = beanName;
    }

    public JMXServiceURL getUrl() {
        return url;
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof ConnectionDescription)){
            return false;
        }
        ConnectionDescription toCompare = (ConnectionDescription) o;
        return this.getBeanName().equals(toCompare.getBeanName()) && this.getUrl().equals(toCompare.getUrl());
    }


    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + beanName.hashCode();
        return result;
    }
}
