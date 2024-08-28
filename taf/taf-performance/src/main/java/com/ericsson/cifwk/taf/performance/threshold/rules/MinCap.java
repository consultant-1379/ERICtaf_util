package com.ericsson.cifwk.taf.performance.threshold.rules;

import com.ericsson.cifwk.taf.performance.threshold.ThresholdRule;

public class MinCap implements ThresholdRule {

    private final double minY;

    public MinCap(double minY) {
        this.minY = minY;
    }

    @Override
    public boolean check(double x, double y) {
        return y < minY;
    }

    @Override
    public String toString() {
        return String.format("\"y < %.3f\"", minY);
    }

}
