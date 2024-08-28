package com.ericsson.cifwk.taf.findbugs.testng;

import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

/**
 * Checks for tests which order their execution by using the testng dependsOn parameters
 */
public class UsingDependsOnToOrderTests extends BytecodeScanningDetector {

    private static final String BUG_TYPE = "DEPENDS_ON_IN_TESTNG_TEST";

    private BugReporter bugReporter;

    public UsingDependsOnToOrderTests(final BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void doVisitMethod(Method method){
        super.doVisitMethod(method);

        if(TestNgUtilities.isTestNgTest(method) && TestNgUtilities.isDependsOnPresent(method)){
            BugInstance bug = new BugInstance(this, BUG_TYPE, NORMAL_PRIORITY)
                    .addClass(this)
                    .addMethod(getThisClass(), method);
            bugReporter.reportBug(bug);
        }
    }
}
