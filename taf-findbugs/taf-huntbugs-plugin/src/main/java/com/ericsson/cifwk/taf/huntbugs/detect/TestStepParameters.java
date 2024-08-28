package com.ericsson.cifwk.taf.huntbugs.detect;

import com.strobel.assembler.metadata.MethodDefinition;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.MethodVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;
import one.util.huntbugs.warning.Role.StringRole;
import one.util.huntbugs.warning.WarningAnnotation;

import static com.ericsson.cifwk.taf.huntbugs.TafDetectors.isTestStepMethod;

@WarningDefinition(category = "TAF Scenario", name = "TestStepParameters", maxScore = 50)
public class TestStepParameters {

    private static final int MAXIMUM = 7;
    private static final StringRole ACTUAL_PARAMETERS = StringRole.forName("ACTUAL_PARAMETERS");
    private static final WarningAnnotation<String> EXPECTED_PARAMETERS = StringRole
            .forName("EXPECTED_PARAMETERS").createFromConst("" + MAXIMUM);

    @MethodVisitor
    public void visitMethod(MethodContext mc, MethodDefinition md) {
        if (isTestStepMethod(md)) {
            int parameterCount = md.getParameters().size();
            if (parameterCount > MAXIMUM) {

                mc.report("TestStepParameters", 20, ACTUAL_PARAMETERS.createFromConst("" + parameterCount),EXPECTED_PARAMETERS);
            }
        }
    }

}
