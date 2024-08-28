
package com.ericsson.cifwk.taf.assertions;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class TafAssertsTest {

    private Logger logger;

    @Before
    public void setUp() throws Exception {
        logger = mock(Logger.class);
        TafHamcrestAsserts.logger = logger;
    }

    @Test
    public void shouldNotFailAndLogException_when_softAssert_passedNull() throws Exception {
        TafAsserts.softAssert((Object[][]) null);
        verify(logger).error(same("Assertion failed"), (Throwable) argThat(instanceOf(AssertionError.class)));
        verifyNoMoreInteractions(logger);
    }
}