package com.ericsson.cifwk.taf.handlers

import com.ericsson.cifwk.taf.data.Host
import groovy.transform.WithWriteLock
import groovy.util.logging.Log4j

import javax.jms.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue

/**
 *
 * JmsHandler class for sending, receiving and listening for jms messages for a destination.
 * Once the handler is instantiated it starts listening for messages immediately and stores them in LinkedBlockingQueue
 */
@Log4j
class JmsHandler extends JmsQueuePoolHelper {

    /**
     * Flag set to true when listeners are being updated
     */
    public boolean listenersUpdateInProgress = false

    /**
     * Interval for checking for new messages arrival
     */
    public int checkForNewMessageInterval = 1000
    /**
     * Default timeout for receiving message
     */
    public static final int DEFAULT_WAIT_TIMEOUT = 10000

    /**
     * Time out to wait for message
     */
    public long messageWaitTimeOut


    @Lazy
    private List messageReceivedListeners = new CopyOnWriteArrayList()
    private boolean consumeMessages = false

    private BlockingQueue listenersMessagesInstance

    /**
     * Method to get queue containing messages processed by listeners
     * @return
     */
    private BlockingQueue getListenersMessages() {
        if (listenersMessagesInstance == null)
            listenersMessagesInstance = new LinkedBlockingQueue()
        return listenersMessagesInstance
    }

    /**
     * Method to synchronize messages processed by local listeners with messages put to all messages queue
     */
    private void synchronizeListeners() {
        listenersUpdateInProgress = true
        if (getListenersMessages().size() < getAllMessages().size())
            if (log.traceEnabled) log.trace "Listeners synch in progress for ${getAllMessages().size() - getListenersMessages().size()} messages ( all: ${getAllMessages().size()}, processed: ${getListenersMessages().size()})"
        getAllMessages().findAll { !getListenersMessages().contains(it) }.each {
            if (log.traceEnabled) log.trace "Message to synch $it"
            Thread.start("Notify listeners thread ", notifyListeners(it))
            getListenersMessages() << it

        }
        listenersUpdateInProgress = false
    }

    /**
     * Process message by adding it to queues and sending to listeners
     * @param m
     */
    private void processMessage(Message m) {
        listenersUpdateInProgress = true
        if (log.traceEnabled) log.trace "Processing message $m"
        if (!addMessageIfNotInQueues(getQueueKey(jbossNode, destinationName), m)) {
            if (log.traceEnabled) log.trace "Not a new message $m"
        }
        synchronizeListeners()
    }

    /**
     * Constructor of JMS handler allowing to read and send messages to JMS destinations
     * @param jbossNode - Host to connect to for looking for destination
     * @param destinationName - the name of the destination to use on the Host.
     * @param transacted - indicates whether the session is transacted or not; default = false
     * @param acknowledgementMode - indicates whether the consumer or the client will acknowledge any messages it receives; ignored if the session is transacted.; default to CLIENT_ACKNOWLEDGE
     * @param consumeMessages - consume the messages coming to the queue, so messages will not be passed to next consumer; default to false
     * @param producerOnly - set JmsHandler to producer only mode - messages will not be read from destination ,but the can be sent; dafault to false
     * @param messageWaitTimeOut - set message timeout to receive messages for a certain length of time. If messages are being lost then it is advised to increase the timeout parameter.
     */
    public JmsHandler(Host jbossNode, String destinationName, boolean transacted = false, int acknowledgmentMode = Session.CLIENT_ACKNOWLEDGE, boolean consumeMessages = false, boolean producerOnly = false, messageWaitTimeOut = DEFAULT_WAIT_TIMEOUT) throws JMSException {
        this.destinationName = destinationName
        this.jbossNode = jbossNode
        this.transacted = transacted
        this.acknowledgmentMode = acknowledgmentMode
        this.consumeMessages = consumeMessages
        this.messageWaitTimeOut = messageWaitTimeOut

        if (producerOnly) {
            startConnection()
            log.debug "Starting JmsHandler as producer. Not getting messages"
            getProducer()
        } else {
            startBackgroundListener()
            startConnection()
            drainMessages()
            log.debug "Starting JmsHandler as listener."
        }
        Runtime.getRuntime().addShutdownHook(new Thread({ this.close() }))
        if (log.debugEnabled) log.debug "Instantiated JmsHandler ${this.dump()}"
    }

    /**
     * Default JMS listener for Topics and Queues that should remove messages
     */
    private MessageListener defaultJmsListener = { Message m ->
        log.trace "Received new message ${m} in ${this}"
        processMessage(m)
    } as MessageListener

    /**
     * Listener for queue to listen without consuming messages
     */
    Closure queueListener = {
        while (keepListening) {
            try {
                drainMessages()
            } catch (Exception ignore) {
                log.warn "Exception in queue listener", ignore
            }
            Thread.sleep(checkForNewMessageInterval)
        }
    }
    /**
     * Local variable for informing synchronous listener if listening should continue.
     */
    private boolean keepListening = true

