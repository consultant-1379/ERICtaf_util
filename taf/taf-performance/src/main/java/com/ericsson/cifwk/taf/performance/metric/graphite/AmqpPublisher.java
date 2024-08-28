package com.ericsson.cifwk.taf.performance.metric.graphite;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AmqpPublisher {

    private final Logger logger = LoggerFactory.getLogger(AmqpPublisher.class);

    private final ConnectionFactory factory;
    private final String exchange;
    private Connection connection;
    private Channel channel;

    public AmqpPublisher(String host,
                         int port,
                         String username,
                         String password,
                         String exchange) {
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        this.exchange = exchange;
    }

    public AmqpPublisher(Host host, String exchange) {
        this(host.getIp(),
                Integer.parseInt(host.getPort().get(Ports.AMQP)),
                host.getUser(),
                host.getPass(),
                exchange);
    }

    public void connect() throws IOException {
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public void send(String name, String value, long seconds) throws IOException {
        String message = String.format("%s %s %d\n", name, value, seconds);
        logger.debug("Reporting: {}", message.trim());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .deliveryMode(2)
                .build();
        channel.basicPublish(exchange, "", props, message.getBytes("UTF-8"));
    }

    public void shutdown() throws IOException {
        channel.close();
        connection.close();
    }

}
