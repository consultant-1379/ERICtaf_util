/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.supercsv.io.CsvMapReader;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Maps;

/**
 * <pre>
 * Class Name: CsvDataSource
 * Description: Data provider implementation reading data from Excel-formatted CSV files.
 * suite.
 * </pre>
 **/
@API(Internal)
public final class CsvDataSource extends UnmodifiableDataSource<DataRecord> {

    public static final String DELIMITER_ATTRIBUTE = "taf.csv.delimiter";
    public static final String LOCATION = "location";
    public static final String URI = "uri";
    public static final String DELIMITER = "delimiter";

    private CsvReaderFactory factory;

    /**
     * <pre>
     * Name: init(ConfigurationSource)
     * Description: initialization of 'CsvDataSource' instance. This method read property  values and fill instance local values.
     * </pre>
     *
     * @param reader
     *         supplier of configuration properties
     */
    @Override
    public void init(final ConfigurationSource reader) {
        final String dataProviderDelimiter = reader.getProperty(DELIMITER);
        final String location = reader.getProperty(LOCATION);
        final String uri = reader.getProperty(URI);

        /* check if at least one of property 'locator' (location, uri) is definied */
        final String msg = String.format("Can't initialise Data source, because '%s' or '%s' property is not supplied", LOCATION, URI);
        Preconditions.checkArgument(location != null || uri != null, msg);

        this.factory = new CsvReaderFactory(location, uri, dataProviderDelimiter);
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public TestDataSource getSource() {
        return null;
    }

    @Override
    public Iterator<DataRecord> iterator() {
        final List<DataRecord> dataRecords = newArrayList();
        try (CsvMapReader reader = this.factory.createCsvReader()) {
            dataRecords.addAll(loadData(reader));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return dataRecords.iterator();
    }

    private String[] loadHeaders(final CsvMapReader reader) throws IOException {
        return reader.getHeader(true);
    }

    private List<DataRecord> loadData(final CsvMapReader reader) throws IOException {
        final List<DataRecord> dataRecords = newArrayList();
        final String[] headers = loadHeaders(reader);
        final CsvReaderIterator iterator = new CsvReaderIterator(reader, headers);
        while (iterator.hasNext()) {
            dataRecords.add(iterator.next());
        }
        return dataRecords;
    }

    @Override
    public String toString() {
        return "CsvDataSource{" + "factory=" + factory + '}';
    }

    private class CsvReaderIterator extends AbstractIterator<DataRecord> {
        private final CsvMapReader csvReader;
        private String[] headers;

        CsvReaderIterator(final CsvMapReader csvReader, final String[] headers) {
            this.csvReader = csvReader;
            this.headers = headers;
        }

        @Override
        protected DataRecord computeNext() {
            try {
                final Map<String, String> row = csvReader.read(headers);
                if (row != null) {
                    final Map<String, Object> copy = Maps.newHashMap();
                    copy.putAll(row);
                    final DataRecord dataRecord = TestDataSourceFactory.createDataRecord(copy);
                    return DataRecordProxyFactory.createProxy(dataRecord, DataRecord.class);
                }
            } catch (final IOException e) {
                close();
                throw new RuntimeException(e);
            }
            close();
            return endOfData();
        }

        private void close() {
            try {
                csvReader.close();
            } catch (final IOException e) {
                throw new RuntimeException("Closing CSV reader failed", e);
            }
        }
    }
}
