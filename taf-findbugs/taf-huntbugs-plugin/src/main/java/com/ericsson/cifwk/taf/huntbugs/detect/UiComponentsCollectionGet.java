package com.ericsson.cifwk.taf.huntbugs.detect;

import com.strobel.assembler.metadata.IGenericInstance;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.ParameterDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.ast.Expression;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.AstNodes;
import one.util.huntbugs.registry.anno.AstVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;

import java.util.List;

import static com.ericsson.cifwk.taf.huntbugs.Expressions.isMethodCall;
import static com.ericsson.cifwk.taf.huntbugs.UiDetectors.isUiComponent;
import static one.util.huntbugs.util.Types.isCollection;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 12.09.2016
 */
@WarningDefinition(category = "TAF UI", name = "UiComponentsCollectionGet", maxScore = 50)
public class UiComponentsCollectionGet {

    @AstVisitor(nodes = AstNodes.EXPRESSIONS)
    public boolean visit(Expression expr, MethodContext mc) {

        // we are interested only in method call expressions
        if (!isMethodCall(expr)) {
            return true;
        }

        // we are interested only in List.get(int)
        MethodReference method = (MethodReference) expr.getOperand();
        if (!isGetByIndex(method)) {
            return true;
        }

        // checking type of the list (the get(int) method is called on)
        TypeReference expectedType = expr.getArguments().get(0).getExpectedType();
        if (isCollection(expectedType) && expectedType instanceof IGenericInstance) {
            IGenericInstance genericType = (IGenericInstance) expectedType;
            TypeReference listParameter = genericType.getTypeArguments().get(0);
            if (isUiComponent(listParameter)) {
                mc.report("UiComponentsCollectionGet", 10);
                return false;
            }

        }
        return true;
    }

    private boolean isGetByIndex(MethodReference method) {
        String methodName = method.getName();
        List<ParameterDefinition> parameters = method.getParameters();
        return "get".equals(methodName) && parameters.size() == 1 && "int".equals(parameters.get(0).getParameterType().getName());
    }

}
