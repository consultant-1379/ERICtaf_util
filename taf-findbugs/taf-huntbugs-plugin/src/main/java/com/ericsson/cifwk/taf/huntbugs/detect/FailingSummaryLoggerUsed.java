package com.ericsson.cifwk.taf.huntbugs.detect;

import com.ericsson.cifwk.taf.huntbugs.Expressions;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.decompiler.ast.Expression;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.AstNodes;
import one.util.huntbugs.registry.anno.AstVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;

import static com.ericsson.cifwk.taf.huntbugs.Expressions.getConstructor;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 31.10.2016
 */
@WarningDefinition(category = "TAF Scenario", name = "FailingSummaryLoggerUsed", maxScore = 50)
public class FailingSummaryLoggerUsed {

    private static final String FORBIDDEN_TYPE = "com.ericsson.enm.scenarios.FailingSummaryLogger";

    @AstVisitor(nodes = AstNodes.EXPRESSIONS)
    public boolean visitMethod(Expression expression, MethodContext mc) {

        // interested in object constructor methods only
        if (!Expressions.isConstructor(expression)) {
            return true;
        }

        // interested in FailingSummaryLogger class only
        MethodReference constructor = getConstructor(expression);
        String clazz = constructor.getDeclaringType().getFullName();
        if (!FORBIDDEN_TYPE.equals(clazz)) {
            return true;
        }

        mc.report("FailingSummaryLoggerUsed", 20);
        return false;
    }

}
