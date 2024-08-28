package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.execution.ContextSetterTestListener;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 17/11/2016
 */
public class AllureTafTestListenerTest {

    @Test
    public void properListenerOrder() {
        assertTrue(AllureTafTestListener.PRIORITY > ContextSetterTestListener.CONTEXT_SETTER_PRIORITY);
    }

}