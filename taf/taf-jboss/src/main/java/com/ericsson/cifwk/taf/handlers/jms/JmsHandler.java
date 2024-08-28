package com.ericsson.cifwk.taf.handlers.jms;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import java.io.InputStream;
import java.util.Map;

/**
 *
 */
public interface JmsHandler {
    JmsProducer getSender();

    void close();

    TextMessage textMessage(String text);

    ObjectMessage objectMessage(Object value);

    BytesMessage byteMessage(byte[] bytes);

    MapMessage mapMessage(Map<?, ?> map);

    StreamMessage streamMessage(InputStream stream);

    JmsConsumer getReceiver();

    void clear();
}
