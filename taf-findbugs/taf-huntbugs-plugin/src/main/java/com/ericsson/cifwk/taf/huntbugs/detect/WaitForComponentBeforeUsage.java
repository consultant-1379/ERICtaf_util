package com.ericsson.cifwk.taf.huntbugs.detect;

import com.strobel.assembler.metadata.MethodReference;
import com.strobel.decompiler.ast.Expression;
import com.strobel.decompiler.ast.Node;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.AstNodes;
import one.util.huntbugs.registry.anno.AstVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;
import one.util.huntbugs.util.NodeChain;
import one.util.huntbugs.warning.Role.MemberRole;

import java.util.List;

import static com.ericsson.cifwk.taf.huntbugs.Expressions.isMethodCall;
import static com.ericsson.cifwk.taf.huntbugs.UiDetectors.isUiComponentMethod;
import static com.ericsson.cifwk.taf.huntbugs.UiDetectors.isWaitUntil;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 26.08.2016
 */
@WarningDefinition(category = "TAF UI", name = "WaitForComponentBeforeUsage", maxScore = 50)
public class WaitForComponentBeforeUsage {

    private static final MemberRole FIRST_METHOD = MemberRole.forName("FIRST_METHOD");

    private static final MemberRole SECOND_METHOD = MemberRole.forName("SECOND_METHOD");

    @AstVisitor(nodes = AstNodes.EXPRESSIONS)
    public boolean visit(Expression expr, NodeChain nc, MethodContext mc) {

        // we are interested just in method call expressions
        if (!isMethodCall(expr)) {
            return true;
        }

        // we are interested in any method declared in (custom) UI component
        MethodReference uiAction = (MethodReference) expr.getOperand();
        if (!isUiComponentMethod(uiAction)) {
            return true;
        }

        // searching for previous expression
        MethodReference waitMethod = getPreviousMethodCall(nc, expr);

        // we are interested in a set of UI component wait methods
        if (isWaitUntil(waitMethod)) {
            mc.report("WaitForComponentBeforeUsage", 10, expr,
                    FIRST_METHOD.create(waitMethod), SECOND_METHOD.create(uiAction));
            return false;
        }

        return true;
    }

    private MethodReference getPreviousMethodCall(NodeChain nc, Expression expr) {
        List<Node> children = nc.getNode().getChildren();
        int clickExprIndex = children.indexOf(expr);
        if (clickExprIndex < 1) {
            return null;
        }
        Node waitExpressionCandidate = children.get(clickExprIndex - 1);
        if (!(waitExpressionCandidate instanceof Expression)) {
            return null;
        }
        Expression waitExpression = (Expression) waitExpressionCandidate;
        if (!isMethodCall(waitExpression)) {
            return null;
        }
        return (MethodReference) waitExpression.getOperand();
    }

}
