package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.google.inject.Provider;

import javax.inject.Inject;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 02/06/2016
 */
@Operator
public class OperatorWithNonFinalInstanceFields {


    private final String goodStateField = null;

    @Inject
    public OperatorWithNonFinalInstanceFields injectedValue;

    @Inject
    public Provider<String> allowedProvider;

    public Provider<String> notAllowedProvider;

    private String badStateField;

}