    /**
     * Method to close connection to JMS. It is strongly recommended to use it.
     */
    @Override
    public void close() {
        keepListening = false
        super.close()
    }

    /**
     * Convenience method to get messages from Queue directyly using queue browser
     * @return list of messages returned by queue browser
     */
    public List<Message> getFromQueueBrowser() {
        List result
        log.trace "Draining using queue browser"
        getSession().createBrowser(getDestination()).with { browser ->
            result = browser.getEnumeration().toList()
            browser.close()
        }
        return result
    }

    /**
     * Method for getting messages synchronously from destination using receive() method and queue browser if destination is a queue
     */
    @WithWriteLock
    protected void drainMessages() {
        if (log.traceEnabled) log.trace "Draining messages in ${this}"
        if (isQueue() && !consumeMessages) {
            getFromQueueBrowser().each { msg ->
                processMessage(msg)
            }
        } else {
            receiveAll().each { msg ->
                processMessage(msg)

            }
            log.trace "${allMessages.size()} messages after drain"
        }
    }

    /**
     * Start the message listener which will store all messages received into queue
     */
    private void startBackgroundListener() {
        try {
            if (isQueue() && !consumeMessages) {
                Thread.startDaemon queueListener
            } else {
                getConsumer().setMessageListener(defaultJmsListener)
            }
        } catch (Exception e) {
            log.debug "Could not start message Listener $e"
        }

    }

    /**
     * Filter for getting messages as per specified content
     */
    private Closure hasContent = { Object expectedContent, Message message ->
        def actualContent

        try {
            if (TextMessage.isInstance(message))
                actualContent = ((TextMessage) message).getText()
            else if (ObjectMessage.isInstance(message))
                actualContent = ((ObjectMessage) message).getObject()
            else if (BytesMessage.isInstance(message)) {
                long length = ((BytesMessage) message).getBodyLength()
                byte[] content = new byte[length]
                ((BytesMessage) message).reset()
                ((BytesMessage) message).readBytes(content)
                actualContent = content
            } else if (MapMessage.isInstance(message)) {
                actualContent = new HashMap()
                def keys = ((MapMessage) message).getMapNames()
                keys.each { key ->
                    try {
                        actualContent.putAt(key, ((MapMessage) message).getInt(key))
                    } catch (Exception e) {
                        try {
                            actualContent.putAt(key, ((MapMessage) message).getFloat(key))
                        } catch (Exception e1) {
                            try {
                                actualContent.putAt(key, ((MapMessage) message).getString(key))
                            } catch (Exception e2) {
                                actualContent.putAt(key, ((MapMessage) message).getObject(key))
                            }
                        }
                    }
                }
            } else return false
        } catch (JMSException e) {
            log.debug "Cannot compare messages in the queue due to $e"
        }
        if (log.traceEnabled) log.trace "Actual content of message $actualContent and expected content $expectedContent"
        boolean directResult = actualContent == expectedContent
        if (log.traceEnabled) log.trace "Direct result: $directResult"
        boolean dumpResult = false
        if (!directResult) {
            if (log.traceEnabled) log.trace "Looking for ${expectedContent as String} in ${actualContent.dump()}"
            dumpResult = actualContent.dump().contains(expectedContent as String)
            if (log.traceEnabled) log.trace "Dump result: $dumpResult"
        }
        return directResult || dumpResult
    }

    /**
     * Blocking Method to poll the list of received messages for a particular message or throw a JMSException when it doesn't receive the message within a time (default or specified).
     * @param messageContent - information we are searching for
     * @param timeout - the length of time to wait before timing out, default is 10 seconds
     * @return message - the wanted message received. If it doesn't find the message it throws a JMSException.
     */
    public Message getMessageFromJms(Object messageContent, Long timeOut = messageWaitTimeOut) throws JMSException {
        Message foundMessage
        Long timeLeft = timeOut
        log.debug "Looking for message with content $messageContent in ${this}"
        while (!foundMessage && timeLeft > 0) {
            foundMessage = getUndeliveredMessages().find hasContent.curry(messageContent)
            if (!foundMessage) {
                drainMessages()
                timeLeft -= checkForNewMessageInterval
                Thread.sleep(checkForNewMessageInterval)
                restartConnection()
                if (log.traceEnabled) log.trace "Restarted connection"
            }
        }
        if (log.debugEnabled) log.debug "Found message: ${foundMessage?.dump()} within ${timeOut - timeLeft}"
        if (foundMessage == null)
            throw new JMSException("Message not received before timeout")
        else {
            getUndeliveredMessages().remove(foundMessage)
            synchronizeListeners()
            log.trace "Listeners synchronization finished"
            return foundMessage
        }
    }

