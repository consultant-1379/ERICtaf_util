package com.ericsson.cifwk.taf.performance.threshold.rules;

import com.ericsson.cifwk.taf.performance.threshold.ThresholdRule;

public class MaxCap implements ThresholdRule {

    private final double maxY;

    public MaxCap(double maxY) {
        this.maxY = maxY;
    }

    @Override
    public boolean check(double x, double y) {
        return y > maxY;
    }

    @Override
    public String toString() {
        return String.format("\"y > %.3f\"", maxY);
    }

}
