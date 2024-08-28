package com.ericsson.cifwk.taf.handlers.jms;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface JmsConsumer {

    TextMessage expectTextMessage(String content, long timeout);

    BytesMessage expectByteMessage(byte[] content, long timeout);

    ObjectMessage expectObjectMessage(Object content, long timeout);

    MapMessage expectMapMessage(Object content, long timeout);

    StreamMessage expectStreamMessage(InputStream content, long timeout);

    void listen(MessageListener listener);

    List<Message> fetchMessages(int maxCount, int i);

}
