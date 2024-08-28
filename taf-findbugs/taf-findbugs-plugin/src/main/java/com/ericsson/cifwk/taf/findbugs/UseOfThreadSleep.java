package com.ericsson.cifwk.taf.findbugs;

import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

public class UseOfThreadSleep extends BytecodeScanningDetector {

    private static final String BUG_TYPE = "THREAD_SLEEP";

    private final BugReporter reporter;

    private boolean reportedThisMethod;

    public UseOfThreadSleep(BugReporter reporter) {
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
                && getClassConstantOperand().startsWith("java/lang/Thread")
                && getNameConstantOperand().equals("sleep")) {

            BugInstance bug = new BugInstance(this, BUG_TYPE, HIGH_PRIORITY)
                    .addClassAndMethod(this)
                    .addSourceLine(this);
            reporter.reportBug(bug);

            reportedThisMethod = true;
        }
    }
}

