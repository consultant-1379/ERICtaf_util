package com.ericsson.de.scenariorx.api;

import static com.ericsson.de.scenariorx.impl.FlowBuilder.ERROR_EXCEPTION_HANDLER_NULL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RxCompositeExceptionHandlerBuilderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private RxCompositeExceptionHandlerBuilder builder = new RxCompositeExceptionHandlerBuilder();

    @Test
    public void addExceptionHandler_exception_whenNull() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage(ERROR_EXCEPTION_HANDLER_NULL);

        builder.addExceptionHandler(null);
    }

    @Test
    public void setFinalExceptionHandler_exception_whenNull() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage(ERROR_EXCEPTION_HANDLER_NULL);

        builder.setFinalExceptionHandler(null);
    }
}