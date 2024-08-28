/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario.ext.exporter;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import java.io.Writer;

import com.ericsson.cifwk.meta.API;

@API(Internal)
public interface ScenarioExecutionGraphExporter {
    void init(ScenarioExecutionGraph graph);

    String getExtension();

    void export(Writer writer);
}
