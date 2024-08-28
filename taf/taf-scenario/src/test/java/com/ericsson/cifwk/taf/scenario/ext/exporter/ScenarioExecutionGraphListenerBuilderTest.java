/*
 * COPYRIGHT Ericsson (c) 2016.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.ext.exporter;

import com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListenerBuilder.ScenarioExecutionGraphFinalizer;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioEventBus;
import com.ericsson.cifwk.taf.scenario.impl.configuration.HashMapConfiguration;
import com.ericsson.cifwk.taf.scenario.impl.configuration.MockTafScenarioConfiguration;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Map;

import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListenerBuilder.TAF_SCENARIO_DEBUG_ENABLED;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListenerBuilder.TAF_SCENARIO_DEBUG_PORT;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListenerBuilder.TAF_SCENARIO_SHOW_SYNC_POINTS;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ScenarioExecutionGraphListenerBuilderTest {
    ScenarioEventBus eventBus = mock(ScenarioEventBus.class);

    @Test
    public void checkDefaultExporter() throws Exception {
        config(null, null, null);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                ScenarioExecutionGraphFinalizer finalizer = (ScenarioExecutionGraphFinalizer) invocation.getArguments()[0];

                assertThat(finalizer.exporters).isEmpty();
                assertThat(finalizer.onFailureExporter).isNotNull();
                assertThat(finalizer.server).isNull();

                return null;
            }
        }).when(eventBus).registerAsync(isA(ScenarioExecutionGraphFinalizer.class));

        ScenarioExecutionGraphListenerBuilder.registerIfApplicable(eventBus);

        verify(eventBus).registerAsync(isA(ScenarioExecutionGraphFinalizer.class));
        verify(eventBus).registerAsync(isA(ScenarioExecutionGraphListener.class));
        verifyNoMoreInteractions(eventBus);
    }

    @Test
    public void checkExplicitlyDisabled() throws Exception {
        config(false, null, null);

        ScenarioExecutionGraphListenerBuilder.registerIfApplicable(eventBus);

        verifyNoMoreInteractions(eventBus);
    }

    @Test
    public void checkExplicitlyDisabledOverridesOtherFields() throws Exception {
        config(false, 8080, true);

        ScenarioExecutionGraphListenerBuilder.registerIfApplicable(eventBus);

        verifyNoMoreInteractions(eventBus);
    }

    @Test
    public void checkWithServer() throws Exception {
        config(true, 8080, null);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                ScenarioExecutionGraphFinalizer finalizer = (ScenarioExecutionGraphFinalizer) invocation.getArguments()[0];

                assertThat(finalizer.exporters).hasSize(2);
                assertThat(finalizer.onFailureExporter).isNotNull();
                assertThat(finalizer.server).isNotNull();

                return null;
            }
        }).when(eventBus).registerAsync(isA(ScenarioExecutionGraphFinalizer.class));

        ScenarioExecutionGraphListenerBuilder.registerIfApplicable(eventBus);

        verify(eventBus).registerAsync(isA(ScenarioExecutionGraphFinalizer.class));
        verify(eventBus).registerAsync(isA(ScenarioExecutionGraphListener.class));
        verifyNoMoreInteractions(eventBus);
    }

    @Test
    public void checkServerRequiresDebugEnabled() throws Exception {
        config(null, 8080, null);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                ScenarioExecutionGraphFinalizer finalizer = (ScenarioExecutionGraphFinalizer) invocation.getArguments()[0];

                assertThat(finalizer.exporters).isEmpty();
                assertThat(finalizer.onFailureExporter).isNotNull();
                assertThat(finalizer.server).isNull();

                return null;
            }
        }).when(eventBus).registerAsync(isA(ScenarioExecutionGraphFinalizer.class));

        ScenarioExecutionGraphListenerBuilder.registerIfApplicable(eventBus);

        verify(eventBus).registerAsync(isA(ScenarioExecutionGraphFinalizer.class));
        verify(eventBus).registerAsync(isA(ScenarioExecutionGraphListener.class));
        verifyNoMoreInteractions(eventBus);
    }

    void config(Object tafScenarioDebugEnabled, Object tafScenarioDebugPort, Object tafScenarioShowSyncPoints) {
        final Map<String, Object> configuration = Maps.newHashMap();

        configuration.put(TAF_SCENARIO_DEBUG_ENABLED, tafScenarioDebugEnabled);
        configuration.put(TAF_SCENARIO_DEBUG_PORT, tafScenarioDebugPort);
        configuration.put(TAF_SCENARIO_SHOW_SYNC_POINTS, tafScenarioShowSyncPoints);

        HashMapConfiguration mockConfiguration = new HashMapConfiguration(configuration);
        MockTafScenarioConfiguration.setMockConfiguration(mockConfiguration);
    }
}