package com.ericsson.cifwk.taf.huntbugs.detect;

import com.ericsson.cifwk.taf.huntbugs.TafDetectors;
import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import one.util.huntbugs.registry.FieldContext;
import one.util.huntbugs.registry.anno.FieldVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;

import static com.ericsson.cifwk.taf.huntbugs.TafDetectors.isInjectable;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 21.10.2016
 */
@WarningDefinition(category = "TAF Scenario", name = "OperatorInterfaceInjection", maxScore = 50)
public class OperatorInterfaceInjection {

    @FieldVisitor
    public void visitField(FieldDefinition fd, FieldContext fc) {

        // interested in injected fields only
        if (!isInjectable(fd)) {
            return;
        }

        // interested only in Provider injections
        TypeReference fieldType = getFieldType(fd);
        if (!TafDetectors.isProvider(fieldType)) {
            return;
        }

        // interested in parametrized Provider only
        if (!fieldType.isGenericType()) {
            return;
        }

        // some types metadata is not available (e.g. provided type of maven dependency)
        TypeReference providerType = TafDetectors.getTypeArgument(fieldType);
        TypeDefinition resolvedProviderType = providerType.resolve();
        if (resolvedProviderType == null) {
            return;
        }

        // interested in operator providers only
        if (!TafDetectors.isOperator(providerType)) {
            return;
        }

        // injected operators shouldn't be interface
        if (resolvedProviderType.isInterface()) {
            fc.report("OperatorInterfaceInjection", 20);
        }
    }

    private TypeReference getFieldType(FieldDefinition fd) {
        return TafDetectors.tryToResolve(fd.getFieldType());
    }

}
