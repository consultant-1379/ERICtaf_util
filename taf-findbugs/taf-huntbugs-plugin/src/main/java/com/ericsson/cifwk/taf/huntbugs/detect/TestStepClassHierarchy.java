package com.ericsson.cifwk.taf.huntbugs.detect;

import com.strobel.assembler.metadata.MethodDefinition;
import one.util.huntbugs.db.Hierarchy;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.ClassVisitor;
import one.util.huntbugs.registry.anno.MethodVisitor;
import one.util.huntbugs.registry.anno.VisitOrder;
import one.util.huntbugs.registry.anno.WarningDefinition;

import java.util.Set;

import static com.ericsson.cifwk.taf.huntbugs.TafDetectors.isTestStepMethod;

@WarningDefinition(category = "TAF Scenario", name = "TestStepClassHierarchy", maxScore = 50)
public class TestStepClassHierarchy {

    private boolean hasSuperClass = false;

    @ClassVisitor(order = VisitOrder.BEFORE)
    public void visitClass(Hierarchy.TypeHierarchy typeHierarchy) {
        hasSuperClass = false;
        Set<Hierarchy.TypeHierarchy> superClasses = typeHierarchy.getSuperClasses();
        if (superClasses.size() > 0) {
            hasSuperClass = true;
        }
    }

    @MethodVisitor
    public void visitMethod(MethodContext mc, MethodDefinition md) {
        if (isTestStepMethod(md)) {
            if (hasSuperClass) {
                mc.report("TestStepClassHierarchy", 20);
            }
        }
    }

}
