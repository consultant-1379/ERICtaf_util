package com.ericsson.cifwk.taf.spi;

import com.ericsson.cifwk.taf.api.Nullable;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataSourceContext;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.lang.reflect.Method;

/**
 * SPI interface
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 09/03/2016
 */
public interface DataSourceAdapter<T extends DataRecord> {

    /**
     * Provide data source for given name, predicate and data record type
     * @param name name of data source
     * @param method
     * @param globalContext  context where to lookup data source
     * @param predicate predicate which will be applied to data source records
     * @param dataRecordType data record type of data source
     * @return optional data source with specified data record type and applied predicate to records.
     * If such data source is not found, {@link com.google.common.base.Absent} will be returned.
     */
    Optional<TestDataSource<T>> provide(String name,
                                        @Nullable Method method,
                                        @Nullable DataSourceContext globalContext,
                                        @Nullable Predicate<? super T> predicate,
                                        @Nullable Class<T> dataRecordType);

}
