/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.spi;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.Function;

@API(Internal)
public interface ScenarioConfiguration {
    <T> T getProperty(String key, T defaultValue, Function<String, T> converter);

    Boolean getProperty(String key, Boolean defaultValue);

    Integer getProperty(String key, Integer defaultValue);

    String getProperty(String key, String defaultValue);
}
