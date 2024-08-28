package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.InvokedMethod;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Sent after the test method is invoked
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 06/09/2016
 */
@API(Internal)
public class AfterMethodInvocationEvent extends AbstractMethodInvocationEvent {

    public AfterMethodInvocationEvent(InvokedMethod invokedMethod, TestExecutionContext executionContext) {
        super(invokedMethod, executionContext);
    }

}
