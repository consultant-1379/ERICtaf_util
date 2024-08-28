package com.ericsson.cifwk.taf.handlers.jms;

import com.ericsson.cifwk.taf.data.Host;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@Ignore
public class JmsHandlerFactoryTest {

    JmsHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = JmsHandlerFactory.create(new Host(), "jms/queue/test"); // creates session
        handler.clear(); // clears all messages in queue
    }

    @Test(expected = JmsException.class)
    public void shouldNotConnect() throws Exception {
        handler = JmsHandlerFactory.create(new Host(), "missing"); // creates session
    }

    @Test
    public void shouldSendTextMessage() throws Exception {
        Message message = handler.textMessage("text");
        JmsProducer sender = handler.getSender();
        sender.sendMessage(message);
    }

    @Test
    public void shouldSendObjectMessage() throws Exception {
        Message message = handler.objectMessage(new Object());
        JmsProducer sender = handler.getSender();
        sender.sendMessage(message);
    }

    @Test
    public void shouldSendByteMessage() throws Exception {
        Message message = handler.byteMessage(new byte[]{});
        JmsProducer sender = handler.getSender();
        sender.sendMessage(message);
    }

    @Test
    public void shouldSendMapMessage() throws Exception {
        Message message = handler.mapMessage(new HashMap<String, String>());
        JmsProducer sender = handler.getSender();
        sender.sendMessage(message);
    }

    @Test(expected = JmsException.class, timeout = 1000)
    public void shouldExpectMessage_fail() throws Exception {
        JmsConsumer receiver = handler.getReceiver();
        receiver.expectTextMessage("OK", 500);
    }

    @Test
    public void shouldExpectTextMessage_ok() throws Exception {
        sendTextMessage("OK");

        JmsConsumer receiver = handler.getReceiver();
        receiver.expectTextMessage("OK", 500);
    }

    @Test
    public void shouldExpectTextMessage_contains() throws Exception {
        sendTextMessage("-----OK-----");

        JmsConsumer receiver = handler.getReceiver();
        TextMessage result = receiver.expectTextMessage("OK", 500);
        assertThat(result.getText(), equalTo("-----OK-----"));
    }

    @Test
    public void shouldExpectByteMessage() throws Exception {
        JmsProducer sender = handler.getSender();
        byte[] content = {1};
        Message message = handler.byteMessage(content);
        sender.sendMessage(message);

        JmsConsumer receiver = handler.getReceiver();
        receiver.expectByteMessage(content, 500);
    }

    @Test
    public void shouldExpectObjectMessage() throws Exception {
        JmsProducer sender = handler.getSender();
        Message message = handler.objectMessage(1L);
        sender.sendMessage(message);

        JmsConsumer receiver = handler.getReceiver();
        receiver.expectObjectMessage(1L, 500);
    }

    @Test
    public void shouldExpectMapMessage() throws Exception {
        JmsProducer sender = handler.getSender();
        Map<String, Integer> map = new HashMap<>();
        map.put("id1", 100);
        map.put("id2", 200);
        Message message = handler.mapMessage(map);
        sender.sendMessage(message);

        JmsConsumer receiver = handler.getReceiver();
        MapMessage result = receiver.expectMapMessage(100, 500);

        assertThat(result.getInt("id1"), equalTo(100));
        assertThat(result.getInt("id2"), equalTo(200));
    }

    @Test
    public void shouldListenToMessages() throws Exception {
        JmsConsumer receiver = handler.getReceiver();
        RecordingListener listener = new RecordingListener();
        receiver.listen(listener);

        sendTextMessage("OK");

        assertThat(listener.message.getText(), equalTo("OK"));
    }

    @Test
    public void shouldReceiveAll() throws Exception {
        sendTextMessage("1");
        sendTextMessage("1");
        sendTextMessage("1");
        sendTextMessage("1");

        JmsConsumer receiver = handler.getReceiver();
        List<Message> messages = receiver.fetchMessages(3, 1000);

        assertThat(messages.size(), equalTo(3));
        assertThat(((TextMessage) messages.get(0)).getText(), equalTo("1"));
    }

    @Test(expected = JmsException.class, timeout = 1000)
    public void shouldNotReceiveAll() throws Exception {
        sendTextMessage("1");
        sendTextMessage("1");

        JmsConsumer receiver = handler.getReceiver();
        receiver.fetchMessages(3, 500);
    }

    @After
    public void tearDown() throws Exception {
        handler.close(); // closes session
    }

    private void sendTextMessage(String text) {
        JmsProducer sender = handler.getSender();
        Message message = handler.textMessage(text);
        sender.sendMessage(message);
    }

    private static class RecordingListener implements MessageListener {

        TextMessage message;

        @Override
        public void onMessage(Message message) {
            this.message = (TextMessage) message;
        }

    }

}