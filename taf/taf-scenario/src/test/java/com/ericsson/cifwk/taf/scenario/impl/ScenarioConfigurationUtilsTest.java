package com.ericsson.cifwk.taf.scenario.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

import static com.ericsson.cifwk.taf.scenario.impl.configuration.ScenarioConfigurationUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 26.04.2016
 */
public class ScenarioConfigurationUtilsTest {

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void isDebugScenario() throws Exception {
        assertThat(debugScenario("true")).isTrue();
        assertThat(debugScenario("false")).isFalse();
        assertThat(debugScenario("string")).isFalse();
        assertThat(debugScenario("")).isFalse();

        System.clearProperty(DEBUG_LOG);
        assertThat(shouldDebugScenario()).isFalse();
    }

    @Test
    public void isValidationStrict() throws Exception {

        // checking that strict mode is switched off by default
        assertThat(isDataSourceValidationStrict()).isFalse();

        // setting system properties
        assertThat(isStrict("")).isFalse();
        assertThat(isStrict("true")).isFalse();
        assertThat(isStrict("false")).isFalse();
        assertThat(isStrict("string")).isFalse();
        assertThat(isStrict("strict")).isTrue();

        System.clearProperty(DATASOURCE_VALIDATION);
        assertThat(isDataSourceValidationStrict()).isFalse();
    }

    private boolean debugScenario(String propertyValue) {
        System.setProperty(DEBUG_LOG, propertyValue);
        return shouldDebugScenario();
    }

    private boolean isStrict(String propertyValue) {
        System.setProperty(DATASOURCE_VALIDATION, propertyValue);
        return isDataSourceValidationStrict();
    }

}
