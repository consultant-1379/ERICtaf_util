package com.ericsson.cifwk.taf.huntbugs.detect;

import com.ericsson.cifwk.taf.huntbugs.Expressions;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.decompiler.ast.Expression;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.AstNodes;
import one.util.huntbugs.registry.anno.AstVisitor;
import one.util.huntbugs.registry.anno.ClassVisitor;
import one.util.huntbugs.registry.anno.VisitOrder;
import one.util.huntbugs.registry.anno.WarningDefinition;

import static com.ericsson.cifwk.taf.huntbugs.TafDetectors.isOperator;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 02.11.2016
 */
@WarningDefinition(category = "TAF Scenario", name = "AssertionInOperator", maxScore = 50)
public class AssertionInOperator {

    public static final String ASSERTION_CLASS_1 = "org.assertj.core.api.Assertions";

    public static final String ASSERTION_CLASS_2 = "com.google.common.truth.Truth";

    public static final String ASSERTION_CLASS_3 = "org.hamcrest.MatcherAssert";

    private boolean isOperator;

    @ClassVisitor(order = VisitOrder.BEFORE)
    public void visitClass(TypeDefinition td) {
        isOperator = isOperator(td);
    }

    @AstVisitor(nodes = AstNodes.EXPRESSIONS)
    public boolean visitMethod(Expression expression, MethodContext mc) {

        // interested in @Operator class only
        if (!isOperator) {
            return false;
        }

        // interested in method calls only
        if (!Expressions.isMethodCall(expression)) {
            return true;
        }

        // checking usage of TAF assertions
        MethodReference methodReference = Expressions.toMethod(expression);
        String type = methodReference.getDeclaringType().getFullName();
        if (ASSERTION_CLASS_1.equals(type) || ASSERTION_CLASS_2.equals(type) || ASSERTION_CLASS_3.equals(type)) {
            mc.report("AssertionInOperator", 30);
            return false;
        }

        return true;
    }

}
