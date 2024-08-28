package com.ericsson.cifwk.taf.findbugs.testng;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.Method;

/**
 * Utility class for interrogating testng methods
 */
public final class TestNgUtilities {

    private static final String TESTNG_TEST = "Lorg/testng/annotations/Test;";

    private TestNgUtilities(){

    }

    public static boolean isTestNgTest(final Method method) {
        final AnnotationEntry[] entries = method.getAnnotationEntries();
        for (AnnotationEntry entry : entries) {
            if (entry.getAnnotationType().equals(TESTNG_TEST)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDependsOnPresent(final Method method) {
        final AnnotationEntry[] entries = method.getAnnotationEntries();
        for (AnnotationEntry entry : entries) {
            if (entry.getAnnotationType().equals(TESTNG_TEST)) {
                final ElementValuePair[] valuePairs = entry.getElementValuePairs();
                for (ElementValuePair valuePair : valuePairs) {
                    if (valuePair.getNameString().startsWith("dependsOn")) {
                        return true;
                    }
                }
            }

        }
        return false;
    }
}
