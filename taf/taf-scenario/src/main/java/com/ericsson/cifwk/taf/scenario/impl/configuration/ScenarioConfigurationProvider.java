/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.impl.configuration;

import static com.ericsson.cifwk.meta.API.Quality.*;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.spi.ScenarioConfiguration;

@API(Internal)
public class ScenarioConfigurationProvider {
    protected static InheritableThreadLocal<ScenarioConfiguration> tafScenarioConfiguration = new InheritableThreadLocal<ScenarioConfiguration>() {
        @Override
        protected ScenarioConfiguration initialValue() {
            return new TafScenarioConfiguration();
        }
    };

    public static ScenarioConfiguration provide() {
        return tafScenarioConfiguration.get();
    }
}
