package com.ericsson.cifwk.taf.ddc;

import com.ericsson.cifwk.diagmon.util.instr.config.ConfigTreeNode;
import com.ericsson.cifwk.diagmon.util.instr.outputhandlers.taf.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GraphiteDDCHandler implements DDCHandler {

    public static final String GRAPHITE_METRIC_PREFIX_PROPERTY = "graphite.metric-prefix";

    static final Logger LOG = LoggerFactory.getLogger(GraphiteDDCHandler.class);

    String host;
    String metricPrefix;
    Graphite graphite;

    @Override
    public void init(Config config) {
        host = config.getMonitoredHost();
        metricPrefix = getMetricPrefix();
        String graphiteHost = System.getProperty(Graphite.HOST_PROPERTY, "");
        graphite = new Graphite().init(graphiteHost);
        LOG.debug(Messages.format("GraphiteOutputHandler.init(): host:{0}, metric.prefix:{1}", host, metricPrefix));
    }

    public static String getMetricPrefix() {
        String metricPrefix = System.getProperty(GRAPHITE_METRIC_PREFIX_PROPERTY, "").trim();
        if (!metricPrefix.isEmpty() && !metricPrefix.startsWith(".")) {
            metricPrefix = '.' + metricPrefix;
        }
        return metricPrefix;
    }

    @Override
    public void recordValues(ConfigTreeNode node) {
        recordValues(node, node.baseName());
    }

    void recordValues(ConfigTreeNode node, String metricName) {
        long timestamp = node.getLastUpdate();
        String data = node.getData();
        if (data != null) send(metricName, data, timestamp);
        if (node.hasChildren()) {
            for (ConfigTreeNode child : node.getChildren()) {
                recordValues(child, metricName + "." + child.baseName());
            }
        }
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private void send(String name, String value, long timestamp) {
        name = "tor." + host + (metricPrefix.isEmpty() ? "" : ("." +        metricPrefix)) + "." + name;
        String message = String.format("%s %s %d\n", name, value, timestamp / 1000);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.format("Graphite message: [{2}]:{0}={1}", name, value, sdf.format(new Date(timestamp))));
        }
        graphite.send(message);
    }

    @Override
    public void shutdown() {
        graphite.shutdown();
    }

}

