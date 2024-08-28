package com.ericsson.cifwk.taf.performance.sample.impl;

import com.ericsson.cifwk.taf.performance.sample.BodyConsumer;
import com.google.common.base.Preconditions;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class AmqpClient {

    private final ConnectionFactory factory;
    private final String exchange;
    private Connection connection;
    private Channel channel;

    public AmqpClient(ConnectionFactory factory,
                      String exchange) {
        this.factory = factory;
        this.exchange = exchange;
    }

    public static AmqpClient create(String amqpUri, String exchange)
            throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(amqpUri);
        return new AmqpClient(factory, exchange);
    }

    public static AmqpClient create(String host, int port, String username, String password, String exchange) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        return new AmqpClient(factory, exchange);
    }

    public void connect() throws IOException {
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(exchange, "fanout", true);
    }

    public void send(byte[] bytes) throws IOException {
        Preconditions.checkNotNull(channel, "Client not connected");
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .deliveryMode(2)
                .build();
        channel.basicPublish(exchange, "", props, bytes);
    }

    public void subscribe(final BodyConsumer consumer) throws IOException {
        String queue = channel.queueDeclare().getQueue();
        channel.queueBind(queue, exchange, "");
        channel.basicConsume(queue, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                long deliveryTag = envelope.getDeliveryTag();
                consumer.handle(body);
                channel.basicAck(deliveryTag, false);
            }
        });
    }

    public void shutdown() throws IOException {
        if (channel.isOpen()) {
            channel.close();
            channel = null;
        }
        if (connection.isOpen()) {
            connection.close();
        }
    }

}
