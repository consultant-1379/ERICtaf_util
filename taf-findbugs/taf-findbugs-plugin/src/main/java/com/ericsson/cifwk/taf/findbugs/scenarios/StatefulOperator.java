package com.ericsson.cifwk.taf.findbugs.scenarios;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks that operator has inject
 * /html/body
 */
public class StatefulOperator extends BytecodeScanningDetector {

    private static final String OPERATOR = "Lcom/ericsson/cifwk/taf/annotations/Operator;";
    private static final String INJECT_ANNOTATION = "inject/Inject";
    private static final String NON_INJECTED_STATE_FIELD_BUG = "NOT_INJECTED_STATE_FIELD_IN_OPERATOR";

    private final BugReporter reporter;
    private final List<BugInstance> list;

    public StatefulOperator(BugReporter reporter) {
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

        if (!field.isStatic() && !field.isFinal()) {
            // Check value is injected
            AnnotationEntry[] fieldAnnotations = field.getAnnotationEntries();
            for (AnnotationEntry fieldAnnotation : fieldAnnotations) {
                if (fieldAnnotation.getAnnotationType().contains(INJECT_ANNOTATION)) {
                    return;
                }
            }

            BugInstance bug = new BugInstance(this, NON_INJECTED_STATE_FIELD_BUG, HIGH_PRIORITY)
                    .addClass(this)
                    .addField(this);
            list.add(bug);
        }
    }
}
