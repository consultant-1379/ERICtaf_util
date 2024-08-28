package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.google.inject.Provider;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 02/06/2016
 */
@Operator
public class OperatorWithWrongInject {

    @com.google.inject.Inject
    Provider<String> badInjection;
    @Inject
    Provider<String> anotherBadInjection;
    @javax.inject.Inject
    Provider<String> goodInjection;


}
