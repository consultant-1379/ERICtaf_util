package com.ericsson.cifwk.taf.huntbugs.detect;

import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.ParameterDefinition;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.MethodVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;

import java.util.List;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 17.10.2016
 */
@WarningDefinition(category = "TAF Scenario", name = "FlowParameters", maxScore = 50)
public class FlowParameters {

    private static final String FLOW_CLASS = "com.ericsson.cifwk.taf.scenario.TestStepFlow";

    private static final String DATA_SOURCE_BUILDER_CLASS = "com.ericsson.cifwk.taf.scenario.api.TafDataSourceDefinitionBuilder";

    @MethodVisitor
    public void visitMethod(MethodContext mc, MethodDefinition md) {

        // interested in flow factory methods only
        String returnTypeName = md.getReturnType().getFullName();
        if (!FLOW_CLASS.equals(returnTypeName)) {
            return;
        }

        if (!hasDataSourceParameter(md)) {
            mc.report("FlowParameters", 20);
        }

    }

    private boolean hasDataSourceParameter(MethodDefinition md) {
        List<ParameterDefinition> parameters = md.getParameters();
        for (ParameterDefinition parameter : parameters) {
            String parameterClass = parameter.getParameterType().getFullName();
            if (DATA_SOURCE_BUILDER_CLASS.equals(parameterClass)) {
                return true;
            }
        }
        return false;
    }

}
