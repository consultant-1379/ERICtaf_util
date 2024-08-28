package com.ericsson.cifwk.taf.performance.metric;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.performance.PerformancePluginServices;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MetricsDemo {

    private static final int VUSERS = 150;
    private static final String AMQP_HOST = "rabbitmq";
    private static final String EXCHANGE_NAME = "eiffel.poc.graphite";

    public static void main(String[] args) throws Exception {
        configureDataHandler();
        MetricsWriter writer = PerformancePluginServices.getDefaultMetricsWriter();
        TestMetricsWriter metrics = new TestMetricsWriter(TestMetricsWriter.TAF, writer);

        ExecutorService executorService = Executors.newFixedThreadPool(VUSERS);
        for (int i = 0; i < VUSERS; i++) {
            executorService.submit(new VUser(metrics));
        }
    }

    private static void configureDataHandler() {
        DataHandler.setAttribute("taf.performance.writer.jmx.enabled", "true");
        DataHandler.setAttribute("taf.performance.writer.graphite.enabled", "true");
        DataHandler.setAttribute("taf.performance.writer.graphite.amqp.host", AMQP_HOST);
        DataHandler.setAttribute("taf.performance.writer.graphite.amqp.exchange", EXCHANGE_NAME);
    }

    private static class VUser implements Runnable {

        private static final OperationResult[] RESULTS = OperationResult.values();

        private final TestMetricsWriter listener;
        private final Random random;

        public VUser(TestMetricsWriter listener) {
            this.listener = listener;
            random = new Random();
        }

        @Override
        public void run() {
            while (true) {
                long time = random.nextInt(10_000) + 1;
                OperationResult result = RESULTS[random.nextInt(RESULTS.length)];
                String suiteName = "suite" + random.nextInt(3);
                String testName = "test" + random.nextInt(5);
                try {
                    update(suiteName, testName, time, result);
                } catch (Exception e) {
                    System.out.println("Worker error: " + e);
                }
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        private void update(String suiteName, String testName, long time, OperationResult testResult) {
            listener.update(suiteName, testName, time, testResult.toString());
        }
    }

}
