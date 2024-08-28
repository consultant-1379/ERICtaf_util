package com.ericsson.cifwk.taf.findbugs.scenarios;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

/**
 * Checks class with Operator in it's name have @Operator annotation
 */
public class OperatorClassHasNoAnnotation extends BytecodeScanningDetector {

    private static final String OPERATOR_POSTFIX = "operator";
    private static final String OPERATOR_ANNOTATION = "Lcom/ericsson/cifwk/taf/annotations/Operator;";
    private static final String CLASS_HAS_NO_OPERATOR_ANNOTATION = "CLASS_HAS_NO_OPERATOR_ANNOTATION";

    private final BugReporter reporter;

    public OperatorClassHasNoAnnotation(BugReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void visitAfter(JavaClass obj) {
        super.visitAfter(obj);

        if (obj.getClass().getName().toLowerCase().endsWith(OPERATOR_POSTFIX) && !hasOperatorAnnotation(obj)) {
            BugInstance bug = new BugInstance(this, CLASS_HAS_NO_OPERATOR_ANNOTATION, HIGH_PRIORITY)
                       .addClass(this);

            reporter.reportBug(bug);
        }
    }

    private boolean hasOperatorAnnotation(final JavaClass obj) {
        AnnotationEntry[] annotations = obj.getAnnotationEntries();
        for (AnnotationEntry annotation : annotations) {
            if (OPERATOR_ANNOTATION.equals(annotation.getAnnotationType())) {
                return true;
            }
        }
        return false;
    }
}
