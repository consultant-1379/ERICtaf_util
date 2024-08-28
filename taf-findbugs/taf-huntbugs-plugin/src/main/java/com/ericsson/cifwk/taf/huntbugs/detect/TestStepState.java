package com.ericsson.cifwk.taf.huntbugs.detect;

import com.ericsson.cifwk.taf.huntbugs.TafDetectors;
import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import one.util.huntbugs.registry.FieldContext;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.ClassVisitor;
import one.util.huntbugs.registry.anno.FieldVisitor;
import one.util.huntbugs.registry.anno.MethodVisitor;
import one.util.huntbugs.registry.anno.VisitOrder;
import one.util.huntbugs.registry.anno.WarningDefinition;

import static com.ericsson.cifwk.taf.huntbugs.TafDetectors.isInjectable;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 21.10.2016
 */
@WarningDefinition(category = "TAF Scenario", name = "TestStepState", maxScore = 50)
public class TestStepState {

    private boolean isTestStepsClass = false;

    @ClassVisitor(order = VisitOrder.AFTER)
    public void visitClass(TypeDefinition td) {
        isTestStepsClass = false;
    }

    @MethodVisitor
    public void visitMethod(MethodDefinition md, MethodContext mc) {
        if (TafDetectors.isTestStepMethod(md)) {
            isTestStepsClass = true;
        }
    }

    @FieldVisitor
    public void visitField(FieldDefinition fd, FieldContext fc) {

        // interested in test steps classes only
        if (!isTestStepsClass) {
            return;
        }

        // interested in mutable state only
        if (fd.isFinal()) {
            return;
        }

        // interested in NOT injected values only
        if (isInjectable(fd)) {
            return;
        }

        fc.report("TestStepState", 20);
    }

}
