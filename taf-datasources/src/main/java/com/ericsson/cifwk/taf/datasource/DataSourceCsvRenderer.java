package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.*;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@API(Stable)
public final class DataSourceCsvRenderer implements DataSourceRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceCsvRenderer.class);

    @Override
    public void render(Writer writer, TestDataSource<DataRecord> dataSource) {
        LOGGER.info("Exporting data source {} to CSV format", dataSource.toString());

        ICsvMapWriter csvWriter;
        try {
            csvWriter = new CsvMapWriter(writer, CsvPreference.STANDARD_PREFERENCE);

            Iterator<DataRecord> iterator = dataSource.iterator();
            writeRows(csvWriter, iterator);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LOGGER.warn("Failed to close writer", e);
                }
            }
        }
    }

    private void writeRows(ICsvMapWriter writer, Iterator<DataRecord> iterator) {
        boolean firstRow = true;

        try {
            while (iterator.hasNext()) {
                DataRecord dataRecord = iterator.next();
                Map<String, Object> fields = dataRecord.getAllFields();
                Set<String> headerSet = fields.keySet();
                String[] headers = headerSet.toArray(new String[headerSet.size()]);
                if (firstRow) {
                    writer.writeHeader(headers);
                }
                writer.write(fields, headers);
                writer.flush();

                firstRow = false;
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

}
