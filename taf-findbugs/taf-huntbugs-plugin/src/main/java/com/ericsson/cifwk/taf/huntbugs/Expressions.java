package com.ericsson.cifwk.taf.huntbugs;

import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.ParameterDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.ast.AstCode;
import com.strobel.decompiler.ast.Expression;
import com.strobel.decompiler.ast.Variable;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 24.10.2016
 */
public class Expressions {

    public static boolean isConstructor(Expression expression) {
        AstCode code = expression.getCode();
        return AstCode.InitObject == code;
    }

    public static MethodReference getConstructor(Expression expression) {
        AstCode code = expression.getCode();

        // interested in NEW instructions only
        if (!isConstructor(expression)) {
            throw new RuntimeException("Given expression has wrong code: " + code);
        }

        return (MethodReference) expression.getOperand();
    }

    public static boolean isMethodCall(Expression expr) {
        AstCode code = expr.getCode();
        return code == AstCode.InvokeVirtual || code == AstCode.InvokeInterface ||
                code == AstCode.InvokeStatic || code == AstCode.InvokeSpecial;
    }

    public static MethodReference toMethod(Expression expression) {

        AstCode code = expression.getCode();

        // interested in method call expression
        if (!isMethodCall(expression)) {
            throw new RuntimeException("Given expression has wrong code: " + code);
        }

        Object operand = expression.getOperand();
        if (!(operand instanceof MethodReference)) {
            throw new RuntimeException("Method is not resolved: " + operand);
        }
        return (MethodReference) operand;
    }

    public static TypeReference toMethodOwner(Expression expression) {

        AstCode code = expression.getCode();

        // interested in static method call
        if (!isMethodCall(expression)) {
            throw new RuntimeException("Given expression has wrong code: " + code);
        }

        Expression methodOwner = expression.getArguments().get(0);
        Variable variable = (Variable) methodOwner.getOperand();
        return variable.getType();
    }

    /**
     * This method is useful to check if parameter for specific API was provided as current method parameter.
     *
     * E.g. you have a method:
     * <pre>
     *     public void doSomething(String originalParameter) {
     *
     *         // in this case getOriginalParameter(targetMethod, 1) will find ParameterDefinition
     *         api.targetMethod(originalParameter);
     *
     *         // in this case getOriginalParameter(targetMethod, 1) will find nothing and return null
     *         String localVariable = "not parametrized"
     *         api.targetMethod(localVariable);
     *     }
     * </pre>
     */
    public static ParameterDefinition getOriginalParameter(Expression targetMethod, int targetMethodParameter) {

        AstCode code = targetMethod.getCode();

        // interested in method call expression
        if (!isMethodCall(targetMethod)) {
            throw new RuntimeException("Given expression has wrong code: " + code);
        }

        // we know exactly how many arguments target method has
        Expression methodParameter = targetMethod.getArguments().get(targetMethodParameter);

        // checking if target method parameter came from variable (was NOT hardcoded)
        Object operand = methodParameter.getOperand();
        if (operand instanceof Variable) {
            Variable variable = (Variable) operand;

            // checking if target method parameter came from current method parameters
            return variable.getOriginalParameter();
        }

        return null;
    }

}
