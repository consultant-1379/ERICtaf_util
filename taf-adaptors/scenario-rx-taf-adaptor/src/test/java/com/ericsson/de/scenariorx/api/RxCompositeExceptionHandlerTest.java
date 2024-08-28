package com.ericsson.de.scenariorx.api;

import static org.apache.log4j.Logger.getLogger;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.util.Stack;

import com.ericsson.de.scenariorx.api.RxExceptionHandler.Outcome;
import org.apache.log4j.Layout;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.junit.Test;

public class RxCompositeExceptionHandlerTest {

    @Test
    public void handlers_withoutExceptions_allExecuted() throws Exception {
        Stack<String> stack = new Stack<>();

        RxCompositeExceptionHandler handler = TafRxScenarios.compositeExceptionHandler()
                .addExceptionHandler(new PushStackHandler(1, stack))
                .addExceptionHandler(new PushStackHandler(2, stack))
                .addExceptionHandler(new PushStackHandler(3, stack))
                .build();

        handler.onException(new RuntimeException("foo"));

        assertThat(stack).containsExactly(
                "Handler 1 got exception: foo",
                "Handler 2 got exception: foo",
                "Handler 3 got exception: foo"
        );
    }

    @Test
    public void handlers_throwingExceptions_allLogged() throws Exception {
        StringWriter logOutput = new StringWriter();
        WriterAppender appender = new WriterAppender(new SimpleLayout(), logOutput);
        getLogger(RxCompositeExceptionHandler.class).addAppender(appender);

        RxCompositeExceptionHandler handler = TafRxScenarios.compositeExceptionHandler()
                .addExceptionHandler(new ThrowingHandler("foo"))
                .addExceptionHandler(new ThrowingHandler("bar"))
                .addExceptionHandler(new ThrowingHandler("baz"))
                .build();

        handler.onException(new RuntimeException());

        assertThat(logOutput.toString().split(Layout.LINE_SEP)).containsExactly(
                "INFO - com.ericsson.de.scenariorx.api.RxCompositeExceptionHandlerTest$ThrowingHandler has thrown java.lang.RuntimeException: foo",
                "INFO - com.ericsson.de.scenariorx.api.RxCompositeExceptionHandlerTest$ThrowingHandler has thrown java.lang.RuntimeException: bar",
                "INFO - com.ericsson.de.scenariorx.api.RxCompositeExceptionHandlerTest$ThrowingHandler has thrown java.lang.RuntimeException: baz"
        );
        getLogger(RxCompositeExceptionHandler.class).removeAppender(appender);
    }

    @Test
    public void finalHandler_outcomeByDefault_propagateException() throws Exception {
        RxCompositeExceptionHandler handler = TafRxScenarios.compositeExceptionHandler().build();

        Outcome outcome = handler.onException(new RuntimeException());

        assertThat(outcome).isSameAs(Outcome.PROPAGATE_EXCEPTION);
    }

    @Test
    public void finalHandler_outcomeFromFinalHandler() throws Exception {
        RxCompositeExceptionHandler handler = TafRxScenarios.compositeExceptionHandler()
                .setFinalExceptionHandler(RxExceptionHandler.IGNORE)
                .build();

        Outcome outcome = handler.onException(new RuntimeException());

        assertThat(outcome).isSameAs(Outcome.CONTINUE_FLOW);
    }

    @Test(expected = RuntimeException.class)
    public void finalHandler_exceptionReThrown() throws Exception {
        RxCompositeExceptionHandler handler = TafRxScenarios.compositeExceptionHandler()
                .setFinalExceptionHandler(new ThrowingHandler(""))
                .build();

        handler.onException(new RuntimeException());
    }

    private static final class PushStackHandler extends RxExceptionHandler {

        private final int id;
        private final Stack<String> stack;

        private PushStackHandler(int id, Stack<String> stack) {
            this.id = id;
            this.stack = stack;
        }

        @Override
        public Outcome onException(Throwable e) {
            stack.push("Handler " + id + " got exception: " + e.getMessage());
            return Outcome.CONTINUE_FLOW;
        }
    }

    private static final class ThrowingHandler extends RxExceptionHandler {

        private final String message;

        private ThrowingHandler(String message) {
            this.message = message;
        }

        @Override
        public Outcome onException(Throwable e) {
            throw new RuntimeException(message);
        }
    }
}