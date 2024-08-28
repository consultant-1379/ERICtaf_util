package com.ericsson.cifwk.taf.huntbugs.detect;

import org.junit.Test;

import java.util.List;

import static com.ericsson.cifwk.taf.huntbugs.detect.TestwarePackage.isForbidden;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 25.10.2016
 */
public class TestwarePackageTest {

    @Test
    public void forbiddenPackagesField() {
        assertThat(TestwarePackage.forbiddenPackages).containsExactlyInAnyOrder(
                "flows","operators", "teststeps", "steps", "viewmodels", "models",
                "flow", "operator", "teststep", "step", "viewmodel", "model");
    }

    @Test
    public void isForbiddenPackage() {
        assertThat(isForbidden(toList("com.ericsson.testware.flows.fm"), "flows")).isTrue();
        assertThat(isForbidden(toList("com.ericsson.testware.fm.flows"), "flows")).isFalse();
        assertThat(isForbidden(toList("com.ericsson.testware.fm.flows"), "flow")).isFalse();
    }

    private List<String> toList(String packageName) {
        return asList(packageName.split("\\."));
    }

}