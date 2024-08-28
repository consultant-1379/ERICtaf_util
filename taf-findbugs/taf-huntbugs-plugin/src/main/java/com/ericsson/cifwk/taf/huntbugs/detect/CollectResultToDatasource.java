package com.ericsson.cifwk.taf.huntbugs.detect;

import com.ericsson.cifwk.taf.huntbugs.Expressions;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.ParameterDefinition;
import com.strobel.decompiler.ast.Expression;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.AstNodes;
import one.util.huntbugs.registry.anno.AstVisitor;
import one.util.huntbugs.registry.anno.MethodVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;

import java.util.List;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 01.11.2016
 */
@WarningDefinition(category = "TAF Scenario", name = "CollectResultToDatasource", maxScore = 50)
public class CollectResultToDatasource {

    private static final String TEST_STEP_DEFINITION = "com.ericsson.cifwk.taf.scenario.api.TestStepDefinition";

    private static final String METHOD_NAME = "collectResultToDatasource";

    private List<ParameterDefinition> containerMethodParameters;

    @MethodVisitor
    public void visitMethod(MethodDefinition md) {
        containerMethodParameters = md.getParameters();
    }

    @AstVisitor(nodes = AstNodes.EXPRESSIONS)
    public boolean visitMethod(Expression expression, MethodContext mc) {

        // interested in method calls only
        if (!Expressions.isMethodCall(expression)) {
            return true;
        }

        // interested in TestStepDefinition class only
        MethodReference method = Expressions.toMethod(expression);
        String type = method.getDeclaringType().getFullName();
        if (!TEST_STEP_DEFINITION.equals(type)) {
            return true;
        }

        // interested in collectResultToDatasource() method only
        String methodName = method.getName();
        if (!METHOD_NAME.equals(methodName)) {
            return true;
        }

        ParameterDefinition originalParameter = Expressions.getOriginalParameter(expression, 1);
        if (containerMethodParameters.contains(originalParameter)) {
            // OK
            return true;
        }

        mc.report("CollectResultToDatasource", 20);
        return false;
    }

}
