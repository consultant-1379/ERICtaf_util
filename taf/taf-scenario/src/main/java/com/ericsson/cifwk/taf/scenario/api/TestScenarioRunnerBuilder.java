package com.ericsson.cifwk.taf.scenario.api;


import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.impl.GenericScenarioBuilder;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioListenerSortableWrapper;
import com.ericsson.cifwk.taf.scenario.impl.TafScenarioRunner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;
import static com.ericsson.cifwk.meta.API.Quality.Stable;
import static com.google.common.base.MoreObjects.firstNonNull;

@API(Stable)
public final class TestScenarioRunnerBuilder {
    private List<ScenarioListenerSortableWrapper> listeners = Lists.newArrayList();
    private ScenarioExceptionHandler defaultHandler = null;

    @Deprecated
    private CompositeExceptionHandlerBuilder compositeExceptionHandler;

    /**
     * Set final exception handler for all Scenarios that will be started with this Runner.
     * <p>
     * This is the final exception handler to be called on exception in Test Steps, that was not handled by Flow or
     * Scenario level exception handler. Will stop Scenario execution and possibly propagate exception to the main thread
     * and fail the test.
     * <p>
     * NOTE: Unlike {@code withExceptionHandler(ExceptionHandler.IGNORE)}, {@code withDefaultExceptionHandler(ScenarioExceptionHandler.IGNORE)}
     * will not continue Scenario Flow execution. To continue execution use {@link GenericScenarioBuilder#withExceptionHandler(ScenarioExceptionHandler)}
     * <p>
     * In case you need multiple exception handlers use {@link com.ericsson.cifwk.taf.scenario.TestScenarios#compositeExceptionHandler()}
     * <pre>
     * {@code
     *.withExceptionHandler(
     *                       compositeExceptionHandler()
     *                               .addExceptionHandler(handler1)
     *                               .addExceptionHandler(handler2)
     *                               .build())
     * }
     * </pre>
     * @see TestStepFlowBuilder#withExceptionHandler(com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler)
     * @see com.ericsson.cifwk.taf.scenario.impl.GenericScenarioBuilder#withExceptionHandler(com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler)
     * @param handler
     * @return builder
     */
    public TestScenarioRunnerBuilder withDefaultExceptionHandler(ScenarioExceptionHandler handler) {
        Preconditions.checkNotNull(handler);
        Preconditions.checkState(this.defaultHandler == null, "You can not set exception handler twice. " +
                "In case you need multiple exception handlers " +
                "use {@link com.ericsson.cifwk.taf.scenario.TestScenarios#compositeExceptionHandler()");
        defaultHandler = handler;

        return this;
    }

    /**
     * Defines the final exception handler for runner
     * <p>
     * This is the final exception handler to be called and will propagate back and possibly stop execution
     *
     * @deprecated use {@link #withDefaultExceptionHandler(ScenarioExceptionHandler handler)} instead.
     */
    @Deprecated
    @API(Deprecated)
    @API.Since(2.29)
    public TestScenarioRunnerBuilder withExceptionHandler(ExceptionHandler handler) {
        Preconditions.checkNotNull(handler);
        if (compositeExceptionHandler == null) {
            compositeExceptionHandler = new CompositeExceptionHandlerBuilder(ScenarioExceptionHandler.PROPAGATE);
        }

        compositeExceptionHandler.setFinalExceptionHandler(handler);
        return this;
    }

    /**
     * Adds custom listener with default priority (0)
     */
    public TestScenarioRunnerBuilder withListener(ScenarioListener listener) {
        Preconditions.checkNotNull(listener);

        this.listeners.add(new ScenarioListenerSortableWrapper(listener, 0));
        return this;
    }

    /**
     * Adds custom listener with given priority
     *
     * @param priority Listeners with greater priority are called first (Integer.MIN..Integer.MAX_VALUE).
     */
    public TestScenarioRunnerBuilder withListener(ScenarioListener listener, int priority) {
        Preconditions.checkNotNull(listener);

        this.listeners.add(new ScenarioListenerSortableWrapper(listener, priority));
        return this;
    }

    public TestScenarioRunner build() {
        ScenarioExceptionHandler defaultHandler =  firstNonNull(this.defaultHandler, ScenarioExceptionHandler.PROPAGATE);
        ScenarioExceptionHandler compositeExceptionHandler = this.compositeExceptionHandler == null ? null : this.compositeExceptionHandler.build();
        return new TafScenarioRunner(defaultHandler, compositeExceptionHandler, listeners);
    }
}
