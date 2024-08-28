package com.ericsson.cifwk.taf.findbugs.scenarios;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks that operator has static non-final fields
 */
public class NonFinalStaticFieldInOperator extends BytecodeScanningDetector {

    private static final String OPERATOR = "Lcom/ericsson/cifwk/taf/annotations/Operator;";
    private static final String STATIC_FIELD_BUG = "STATIC_FIELD_IN_OPERATOR";

    private final BugReporter reporter;
    private final List<BugInstance> list;

    public NonFinalStaticFieldInOperator(BugReporter reporter) {
        this.reporter = reporter;
        list = new ArrayList<>();
    }

    @Override
    public void visitAfter(JavaClass obj) {
        super.visitAfter(obj);

        AnnotationEntry[] annotations = obj.getAnnotationEntries();
        for (AnnotationEntry annotation : annotations) {
            if (OPERATOR.equals(annotation.getAnnotationType())) {
                for (BugInstance bugInstance : list) {
                    reporter.reportBug(bugInstance);
                }
            }
        }
        list.clear();
    }

    @Override
    public void visit(Field field) {
        super.visit(field);

        if (field.isStatic() && !field.isFinal()) {
            BugInstance bug = new BugInstance(this, STATIC_FIELD_BUG, HIGH_PRIORITY)
                    .addClass(this)
                    .addField(this);
            list.add(bug);
        }
    }
}
