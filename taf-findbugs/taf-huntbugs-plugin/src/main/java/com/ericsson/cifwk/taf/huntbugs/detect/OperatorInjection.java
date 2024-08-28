package com.ericsson.cifwk.taf.huntbugs.detect;

import com.ericsson.cifwk.taf.huntbugs.TafDetectors;
import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.TypeReference;
import one.util.huntbugs.registry.FieldContext;
import one.util.huntbugs.registry.anno.FieldVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;

import static com.ericsson.cifwk.taf.huntbugs.TafDetectors.isInjectable;
import static com.ericsson.cifwk.taf.huntbugs.TafDetectors.isOperator;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 21.10.2016
 */
@WarningDefinition(category = "TAF Scenario", name = "OperatorInjection", maxScore = 50)
public class OperatorInjection {

    @FieldVisitor
    public void visitField(FieldDefinition fd, FieldContext fc) {

        // interested in injected fields only
        if (!isInjectable(fd)) {
            return;
        }

        // provider should be injected instead of operator
        if (isOperator(getFieldType(fd))) {
            fc.report("OperatorInjection", 20);
        }
    }

    private TypeReference getFieldType(FieldDefinition fd) {
        return TafDetectors.tryToResolve(fd.getFieldType());
    }

}
