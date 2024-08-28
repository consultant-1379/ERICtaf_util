package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.Operator;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 24.10.2016
 */
public class TestOperatorClassHierarchy {

    public static final String WARNING = "OperatorClassHierarchy";

    @Operator
    public class CommonOperator {
    }

    public class BaseClass {
    }

    @Operator
    @AssertWarning(WARNING)
    public class CustomizedCommonOperator extends CommonOperator {
    }

    @Operator
    @AssertNoWarning(WARNING)
    public class MyOperator extends BaseClass {
    }

    @AssertNoWarning(WARNING)
    public class Subclass extends CommonOperator {
    }

    @Operator
    @AssertWarning(WARNING)
    public class SubclassOperator extends Subclass {
    }

}
