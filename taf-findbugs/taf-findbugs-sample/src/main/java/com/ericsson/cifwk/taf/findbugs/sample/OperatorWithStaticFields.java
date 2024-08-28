package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.Operator;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 02/06/2016
 */
@Operator
public class OperatorWithStaticFields {

    public static final String GOOD_CONSTANT = "This is allowed";
    private static final Object ANOTHER_GOOD_CONSTANT = null;
    private static String BAD_CONSTANT = "This is not allowed";
    private static String badField;
}
