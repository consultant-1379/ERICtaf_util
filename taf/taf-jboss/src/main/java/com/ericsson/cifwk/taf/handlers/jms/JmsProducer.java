package com.ericsson.cifwk.taf.handlers.jms;

import javax.jms.Message;

/**
 *
 */
public interface JmsProducer {
    void sendMessage(Message message);
}
