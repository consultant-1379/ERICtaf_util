package com.ericsson.cifwk.taf.performance.threshold.rules;

public class StandardDeviationCap extends AbstractHistoryCap {

    public static final int DEFAULT_WINDOW_SIZE = 100;

    private final double maxSigma;

    public StandardDeviationCap(double maxSigma) {
        this(maxSigma, DEFAULT_WINDOW_SIZE);
    }

    public StandardDeviationCap(double maxSigma, int windowSize) {
        super(windowSize);
        this.maxSigma = maxSigma;
    }

    @Override
    public boolean check(double x, double y) {
        record(x, y);
        if (!isFull()) {
            return false;
        }

        Iterable<Double> ys = getYs();
        double sigma = stdDev(ys);
        return sigma > maxSigma;
    }

    static double stdDev(Iterable<Double> xs) {
        double sum = 0;
        int size = 0;
        for (Double x : xs) {
            sum += x;
            size++;
        }
        double mean = sum / size;
        double deltaSquares = 0;
        for (Double x : xs) {
            double delta = x - mean;
            deltaSquares += delta * delta;
        }
        return Math.sqrt(deltaSquares / size);
    }

}
