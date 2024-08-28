package com.ericsson.cifwk.taf.huntbugs.detect;

import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import one.util.huntbugs.registry.ClassContext;
import one.util.huntbugs.registry.anno.ClassVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;
import one.util.huntbugs.util.Types;

import java.util.List;

import static com.ericsson.cifwk.taf.huntbugs.TafDetectors.isOperator;

@WarningDefinition(category = "TAF Scenario", name = "OperatorClassHierarchy", maxScore = 50)
public class OperatorClassHierarchy {

    @ClassVisitor
    public void visitClass(TypeDefinition td, ClassContext cc) {

        // we are interested in operators only
        if (!isOperator(td)) {
            return;
        }

        // checking if any super class is operator
        List<TypeReference> baseTypes = Types.getBaseTypes(td);
        baseTypes.remove(td);
        for (TypeReference baseType : baseTypes) {
            if (isOperator(baseType)) {
                cc.report("OperatorClassHierarchy", 20);
            }
        }
    }

}
