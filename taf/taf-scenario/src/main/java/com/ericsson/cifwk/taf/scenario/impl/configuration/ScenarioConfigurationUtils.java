/*
 * COPYRIGHT Ericsson (c) 2016.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.impl.configuration;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.spi.ScenarioConfiguration;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Class keeps Scenario internal configuration from system properties.
 *
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 26.04.2016
 */
@API(Internal)
public class ScenarioConfigurationUtils {

    public static final String DEBUG_LOG = "taf.scenario.debug.log";

    public static final String DATASOURCE_VALIDATION = "taf.scenario.data-source.validation";

    public static final String STRICT_MODE = "strict";

    public static final ScenarioConfiguration scenarioConfiguration = ScenarioConfigurationProvider.provide();

    public static boolean shouldDebugScenario() {
        return scenarioConfiguration.getProperty(DEBUG_LOG, false);
    }

    public static boolean isDataSourceValidationStrict() {
        return STRICT_MODE.equals(scenarioConfiguration.getProperty(DATASOURCE_VALIDATION, ""));
    }

    public static boolean isDataSourceAllowEmptySetGlobally() {
        return Boolean.parseBoolean(System.getProperty("allowEmptyDataSources"));
    }

}
