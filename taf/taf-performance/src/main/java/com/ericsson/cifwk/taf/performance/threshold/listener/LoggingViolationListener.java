package com.ericsson.cifwk.taf.performance.threshold.listener;

import com.ericsson.cifwk.taf.performance.threshold.ThresholdRule;
import com.ericsson.cifwk.taf.performance.threshold.ViolationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Outputs all threshold violations into log.
 */
public class LoggingViolationListener implements ViolationListener {

    private final Logger logger = LoggerFactory.getLogger(LoggingViolationListener.class);

    @Override
    public void onViolate(ThresholdRule threshold, long timestamp, double update) {
        Date date = new Date(timestamp);
        logger.warn("{} violated by {} at {}", threshold, update, date);
    }

}
