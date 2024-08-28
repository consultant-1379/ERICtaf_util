package com.ericsson.cifwk.taf.findbugs.ui;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import org.apache.bcel.classfile.Method;

/**
 * Checks use of UI.pause
 */
public class UseOfUiPause extends BytecodeScanningDetector {

    private static final String BUG_TYPE = "UI_PAUSE";

    private final BugReporter reporter;

    private boolean reportedThisMethod;

    public UseOfUiPause(BugReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void visit(Method obj) {
        reportedThisMethod = false;
        super.visit(obj);
    }

    @Override
    public void sawOpcode(int seen) {
        if (reportedThisMethod) {
            return;
        }

        if (seen == INVOKESTATIC
                && getClassConstantOperand().startsWith("com/ericsson/cifwk/taf/ui/UI")
                && getNameConstantOperand().equals("pause")) {

            BugInstance bug = new BugInstance(this, BUG_TYPE, HIGH_PRIORITY)
                    .addClassAndMethod(this)
                    .addSourceLine(this);
            reporter.reportBug(bug);

            reportedThisMethod = true;
        }
    }
}
