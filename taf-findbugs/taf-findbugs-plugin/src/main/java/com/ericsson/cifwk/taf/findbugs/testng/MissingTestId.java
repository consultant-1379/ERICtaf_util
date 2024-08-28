package com.ericsson.cifwk.taf.findbugs.testng;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.Method;

/**
 * Checks for missing @TestId annotation on TestNG tests
 */
public class MissingTestId extends BytecodeScanningDetector {

    private static final String TAF_ANNOTATIONS_PACKAGE = "Lcom/ericsson/cifwk/taf/annotations/";
    private static final String TAF_TEST_ID = TAF_ANNOTATIONS_PACKAGE + "TestId;";
    private static final String BUG_TYPE = "NO_TEST_ID";
    private static final String TAF_TEST_SUITE = TAF_ANNOTATIONS_PACKAGE + "TestSuite";

    private final BugReporter reporter;

    public MissingTestId(BugReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void doVisitMethod(Method method) {
        super.doVisitMethod(method);

        if (TestNgUtilities.isTestNgTest(method) && !areTafTestAnnotationsPresent(method)) {
            BugInstance bug = new BugInstance(this, BUG_TYPE, HIGH_PRIORITY)
                    .addClass(this)
                    .addMethod(getThisClass(), method);
            reporter.reportBug(bug);
        }
    }

    private boolean areTafTestAnnotationsPresent(final Method method) {
        return isTestIdPresent(method) || isTestSuitePresent(method);
    }

    private boolean isTestSuitePresent(final Method method) {
        return getAnnotation(method, TAF_TEST_SUITE) != null;
    }

    private AnnotationEntry getAnnotation(final Method method, final String annotation){
        AnnotationEntry[] entries = method.getAnnotationEntries();
        for (AnnotationEntry entry : entries) {
            if (entry.getAnnotationType().equals(annotation)) {
                return entry;
            }

        }
        return null;
    }

    private boolean isTestIdPresent(Method method) {
        AnnotationEntry entry = getAnnotation(method, TAF_TEST_ID);
        if(entry != null ){
            ElementValuePair[] valuePairs = entry.getElementValuePairs();
            for (ElementValuePair valuePair : valuePairs) {
                if (valuePair.getNameString().equals("id")) {
                    return !valuePair.getValue().stringifyValue().trim().isEmpty();
                }
            }
        }
        return false;
    }

}
