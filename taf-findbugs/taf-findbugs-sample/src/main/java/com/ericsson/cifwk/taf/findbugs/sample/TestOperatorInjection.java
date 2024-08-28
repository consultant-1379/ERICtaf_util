package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.Operator;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;

import javax.inject.Provider;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 21.10.2016
 */
public class TestOperatorInjection {

    public static final String WARNING = "OperatorInjection";

    public static class MyTestSteps {

        @javax.inject.Inject
        @AssertWarning(WARNING)
        private MyOperator myOperator;

    }

    public static class ValidInjection {

        @javax.inject.Inject
        @AssertNoWarning(WARNING)
        private Provider<MyOperator> myOperator;

    }

    public static class DirectInjectionOfNonOperatorIsValid {

        @javax.inject.Inject
        @AssertNoWarning(WARNING)
        private String myOperator;

    }

    @Operator
    private static class MyOperator {
    }

}
