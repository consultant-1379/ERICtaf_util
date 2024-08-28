package com.ericsson.cifwk.taf.findbugs.scenarios;

import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

/**
 * Checks class has @Shared annotation
 */
public class DeprecatedAnnotationOnClass extends BytecodeScanningDetector {

    private static final Set<String> DEPRECATED_ANNOTATION = getDeprecatedAnnotations();
    private static final String DEPRECATED_ANNOTATION_ON_CLASS = "DEPRECATED_ANNOTATION_ON_CLASS";

    private final BugReporter reporter;

    public DeprecatedAnnotationOnClass(BugReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void visitAfter(JavaClass obj) {
        super.visitAfter(obj);

        AnnotationEntry[] annotations = obj.getAnnotationEntries();
        for (AnnotationEntry annotation : annotations) {
            if (DEPRECATED_ANNOTATION.contains(annotation.getAnnotationType())) {
                BugInstance bug = new BugInstance(this, DEPRECATED_ANNOTATION_ON_CLASS, HIGH_PRIORITY)
                        .addClass(this);
                reporter.reportBug(bug);
            }
        }
    }

    private static Set getDeprecatedAnnotations(){
        Set<String> DEPRECATED_ANNOTATION = new HashSet<>();
        DEPRECATED_ANNOTATION.add("Lcom/ericsson/cifwk/taf/annotations/Shared;");
        DEPRECATED_ANNOTATION.add("Lcom/ericsson/cifwk/taf/annotations/VUserScoped;");

        return DEPRECATED_ANNOTATION;

    }
}
