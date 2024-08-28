package com.ericsson.cifwk.taf.handlers

import groovy.transform.WithWriteLock

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

import javax.jms.Message

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.ericsson.cifwk.taf.data.Host

class JmsQueuePoolHelper extends JmsHandlerConnectionHelper {


    final protected static int ALL_MESSAGES_QUEUE = 0
    final protected static int UNDELIVERED_MESSAGES_QUEUE = 1
    private static Logger logger = LoggerFactory.getLogger(JmsQueuePoolHelper)
    protected static Map<String, List<BlockingQueue>> queuePool = new ConcurrentHashMap<String, List<BlockingQueue>>()
    /**
     * Method to construct key for the queue pool
     * @param node
     * @param destination
     * @return
     */
    protected static getQueueKey(Host node, String destination) {
        return "$node::$destination"
    }
    /**
     * Method to get message queue from queue pool
     * @param queueKey
     * @param queueType
     * @return
     */
    @WithWriteLock
    protected static BlockingQueue getQueueFromPool(String queueKey, int queueType) {
        List<BlockingQueue> result = queuePool[queueKey]
        if (result == null) {
            result = [new LinkedBlockingQueue(), new LinkedBlockingQueue()]
            queuePool[queueKey] = result
        }
        return result[queueType]
    }

    /**
     * Get all messages not received by getMessageFromJms method
     * @return
     */
    public BlockingQueue<Message> getUndeliveredMessages() {
        return getQueueFromPool(getQueueKey(jbossNode, destinationName), UNDELIVERED_MESSAGES_QUEUE)
    }

    /**
     * Get all messages fetched by JmsHandler
     * @return
     */
    public BlockingQueue<Message> getAllMessages() {
        return getQueueFromPool(getQueueKey(jbossNode, destinationName), ALL_MESSAGES_QUEUE)
    }
    /**
     * Method updating queues storing message
     * @param m
     */
    @WithWriteLock
    protected static void updateMessageQueues(String key, Message m) {
        if (logger.traceEnabled)
            logger.trace "Adding message $m to queues"
        getQueueFromPool(key, ALL_MESSAGES_QUEUE) << m
        getQueueFromPool(key, UNDELIVERED_MESSAGES_QUEUE) << m
    }

    /**
     * Compare to messages based on JMS message fields
     */
    private static Closure isSameMessage = { Message m1, Message m2 ->
        m1.getJMSTimestamp() == m2.getJMSTimestamp() &&
                m1.getJMSMessageID() == m2.getJMSMessageID() &&
                m1.getJMSDestination() == m2.getJMSDestination()
    }
    /**
     * Method to add message to the pool, checking if message is not added to the queue first
     * @param key - key of the queue in queue pool
     * @param msg - JMS message
     * @return true if queue has been updated
     */
    @WithWriteLock
    protected static boolean addMessageIfNotInQueues(String key, Message msg) {
        if (getQueueFromPool(key, ALL_MESSAGES_QUEUE).find(isSameMessage.curry(msg)) == null) {
            if (logger.traceEnabled)
                logger.trace "New message detected ${msg.dump()}"
            updateMessageQueues(key, msg)
            return true
        } else {
            return false
        }
    }
}
