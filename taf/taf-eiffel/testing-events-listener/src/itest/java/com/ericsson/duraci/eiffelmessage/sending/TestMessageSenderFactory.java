package com.ericsson.duraci.eiffelmessage.sending;

import com.ericsson.duraci.configuration.EiffelConfiguration;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.sending.exceptions.EiffelMessageSenderException;
import com.ericsson.duraci.logging.EiffelLog;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class TestMessageSenderFactory extends MessageSender.Factory {

    // We have to take it from parent and keep here, to make it open for stubbing - as it's final in the original class
    private MessageSendWrapper wrapper;
    // Keep a copy here instead of retrieving this from parent
    private EiffelConfiguration configuration;

    private List<EiffelMessage> messagesSent = Lists.newArrayList();


    public TestMessageSenderFactory(EiffelLog log, EiffelConfiguration configuration) {
        super(log, configuration);
        this.wrapper = getParentSendWrapper();
        wrapper = spy(wrapper);
        doReturn(mock(ChannelWrapper.class, Mockito.RETURNS_DEEP_STUBS)).when(wrapper).getChannelWrapper();
        this.configuration = configuration;
    }

    private MessageSendWrapper getParentSendWrapper() {
        try {
            Field sendWrapper = this.getClass().getSuperclass().getDeclaredField("sendWrapper");
            sendWrapper.setAccessible(true);
            return (MessageSendWrapper) sendWrapper.get(this);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public MessageSendWrapper getSendWrapperStub() {
        return wrapper;
    }

    public void setSendWrapper(MessageSendWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public List<EiffelMessage> getMessagesSent() {
        return messagesSent;
    }

    @Override
    public MessageSender create() {
        return new MessageSender(configuration, wrapper) {
            @Override
            public EiffelMessage send(EiffelMessage message) throws EiffelMessageSenderException {
                EiffelMessage sentMessage = super.send(message);
                messagesSent.add(sentMessage);
                return sentMessage;
            }
        };
    }

}
