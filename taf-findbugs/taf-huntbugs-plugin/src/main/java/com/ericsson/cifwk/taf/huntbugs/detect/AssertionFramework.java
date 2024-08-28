package com.ericsson.cifwk.taf.huntbugs.detect;

import com.ericsson.cifwk.taf.huntbugs.Expressions;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.decompiler.ast.Expression;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.AstNodes;
import one.util.huntbugs.registry.anno.AstVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 31.10.2016
 */
@WarningDefinition(category = "TAF General", name = "AssertionFramework", maxScore = 50)
public class AssertionFramework {

    private static final String FORBIDDEN_CLASS_1 = "com.ericsson.cifwk.taf.assertions.SaveAsserts";

    private static final String FORBIDDEN_CLASS_2 = "com.ericsson.cifwk.taf.assertions.TafAsserts";

    private static final String FORBIDDEN_CLASS_3 = "org.testng.Assert";

    private static final String FORBIDDEN_CLASS_4 = "org.junit.Assert";

    @AstVisitor(nodes = AstNodes.EXPRESSIONS)
    public boolean visitMethod(Expression expression, MethodContext mc) {

        // interested in method calls only
        if (!Expressions.isMethodCall(expression)) {
            return true;
        }

        // checking usage of TAF assertions
        MethodReference methodReference = Expressions.toMethod(expression);
        String type = methodReference.getDeclaringType().getFullName();
        if (FORBIDDEN_CLASS_1.equals(type) || FORBIDDEN_CLASS_2.equals(type)) {
            mc.report("AssertionFramework", 30);
            return false;
        }

        // checking usage of TestNG or JUnit assertions
        if (FORBIDDEN_CLASS_3.equals(type) || FORBIDDEN_CLASS_4.equals(type)) {
            mc.report("AssertionFramework", 10);
            return false;
        }

        return true;
    }

}
