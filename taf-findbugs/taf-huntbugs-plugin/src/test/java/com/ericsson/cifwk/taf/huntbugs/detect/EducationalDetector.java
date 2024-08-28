package com.ericsson.cifwk.taf.huntbugs.detect;

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.MethodBody;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.VariableDefinition;
import com.strobel.decompiler.ast.Block;
import com.strobel.decompiler.ast.Expression;
import com.strobel.decompiler.ast.Node;
import one.util.huntbugs.db.DeclaredAnnotations;
import one.util.huntbugs.db.FieldStats;
import one.util.huntbugs.db.Hierarchy;
import one.util.huntbugs.db.MethodStats;
import one.util.huntbugs.db.Mutability;
import one.util.huntbugs.registry.ClassContext;
import one.util.huntbugs.registry.FieldContext;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.AstNodes;
import one.util.huntbugs.registry.anno.AstVisitor;
import one.util.huntbugs.registry.anno.ClassVisitor;
import one.util.huntbugs.registry.anno.FieldVisitor;
import one.util.huntbugs.registry.anno.MethodVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;
import one.util.huntbugs.util.NodeChain;

/**
 * This detector is used to overview HuntBugs callback data.
 * Used for your own detector development.
 */
@WarningDefinition(category = "TAF UI", name = "EducationalDetector", maxScore = 50)
public class EducationalDetector {

    @ClassVisitor
    public boolean visitClass(TypeDefinition td, ClassContext cc,
                              DeclaredAnnotations anno,
                              FieldStats fielsStats,
                              FieldStats.TypeFieldStats typeFieldStats,
                              MethodStats methodStats,
                              Hierarchy hierarchy,
                              Hierarchy.TypeHierarchy typeHierarchy,
                              Mutability mutability) {
//        System.out.println(format("Type: %s, \t\t", td.getFullName()));
        return true;
    }

    @FieldVisitor
    public void visitFields(FieldDefinition fd, FieldContext fc, TypeDefinition td) {
//        System.out.println(format("Field: %s", fd.getName()));
    }

    @MethodVisitor
    public boolean visitMethods(MethodContext mc, MethodDefinition md, TypeDefinition td) {
        MethodBody body = md.getBody();
//        System.out.println(format("Method: %s\nInstructions: %s\nVariables: %s\n\n",
//                md.getFullName(), body.getInstructions(), body.getVariables()));
//        System.out.println(format("Method: %s", md.getFullName()));
        for (VariableDefinition variable : body.getVariables()) {
//            System.out.println(String.format("Variable: %s (%s)", variable.getName(), variable.getVariableType()));
        }
//        System.out.println();
        return true;
    }

    @AstVisitor(nodes = AstNodes.ROOT)
    public void visitMethodBlock(Block block, MethodContext mc, MethodDefinition md, TypeDefinition td) {
//        System.out.println(format("Block class: %s, \t\tblock: %s", block.getClass(), block));
    }

    @AstVisitor(nodes = AstNodes.EXPRESSIONS)
    public boolean visitExpressions(Expression expression, NodeChain nodeChain, MethodContext mc, MethodDefinition md, TypeDefinition td) {
        Object operand = expression.getOperand();
        Class<?> operandClass = operand == null ? null : operand.getClass();
//        System.out.println(format("Expression class: %s\t\texpression: %s\t\t" +
//                        "Operand class:%s\t\tOperand:%s",
//                expression.getClass(), expression, operandClass, operand));
        return true;
    }

    @AstVisitor(nodes = AstNodes.ALL)
    public boolean visitAllNodes(Node node, NodeChain nodeChain, MethodContext mc, MethodDefinition md, TypeDefinition td) {
//        System.out.println(format("Node class: %s, \t\tnode: %s", node.getClass(), node));
        return true;
    }



}
