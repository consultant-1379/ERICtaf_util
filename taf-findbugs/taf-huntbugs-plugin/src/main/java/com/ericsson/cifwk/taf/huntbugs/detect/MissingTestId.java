package com.ericsson.cifwk.taf.huntbugs.detect;

import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.annotations.CustomAnnotation;
import one.util.huntbugs.registry.MethodContext;
import one.util.huntbugs.registry.anno.MethodVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;

import static com.ericsson.cifwk.taf.huntbugs.Annotations.getAnnotation;
import static com.ericsson.cifwk.taf.huntbugs.Annotations.getAnnotationConstantParameter;
import static com.ericsson.cifwk.taf.huntbugs.Annotations.hasAnnotation;
import static com.ericsson.cifwk.taf.huntbugs.TafDetectors.isTestNgTest;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 20.10.2016
 */
@WarningDefinition(category = "TAF Scenario", name = "MissingTestId", maxScore = 50)
public class MissingTestId {

    private static final String TEST_SUITE_ANNOTATION = "com.ericsson.cifwk.taf.annotations.TestSuite";
    private static final String TEST_ID_ANNOTATION = "com.ericsson.cifwk.taf.annotations.TestId";

    @MethodVisitor
    public void visitMethod(MethodDefinition md, MethodContext mc) {

        // interested in test methods only
        if (!isTestNgTest(md)) {
            return;
        }

        if (hasTafTestAnnotations(md)) {
            return;
        }
        mc.report("MissingTestId", 20);
    }

    private boolean hasTafTestAnnotations(final MethodDefinition md) {
        return hasTestId(md) || hasTestSuite(md);
    }

    private boolean hasTestSuite(final MethodDefinition md) {
        return hasAnnotation(md, TEST_SUITE_ANNOTATION);
    }

    private boolean hasTestId(MethodDefinition md) {
        if (!hasAnnotation(md, TEST_ID_ANNOTATION)) {
            return false;
        }

        CustomAnnotation annotation = getAnnotation(md, TEST_ID_ANNOTATION);
        String testId = getAnnotationConstantParameter(annotation, "id");

        if (testId == null || "".equals(testId.trim())) {
            return false;
        }
        return true;
    }

}
