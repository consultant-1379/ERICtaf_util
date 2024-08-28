package com.ericsson.cifwk.taf.performance.metric;

import java.util.Random;

public class MetricsAggregatorStressDemo {

    public static void main(String[] args) {
        MetricsName name = MetricsName.builder().group("g").suite("s").test("t").protocol("p").build();
        DummyMetricsWriter writer = new DummyMetricsWriter();
        Random random = new Random();
        OperationResult[] resultValues = OperationResult.values();
        long time = 0;
        while (true) {
            OperationResult result = resultValues[random.nextInt(resultValues.length)];
            writer.update(name, time++, result.toString());
        }
    }

}
