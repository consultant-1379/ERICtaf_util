package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.api.ExtendedScenarioListener;
import com.ericsson.cifwk.taf.scenario.api.TestFlowResult;
import com.ericsson.cifwk.taf.scenario.api.TestStepResult;
import com.ericsson.cifwk.taf.scenario.impl.exception.ScenarioListenerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Allows to call multiple listeners and wraps their exceptions into ScenarioListenerException
 */
@API(Internal)
public class CompositeScenarioListener extends ExtendedScenarioListener {

    private final List<ScenarioListenerSortableWrapper> listeners = new ArrayList<>();

    public void add(List<ScenarioListenerSortableWrapper> listeners) {
        this.listeners.addAll(listeners);
        Collections.sort(this.listeners, new ScenarioListenerSortableWrapperComparator());
    }

    @Override
    public void onScenarioStarted(TestScenario scenario) {
        try {
            for (ScenarioListenerSortableWrapper listener : listeners) {
                listener.getScenarioListener().onScenarioStarted(scenario);
            }
        } catch (Throwable e) {
            throw new ScenarioListenerException(e);
        }
    }

    @Override
    public void onScenarioFinished(TestScenario scenario) {
        try {
            for (ScenarioListenerSortableWrapper listener : listeners) {
                listener.getScenarioListener().onScenarioFinished(scenario);
            }
        } catch (Throwable e) {
            throw new ScenarioListenerException(e);
        }
    }

    @Override
    public void onFlowStarted(TestStepFlow flow) {
        try {
            for (ScenarioListenerSortableWrapper listener : listeners) {
                listener.getScenarioListener().onFlowStarted(flow);
            }
        } catch (Throwable e) {
            throw new ScenarioListenerException(e);
        }
    }

    @Override
    public void onFlowFinished(TestStepFlow flow, TestFlowResult result) {
        try {
            for (ScenarioListenerSortableWrapper listener : listeners) {
                if (listener.getScenarioListener() instanceof ExtendedScenarioListener) {
                    ExtendedScenarioListener.class.cast(listener.getScenarioListener()).onFlowFinished(flow, result);
                } else {
                    listener.getScenarioListener().onFlowFinished(flow);
                }
            }
        } catch (Throwable e) {
            throw new ScenarioListenerException(e);
        }
    }

    @Override
    public void onTestStepStarted(TestStepInvocation invocation, Object[] args) {
        try {
            for (ScenarioListenerSortableWrapper listener : listeners) {
                listener.getScenarioListener().onTestStepStarted(invocation, args);
            }
        } catch (Throwable e) {
            throw new ScenarioListenerException(e);
        }
    }

    @Override
    public void onTestStepFinished(TestStepInvocation invocation, TestStepResult result) {
        try {
            for (ScenarioListenerSortableWrapper listener : listeners) {
                if (listener.getScenarioListener() instanceof ExtendedScenarioListener) {
                    ExtendedScenarioListener.class.cast(listener.getScenarioListener()).onTestStepFinished(invocation, result);
                } else {
                    listener.getScenarioListener().onTestStepFinished(invocation);
                }
            }
        } catch (Throwable e) {
            throw new ScenarioListenerException(e);
        }
    }
}
