package com.ericsson.cifwk.taf.performance.threshold.rules;

public class MinLinearCap extends AbstractLinearCap {

    public MinLinearCap(double originX, double originY, double slope) {
        super(originX, originY, slope);
    }

    @Override
    public boolean check(double x, double y) {
        return y < valueAt(x);
    }

    @Override
    public String toString() {
        return String.format("\"y < %s\"", equation());
    }

}
