package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.Operator;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;

import javax.inject.Provider;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 24.10.2016
 */
public class TestOperatorInterfaceInjection {

    public static final String WARNING = "OperatorInterfaceInjection";

    @javax.inject.Inject
    @AssertNoWarning(WARNING)
    private Provider<MyOperator> myOperator;

    @javax.inject.Inject
    @AssertWarning(WARNING)
    private Provider<MyAnnotatedOperator> myAnnotatedOperator;

    @javax.inject.Inject
    @AssertNoWarning(WARNING)
    private Provider<?> providerOfSomething;

    @javax.inject.Inject
    @AssertNoWarning(WARNING)
    private Provider rawProvider;

    public interface MyOperator {
    }

    @Operator
    public static class MyOperatorImpl implements MyOperator {
    }

    @Operator
    public interface MyAnnotatedOperator {
    }

    public class MyAnnotatedOperatorImpl implements MyAnnotatedOperator {
    }

}
