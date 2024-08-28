package com.ericsson.cifwk.taf.performance.metric;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.eventbus.Priority;
import com.ericsson.cifwk.taf.eventbus.Subscribe;
import com.ericsson.cifwk.taf.testapi.events.TestCaseEvent;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.base.Preconditions.checkNotNull;

@API(Internal)
public class TestMetricsListener {

    private static final int PRIORITY = 55;

    private final TestMetricsWriter metrics;

    public TestMetricsListener() {
        this(new TestMetricsWriter(TestMetricsWriter.TAF));
    }

    public TestMetricsListener(TestMetricsWriter metrics) {
        this.metrics = metrics;
    }

    @Subscribe
    @Priority(PRIORITY)
    public void onTestCaseEvent(TestCaseEvent event) {
        switch (event.getExecutionState()) {
            case STARTED:
                TestContext.set(event);
                break;
            case SKIPPED:
                TestContext.set(event);
                onTestFinished(OperationResult.UNKNOWN);
                break;
            case SUCCEEDED:
                onTestFinished(OperationResult.SUCCESS);
                break;
            case FAILED:
                onTestFinished(OperationResult.FAILURE);
                break;

        }
    }

    private void onTestFinished(OperationResult resultType) {

        // getting timestamps
        long stopMillis = System.currentTimeMillis();
        String errorMessage = "Please initialize TestContext before calling this method";
        long startMillis = checkNotNull(TestContext.getTestStartedMillis(), errorMessage);

        // updating metrics
        long time = stopMillis - startMillis;
        metrics.update(time, resultType.toString());
        TestContext.reset();
    }

}
