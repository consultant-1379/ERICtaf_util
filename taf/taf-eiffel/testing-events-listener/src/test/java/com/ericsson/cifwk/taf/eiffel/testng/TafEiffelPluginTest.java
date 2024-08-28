/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.eiffel.testng;

import com.ericsson.cifwk.taf.execution.ContextSetterTestListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TafEiffelPluginTest {

    @Spy
    private TafEiffelPlugin unit;

    @Test
    public void properListenerOrder() {
        assertTrue(TafEiffelListener.TAF_EIFFEL_LISTENER_PRIORITY > ContextSetterTestListener.CONTEXT_SETTER_PRIORITY);
    }

    @Test
    public void shouldInstantiateEiffelSendingOnlyWhenConfigured() {
        doReturn(null).when(unit).getMessageBusHost();
        unit.init();
        verify(unit, never()).initEventSender(anyString());

        doReturn("host").when(unit).getMessageBusHost();

        unit.init();
        verify(unit).initEventSender(anyString());
    }

}
