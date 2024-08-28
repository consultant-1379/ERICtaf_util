package com.ericsson.cifwk.taf.ddc;

import java.io.FileInputStream;
import java.util.Properties;
import java.io.File;

public class Graphite {

    public static final String PROPERTIES = "graphite.properties";
    public static final String HOST_PROPERTY = "graphite.host";

    AMQP amqp;

    public Graphite init(String host) {
        Properties properties = new Properties();
        FileInputStream file= null;
        try {
            File jarPath=new File(Graphite.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            String propertiesPath=jarPath.getParentFile().getPath();
            file = new FileInputStream(propertiesPath+"/"+PROPERTIES);
            properties.load(file);
        } catch (Exception e) {
            throw new DDCHandlerException("graphite.properties.load.error");
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {
                    throw new DDCHandlerException("graphite.properties.close.error");
                }
            }
        }
        if (host.trim().isEmpty()) {
            host = properties.getProperty(HOST_PROPERTY, "");
        }
        if (!host.trim().isEmpty() && !host.startsWith(".")) {
            host = '.' + host;
        }
        String propertyPrefix = "graphite" + host;
        amqp = new AMQP().init(properties, propertyPrefix);
        return this;
    }

    public void send(String message) {
        amqp.send(message);
    }

    public void shutdown() {
        amqp.shutdown();
    }
}

