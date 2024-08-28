package com.ericsson.cifwk.taf.scenario;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import java.lang.reflect.Method;

import com.ericsson.cifwk.taf.datasource.TafDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.api.Nullable;
import com.ericsson.cifwk.taf.datasource.ConfigurationSource;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataSourceContext;
import com.ericsson.cifwk.taf.datasource.DataSourceNotFoundException;
import com.ericsson.cifwk.taf.datasource.PropertiesReader;
import com.ericsson.cifwk.taf.datasource.TafDataSourceProvider;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.UnknownDataSourceTypeException;
import com.ericsson.cifwk.taf.spi.DataSourceAdapter;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 10/03/2016
 */
@API(Internal)
public class TafDataSourceAdapter implements DataSourceAdapter<DataRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(TafDataSourceAdapter.class);

    @Override
    public Optional<TestDataSource<DataRecord>> provide(String name,
                                                        @Nullable Method method,
                                                        @Nullable DataSourceContext globalContext,
                                                        @Nullable Predicate<? super DataRecord> predicate,
                                                        @Nullable Class<DataRecord> dataRecordType) {
        ConfigurationSource reader = new PropertiesReader(name);
        TestDataSource<DataRecord> dataSource;
        try {
            dataSource = TafDataSourceProvider.provide(new TafDataSourceFactory(), name, method, globalContext, predicate, dataRecordType);
            if (dataSource == null) {
                return Optional.absent();
            }
        } catch (UnknownDataSourceTypeException e) {
            return Optional.absent();
        } catch (DataSourceNotFoundException e) {
            LOG.debug("Data source not found", e);
            return Optional.absent();
        }
        dataSource.init(reader);
        return Optional.of(dataSource);
    }
}
