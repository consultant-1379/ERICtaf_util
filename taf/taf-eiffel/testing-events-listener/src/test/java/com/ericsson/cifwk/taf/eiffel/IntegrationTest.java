package com.ericsson.cifwk.taf.eiffel;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.sending.MessageSender;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.TestNG;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class IntegrationTest {

    @org.junit.Test
    public void shouldSendEvents() throws Exception {
        runTestNg(false);
    }

    private void runTestNg(boolean parallel) throws Exception {
        DataHandler.setAttribute(EiffelAdapter.MB_HOST, ""); // for plugin init
        TestNG testNG = new TestNG(false);
        if (parallel) {
            testNG.setThreadCount(3);
            testNG.setParallel("true");
        }
        testNG.setTestClasses(new Class[]{SampleTest.class});
        MessageSender sender = mock(MessageSender.class);
        when(sender.send(any(EiffelMessage.class))).then(new Answer<EiffelMessage>() {
            @Override
            public EiffelMessage answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                return (EiffelMessage) arguments[0];
            }
        });
        EiffelAdapter.senderForTests = sender;

        testNG.run();

        // on start + on finish + ((1 test * 1 contexts + 1 test) * (on test start + on test finished)) = 8
        int numberOfEvents = 6;
        verify(sender, times(numberOfEvents)).send(any(EiffelMessage.class));
    }

    @org.testng.annotations.Test(enabled = false)
    public static class SampleTest {

        @org.testng.annotations.Test
        public void passingTest() {
        }

        @org.testng.annotations.Test
        public void failingTest() {
            throw new RuntimeException();
        }

    }


}
