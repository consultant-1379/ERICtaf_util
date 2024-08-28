package com.ericsson.cifwk.taf.tools.http.impl;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.performance.metric.TestMetricsWriter;
import com.ericsson.cifwk.taf.performance.sample.impl.AmqpClient;
import com.ericsson.cifwk.taf.spi.TafPlugin;
import com.ericsson.cifwk.taf.tools.http.HttpToolListener;

import java.io.IOException;
import java.util.UUID;


public class HttpToolTafPlugin implements TafPlugin {

    public static final String PARENT_ID = "ER_REPORTING_PARENT_EXECUTION_ID";

    public static final String METRICS_ENABLED = "taf.performance.metrics.enabled";
    public static final String AMQP_HOST = "taf.performance.metrics.amqp.host";
    public static final String AMQP_EXCHANGE = "taf.performance.metrics.amqp.exchange";

    private MetricsHttpToolListener sampleListener;

    @Override
    public void init() {
        TestMetricsWriter metrics = new TestMetricsWriter(TestMetricsWriter.HTTP);
        HttpToolListener httpToolListener = new RequestListenerAdapter(metrics);
        HttpToolListeners.addListener(httpToolListener);

        String executionId = getStringAttr(PARENT_ID, UUID.randomUUID().toString());
        String enabled = getStringAttr(METRICS_ENABLED, "false");
        String amqpHost = getStringAttr(AMQP_HOST, null);
        String amqpExchange = getStringAttr(AMQP_EXCHANGE, null);

        if (Boolean.parseBoolean(enabled)) {
            enableAmqpListener(executionId, amqpHost, amqpExchange);
        }
    }

    private void enableAmqpListener(String executionId, String amqpHost, String amqpExchange) {
        Host host = DataHandler.getHostByName(amqpHost);

        String user = host.getUser();
        String password = host.getPass();
        String ip = host.getIp();
        int port = Integer.parseInt(host.getPort().get(Ports.AMQP));
        AmqpClient amqp = AmqpClient.create(ip, port, user, password, amqpExchange);
        sampleListener = new MetricsHttpToolListener(executionId);
        HttpToolListeners.addListener(sampleListener);
        try {
            sampleListener.initialize(amqp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
        HttpToolListeners.removeAllListeners();

        if (sampleListener != null) {
            try {
                sampleListener.shutdown();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String getStringAttr(String key, String defaultValue) {
        Object value = DataHandler.getAttribute(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

}
