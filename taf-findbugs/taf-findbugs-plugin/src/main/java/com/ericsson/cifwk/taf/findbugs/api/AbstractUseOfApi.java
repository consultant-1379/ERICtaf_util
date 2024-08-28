package com.ericsson.cifwk.taf.findbugs.api;

import java.util.Set;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.ba.SignatureConverter;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * Checks use of classes annotated with @API(Internal)
 */
abstract class AbstractUseOfApi extends BytecodeScanningDetector {

    private final BugReporter reporter;

    AbstractUseOfApi(BugReporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void sawOpcode(int seen) {
        Set<String> dangerousClasses = getDangerousClasses();
        String classId;
        try {
            classId = getDottedClassConstantOperand();
        } catch (IllegalStateException e) {
            // class is not available - exiting
            return;
        }

        // checking class usage
        if (dangerousClasses.contains(classId)) {
            reportBug();
        }

        //checking if Method
        if (!isMethodCall()) {
            return;
        }

        String method;
        try {
            MethodDescriptor methodDescriptor = getMethodDescriptorOperand();
            method = SignatureConverter.convertMethodSignature(methodDescriptor);
        } catch (IllegalStateException | IllegalArgumentException e) {
            // class is not available - exiting
            return;
        }

        // checking method usage
        Set<String> dangerousMethods = getDangerousMethods();
        if (dangerousMethods.contains(method)) {
            reportBug();
        }
    }

    private void reportBug() {
        reporter.reportBug(new BugInstance(this, getBugType(), getPriority())
                .addClassAndMethod(this)
                .addSourceLine(this));
    }

    protected abstract int getPriority();

    protected abstract String getBugType();

    protected abstract Set<String> getDangerousClasses();

    protected abstract Set<String> getDangerousMethods();

}
