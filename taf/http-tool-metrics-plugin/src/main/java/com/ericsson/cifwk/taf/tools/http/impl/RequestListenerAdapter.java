package com.ericsson.cifwk.taf.tools.http.impl;

import com.ericsson.cifwk.taf.performance.metric.TestMetricsWriter;
import com.ericsson.cifwk.taf.tools.http.HttpToolListener;
import com.ericsson.cifwk.taf.tools.http.RequestEvent;

public class RequestListenerAdapter implements HttpToolListener {

    private TestMetricsWriter metrics;

    public RequestListenerAdapter(TestMetricsWriter metrics) {
        this.metrics = metrics;
    }

    @Override
    public void onRequest(RequestEvent event) {
        metrics.update(event.getResponseTimeMillis(), event.getOperationResult().toString());
    }

}
