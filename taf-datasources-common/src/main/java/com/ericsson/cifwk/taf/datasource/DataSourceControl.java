package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.management.TafContext;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 *
 */
@API(Stable)
public final class DataSourceControl {

    private static final Logger log = LoggerFactory.getLogger(DataSourceControl.class);

    private DataSourceControl() {
    }

    /**
     * Stop execution of all cyclic data sources.
     */
    public static void stopExecution() {
        for (Map.Entry<String, TestDataSource<DataRecord>> dataSource : getAllDataSources().entrySet()) {
            unwrapAndStop(dataSource.getValue(), dataSource.getKey());
        }
    }

    /**
     * Stop execution of cyclic data source with given name.
     */
    public static void stopExecution(String name) {
        TestDataSource<DataRecord> dataSource1 = TafTestContext.getContext().getAllDataSources().get(name);

        TestDataSource<DataRecord> dataSource2 = TafContext.getDataSources().get(name);

        Preconditions.checkArgument(dataSource1 != null || dataSource2 != null, "Data source with name '%s' does not exist", name);

        final boolean stopped1 = unwrapAndStop(dataSource1, name);
        final boolean stopped2 = unwrapAndStop(dataSource2, name);

        if (!stopped1 && !stopped2) {
            throw new IllegalArgumentException(String.format("Data source %s stopped", name));
        }
    }

    public static TestDataSource unwrap(TestDataSource dataSource) {
        while (dataSource.getSource() != null) {
            dataSource = dataSource.getSource();
        }

        return dataSource;
    }

    private static boolean unwrapAndStop(TestDataSource dataSource, String name) {
        boolean stopped = false;

        if (dataSource == null) {
            return false;
        }

        do {
            if (dataSource instanceof CyclicDataSource) {
                ((CyclicDataSource) dataSource).stop();
                log.info("Data source {} stopped", name);
                stopped = true;
            }
        } while ((dataSource = dataSource.getSource()) != null);

        return stopped;
    }


    private static Map<String, TestDataSource<DataRecord>> getAllDataSources() {
        Map<String, TestDataSource<DataRecord>> allDataSources = Maps.newHashMap();
        allDataSources.putAll(TafTestContext.getContext().getAllDataSources());
        allDataSources.putAll(TafContext.getDataSources());
        return allDataSources;
    }
}
