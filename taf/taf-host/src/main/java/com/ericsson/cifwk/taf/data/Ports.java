package com.ericsson.cifwk.taf.data;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import com.ericsson.cifwk.meta.API;
import com.google.gson.annotations.SerializedName;

/**
 * Enum to specify the types of ports available on each host
 */
@API(Stable)
public enum Ports {
    @SerializedName("http")
    HTTP("HTTP"),
    @SerializedName("https")
    HTTPS("HTTPS"),
    @SerializedName("ssh")
    SSH("SSH"),
    @SerializedName("jmx")
    JMX("JMX"),
    @SerializedName("jms")
    JMS("JMS"),
    @SerializedName("jboss_management")
    JBOSS_MANAGEMENT("JBOSS_MANAGEMENT"),
    @SerializedName("rmi")
    RMI("RMI"),
    @SerializedName("amqp")
    AMQP("AMQP"),
    @SerializedName("unknown")
    UNKNOWN("UNKNOWN");

    public static String SSH_PORT = "ssh";
    public static String HTTP_PORT = "http";
    public static String HTTPS_PORT = "https";

    private String name;

    Ports(String defaultName) {
        this.name = defaultName;
    }

    public String toString() {
        return name;
    }

    public static Ports getByName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
