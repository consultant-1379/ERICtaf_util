package com.ericsson.cifwk.taf.performance.metric.graphite;

import com.ericsson.cifwk.taf.performance.metric.*;
import com.ericsson.cifwk.taf.performance.util.SynchronizedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AmqpWriter implements MetricsWriter {

    private static final String TIME_KEY = "time";
    private static final String SUCCESS_KEY = "success";
    private static final String FAILURE_KEY = "failure";
    private static final String UNKNOWN_KEY = "unknown";
    private static final String TOTAL_KEY = "total";

    private final Logger logger = LoggerFactory.getLogger(TestMetricsWriter.class);

    private final AmqpPublisher amqp;
    private final String namePrefix;
    private final SynchronizedRepository<String, AmqpMetrics> repository;

    public AmqpWriter(MetricsAggregatorFactory aggregatorFactory,
                      AmqpPublisher amqp,
                      ScheduledExecutorService scheduler,
                      String namePrefix) {
        this.amqp = amqp;
        this.namePrefix = namePrefix;
        repository = new SynchronizedRepository<>(AmqpMetrics.supplier(aggregatorFactory));
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sendMetrics();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    void sendMetrics() {
        Map<String, AmqpMetrics> metricsMap = repository.asMap();
        for (Map.Entry<String, AmqpMetrics> entry : metricsMap.entrySet()) {
            String keyPrefix = entry.getKey() + '.';
            MetricsAggregator metrics = entry.getValue().getMetrics();
            long total = metrics.getTotal();

            if (total > 0) {
                long seconds = System.currentTimeMillis() / 1000;
                sendMetric(keyPrefix + TOTAL_KEY, Long.toString(total), seconds);
                sendMetric(keyPrefix + TIME_KEY, Double.toString(metrics.getTimeMean()), seconds);
                sendMetric(keyPrefix + SUCCESS_KEY, Long.toString(metrics.getSuccessCount()), seconds);
                sendMetric(keyPrefix + FAILURE_KEY, Long.toString(metrics.getFailureCount()), seconds);
                sendMetric(keyPrefix + UNKNOWN_KEY, Long.toString(metrics.getUnknownCount()), seconds);
                metrics.reset();
            }
        }
    }

    private void sendMetric(String key, String value, long seconds) {
        try {
            amqp.send(key, value, seconds);
        } catch (IOException e) {
            logger.error("AMQP publish failed", e);
        }
    }

    @Override
    public void update(MetricsName metricsName, long time, String result) {
        String name = AmqpMetrics.name(namePrefix, metricsName);
        AmqpMetrics metrics = repository.getOrCreate(name);
        metrics.update(time, result);
    }
}
