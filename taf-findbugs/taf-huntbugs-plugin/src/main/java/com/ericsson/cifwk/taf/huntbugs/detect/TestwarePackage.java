package com.ericsson.cifwk.taf.huntbugs.detect;

import com.google.common.annotations.VisibleForTesting;
import com.strobel.assembler.metadata.TypeDefinition;
import one.util.huntbugs.registry.ClassContext;
import one.util.huntbugs.registry.anno.ClassVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;

import java.util.Arrays;
import java.util.List;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 25.10.2016
 */
@WarningDefinition(category = "TAF Scenario", name = "TestwarePackage", maxScore = 50)
public class TestwarePackage {

    protected static List<String> forbiddenPackages = Arrays.asList(
            "flows","operators", "teststeps", "steps", "viewmodels", "models",
            "flow", "operator", "teststep", "step", "viewmodel", "model");

    @ClassVisitor
    public void visit(TypeDefinition td, ClassContext cc) {
        List<String> packageName = Arrays.asList(td.getPackageName().split("\\."));
        for (String forbiddenPackage : forbiddenPackages) {
            if (isForbidden(packageName, forbiddenPackage)) {
                cc.report("TestwarePackage", 20);
                return;
            }
        }
    }

    @VisibleForTesting
    static boolean isForbidden(List<String> packageName, String forbiddenPart) {

        // not interested
        if (!packageName.contains(forbiddenPart)) {
            return false;
        }

        // checking if last package name item is exactly the same as forbidden one
        // E.g. "fm.flows" is allowed, "flows.fm" is forbidden,
        return !packageName.get(packageName.size() - 1).equals(forbiddenPart);
    }

}
