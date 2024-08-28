package com.ericsson.cifwk.taf.huntbugs.detect;

import com.ericsson.cifwk.taf.huntbugs.Expressions;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.decompiler.ast.Expression;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.AstNodes;
import one.util.huntbugs.registry.anno.AstVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;

import static com.ericsson.cifwk.taf.huntbugs.Expressions.toMethod;
import static com.ericsson.cifwk.taf.huntbugs.TafDetectors.tryToResolve;

/**
 * Checks use of UI.pause
 */
@WarningDefinition(category = "TAF UI", name = "UsageOfUiPause", maxScore = 50)
public class UsageOfUiPause {

    @AstVisitor(nodes = AstNodes.EXPRESSIONS)
    public boolean visitExpressions(Expression expression, MethodContext mc) {

        // interested in static method call
        if (!Expressions.isMethodCall(expression)) {
            return true;
        }

        // we are interested in UI.pause() method only
        if (!isUiPause(toMethod(expression))) {
            return true;
        }

        // reporting and exit from visiting current method again
        mc.report("UsageOfUiPause", 10);
        return false;
    }

    public boolean isUiPause(MethodReference method) {
        method = tryToResolve(method);
        String methodDeclaringType = method.getDeclaringType().getFullName();
        String methodName = method.getName();
        return "com.ericsson.cifwk.taf.ui.UI".equals(methodDeclaringType) && "pause".equals(methodName);
    }

}