    /**
     * Method to use receive() method to fetch all messages from destination
     * @return
     */
    protected List<Message> receiveAll() {
        List result = []
        MessageConsumer drainer = null
        try {
            drainer = getSession().createConsumer(getDestination())

            Message m

            while ((m = drainer.receive(messageWaitTimeOut)) != null) {

                result << m
            }
        } catch (Throwable ignore) {
            log.error "Error with receiving messages ", ignore
        }
        drainer?.close()
        return result
    }

    /**
     * Listen for a particular message using MessageListener
     * @param messageContent - the content to search for.
     */
    public void listen(Object messageContent, MessageListener messageListener) {
        listen(messageContent, { m -> messageListener.onMessage(m) })
    }

    /**
     * Listen for a particular message and if found do 2 things: 1. execute the closure passed to the method 2. notify any registered listeners that the message has been received.
     * @param messageContent - the content to search for
     * @param exec - the closure to execute
     * @param timeOut - the length of time to listen for the object
     */
    public void listen(Object messageContent, Closure listener) throws JMSException {
        MessageListener wrappedListener = { Message message ->
            if (hasContent(messageContent, message)) {
                listener(message)
                log.trace "Listener got message $message with proper content"
            } else {
                log.trace "Listener got message $message with improper content"
            }
        } as MessageListener
        log.debug "Created listener"
        addListener(wrappedListener)
    }

    /**
     * Register a listener on the instance of JMSHandler. The listener must implement the OnMessage method which will be called when the listener is notified of a particular message received
     * @param listener
     */
    public void addListener(MessageListener listener) {
        log.debug "New listener registered $listener"
        messageReceivedListeners.add(listener)
    }

    /**
     * Remove a listener from the list of registered listeners
     * @param listener
     */
    public void removeListener(def listener) {
        messageReceivedListeners.remove(listener)
    }

    /**
     * Remove all listeners from the list of registered listeners
     */
    public void removeAllListeners() {
        messageReceivedListeners.clear()
    }

    /**
     * Notify any registered listeners when a particular message is received.
     * @param message
     */
    private void notifyListeners(Message message) {
        messageReceivedListeners.each {
            log.trace "Updating listener $it with ${message.dump()}"
            it.onMessage(message)
        }

    }

    /**
     * Method to send prepared message
     * @param toBeSent
     */
    @WithWriteLock
    private void sendMessage(Message toBeSent) {
        try {
            getProducer().send(toBeSent)
            log.trace "Message $toBeSent sent. ID: ${toBeSent.dump()}"
        } catch (Exception e) {
            log.error "Cannot send message", e
        }
    }

    /**
     * Method to create and send object message
     * @param messageContent
     */
    @WithWriteLock
    public void sendObjectMessage(Object messageContent) {
        try {
            ObjectMessage msg = getSession().createObjectMessage(messageContent)
            log.trace "Created object message ${msg.dump()}"
            sendMessage(msg)
        } catch (Exception e) {
            log.error "Cannot create message", e
        }
    }

    /**
     * Method to create and send text message
     * @param messageContent
     */
    @WithWriteLock
    public void sendTextMessage(String messageContent) {
        try {
            TextMessage msg = getSession().createTextMessage(messageContent)
            log.trace "Created text message ${msg.dump()}"
            sendMessage(msg)
        } catch (Exception e) {
            log.error "Cannot create message", e
        }
    }

    /**
     * Method to create and send a bytes message.
     * @param messageContent - an array of bytes which contains the message
     */
    @WithWriteLock
    public void sendBytesMessage(byte[] messageContent) {
        try {
            BytesMessage msg = getSession().createBytesMessage()
            msg.writeBytes(messageContent)
            sendMessage(msg)
        } catch (Exception e) {
            log.error "Cannot create message", e
        }
    }

    /**
     * Method to create and send a Map message.
     * @param messageContent - an array of bytes which contains the message
     */
    @WithWriteLock
    public void sendMapMessage(Map messageContent) {
        try {
            MapMessage msg = getSession().createMapMessage()
            log.debug "Message is $messageContent"
            messageContent.each { key, value ->
                log.debug "Adding $key with value $value to Map"
                if (value instanceof String) {
                    log.debug "$key with value $value is of type String"
                    msg.setString(key, value)
                } else if (value instanceof Integer) {
                    log.debug "$key with value $value is of type Integer"
                    msg.setInt(key, value)
                } else if (value instanceof Double) {
                    log.debug "$key with value $value is of type Double"
                    msg.setDouble(key, value)
                } else if (value instanceof Object) {
                    log.debug "$key with value $value is of type Object"
                    msg.setObject(key, value)
                }

            }
            sendMessage(msg)
        } catch (Exception e) {
            log.error "Cannot create message", e
        }
    }

    public List getListeners() {
        return messageReceivedListeners
    }
}
