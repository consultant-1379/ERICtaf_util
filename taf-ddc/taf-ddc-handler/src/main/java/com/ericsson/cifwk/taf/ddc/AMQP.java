package com.ericsson.cifwk.taf.ddc;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class AMQP {

    static final Logger LOG = LoggerFactory.getLogger(AMQP.class);

    private Connection connection;
    private Channel channel;
    String exchange;

    public AMQP init(Properties properties, String propertyPrefix) {
        propertyPrefix = propertyPrefix + ".amqp";
        String host = properties.getProperty(propertyPrefix + ".host");
        String port = properties.getProperty(propertyPrefix + ".port");
        String user = properties.getProperty(propertyPrefix + ".username", "");
        String password = properties.getProperty(propertyPrefix + ".password", "");
        String exchange = properties.getProperty(propertyPrefix + ".exchange", "");
        connect(host, port, user, password, exchange);
        if (LOG.isDebugEnabled())
            LOG.debug(Messages.format("AMQP connection successes to:{2}@{0}:{1}/exchange:{3}", host, port, user, exchange));
        return this;
    }

    void connect(String host, String port, String user, String password, String exchange) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(Integer.parseInt(port));
            factory.setUsername(user);
            factory.setPassword(password);
            connection = factory.newConnection();
            channel = connection.createChannel();
            this.exchange = exchange;
        } catch (Exception e) {
            LOG.debug(Messages.format("AMQP can't connect to: {2}@{0}:{1}/exchange:{3}", e, host, port, user, exchange));
            throw new DDCHandlerException("amqp.connection.error", e, host, port, user);
        }
    }

    public void send(String message) {
        BasicProperties properties = new BasicProperties.Builder().deliveryMode(2).build();
        try {
            byte[] msg = message.getBytes("UTF-8");
            channel.basicPublish(exchange, "", properties, msg);
            if (LOG.isDebugEnabled())
                LOG.debug(Messages.format("AMQP message:{0}", message));
        } catch (Exception e) {
            LOG.error(Messages.format("amqp.message.send.error", message, e.getMessage()), e);
        }
    }

    public void shutdown() {
        try {
            channel.close();
        } catch (Exception ignore) {
        }
        try {
            connection.close();
        } catch (Exception ignore) {
        }
    }

}

