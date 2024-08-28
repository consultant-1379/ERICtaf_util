package com.ericsson.cifwk.taf.findbugs.ui;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.Field;

/**
 * Checks for unmaintainable XPath selectors such as
 * /html/body
 */
public class FragileXpathSelector extends BytecodeScanningDetector {

    private static final String UI_COMPONENT_MAPPNIG = "Lcom/ericsson/cifwk/taf/ui/core/UiComponentMapping;";
    private static final String BUG_TYPE = "FRAGILE_SELECTOR";

    private final BugReporter reporter;

    public FragileXpathSelector(BugReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void visit(Field field) {
        super.visit(field);

        AnnotationEntry[] annotations = field.getAnnotationEntries();
        for (AnnotationEntry annotation : annotations) {
            if (UI_COMPONENT_MAPPNIG.equals(annotation.getAnnotationType())) {
                ElementValuePair[] pairs = annotation.getElementValuePairs();
                for (ElementValuePair pair : pairs) {
                    if ("selector".equals(pair.getNameString())) {
                        if (pair.getValue().stringifyValue().startsWith("/html/body")) {
                            BugInstance bug = new BugInstance(this, BUG_TYPE, HIGH_PRIORITY)
                                    .addClass(this)
                                    .addField(this);
                            reporter.reportBug(bug);
                        }
                    }
                }
            }
        }
    }

}
