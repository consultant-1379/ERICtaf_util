package com.ericsson.cifwk.taf.findbugs.scenarios;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Field;

/**
 * Checks for unmaintainable XPath selectors such as
 * /html/body
 */
public class RestrictedInjectAnnotation extends BytecodeScanningDetector {

    private static final String ALLOWED_INJECT_ANNOTATION = "Ljavax/inject/Inject;";
    private static final String BUG_TYPE = "WRONG_INJECT_ANNOTATION";

    private final BugReporter reporter;

    public RestrictedInjectAnnotation(BugReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void visitField(Field field) {
        super.visitField(field);

        AnnotationEntry[] annotations = field.getAnnotationEntries();
        for (AnnotationEntry annotation : annotations) {
            if (annotation.getAnnotationType().contains("Inject;") && !ALLOWED_INJECT_ANNOTATION.equals(annotation.getAnnotationType())) {
                BugInstance bug = new BugInstance(this, BUG_TYPE, HIGH_PRIORITY)
                        .addClass(this)
                        .addField(this);
                reporter.reportBug(bug);
            }
        }
    }
}
