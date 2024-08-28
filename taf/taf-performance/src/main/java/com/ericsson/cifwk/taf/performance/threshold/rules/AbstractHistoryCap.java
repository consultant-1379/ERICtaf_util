package com.ericsson.cifwk.taf.performance.threshold.rules;

import com.ericsson.cifwk.taf.performance.threshold.ThresholdRule;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.util.ArrayDeque;
import java.util.Queue;

public abstract class AbstractHistoryCap implements ThresholdRule {

    private final int windowSize;
    private final Queue<Point> window;

    protected AbstractHistoryCap(int windowSize) {
        this.windowSize = windowSize;
        window = new ArrayDeque<>(windowSize);
    }

    protected void record(double x, double y) {
        if (isFull()) {
            window.remove();
        }
        window.add(new Point(x, y));
    }

    protected Iterable<Double> getYs() {
        return Iterables.transform(window, new Function<Point, Double>() {
            @Override
            public Double apply(Point point) {
                return point.getY();
            }
        });
    }

    protected boolean isFull() {
        return window.size() == windowSize;
    }

    protected static final class Point {

        private final double x;
        private final double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
}
