package com.ericsson.cifwk.taf.huntbugs.detect;

import com.ericsson.cifwk.taf.huntbugs.TafDetectors;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import one.util.huntbugs.registry.ClassContext;
import one.util.huntbugs.registry.anno.ClassVisitor;
import one.util.huntbugs.registry.anno.MethodVisitor;
import one.util.huntbugs.registry.anno.VisitOrder;
import one.util.huntbugs.registry.anno.WarningDefinition;
import one.util.huntbugs.util.Types;

import java.util.List;

import static com.ericsson.cifwk.taf.huntbugs.TafDetectors.isJavaObjectClass;
import static com.ericsson.cifwk.taf.huntbugs.TafDetectors.isTafTestBase;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 26.10.2016
 */
@WarningDefinition(category = "TAF Scenario", name = "TestClassHierarchy", maxScore = 50)
public class TestClassHierarchy {

    private boolean isTestClass;

    @MethodVisitor
    public void visitMethod(MethodDefinition md) {

        // detecting test class
        if (TafDetectors.isTestNgTest(md)) {
            isTestClass = true;
        }
    }

    @ClassVisitor(order = VisitOrder.AFTER)
    public void visitClass(TypeDefinition td, ClassContext cc) {

        // detecting test class
        if (TafDetectors.isTestNgTest(td)) {
            isTestClass = true;
        }

        // interested in test classes only
        if (!isTestClass) {
            return;
        }

        // checking if test has any super class but TafTestBase or Object
        List<TypeReference> baseTypes = Types.getBaseTypes(td);
        baseTypes.remove(td);
        for (TypeReference baseType : baseTypes) {
            if (!isTafTestBase(baseType) && !isJavaObjectClass(baseType)) {
                cc.report("TestClassHierarchy", 20);
            }
        }

        isTestClass = false;
    }

}
