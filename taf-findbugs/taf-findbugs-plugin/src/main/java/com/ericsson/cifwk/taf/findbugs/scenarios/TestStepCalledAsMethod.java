package com.ericsson.cifwk.taf.findbugs.scenarios;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.ba.XMethod;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.DescriptorFactory;
import edu.umd.cs.findbugs.classfile.analysis.AnnotatedObject;

public class TestStepCalledAsMethod extends BytecodeScanningDetector {
    private static final ClassDescriptor TESTSTEP_CALLED_AS_JAVA_METHOD =
            DescriptorFactory.createClassDescriptorFromSignature("Lcom/ericsson/cifwk/taf/annotations/TestStep;");
    private  static final String BUG = "TESTSTEP_CALLED_AS_JAVA_METHOD";

    private final BugReporter bugReporter;

    public TestStepCalledAsMethod(final BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void sawMethod() {
        checkMethod();
    }

    private void checkMethod() {
        final XMethod invokedMethod = getXMethodOperand();
        if (isAnnotated(invokedMethod)) {

            bugReporter.reportBug(new BugInstance(this, BUG, HIGH_PRIORITY)
                    .addClassAndMethod(this)
                    .addSourceLine(this));
        }
    }

    private boolean isAnnotated(final AnnotatedObject annotatedObject) {
        return annotatedObject != null && annotatedObject.getAnnotation(TESTSTEP_CALLED_AS_JAVA_METHOD) != null;
    }
}
