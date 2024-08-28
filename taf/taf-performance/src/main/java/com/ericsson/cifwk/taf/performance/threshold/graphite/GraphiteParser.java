package com.ericsson.cifwk.taf.performance.threshold.graphite;

import com.ericsson.cifwk.taf.performance.threshold.MetricSlice;

/**
 *
 */
public interface GraphiteParser {

    MetricSlice parse(String data);

}
