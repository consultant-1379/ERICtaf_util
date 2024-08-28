package com.ericsson.cifwk.taf.huntbugs.detect;

import com.ericsson.cifwk.taf.huntbugs.Expressions;
import com.ericsson.cifwk.taf.huntbugs.TafDetectors;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.decompiler.ast.Expression;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.AstNodes;
import one.util.huntbugs.registry.anno.AstVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;
import one.util.huntbugs.util.NodeChain;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 24.10.2016
 */
@WarningDefinition(category = "TAF Scenario", name = "TestStepDirectCall", maxScore = 50)
public class TestStepDirectCall {

    @AstVisitor(nodes = AstNodes.EXPRESSIONS)
    public boolean visitAst(Expression expression, NodeChain nodeChain, MethodContext mc) {

        // interested in method call expressions only
        if (!Expressions.isMethodCall(expression)) {
            return true;
        }

        // test step method shouldn't be called directly
        MethodReference methodReference = Expressions.toMethod(expression);
        if (TafDetectors.isTestStepMethod(methodReference)) {
            mc.report("TestStepDirectCall", 20);
            return false;
        }

        return true;
    }

}
