package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * TestDataSourceFormatter
 * <p>
 * Utility class for formatting the TestDataSource<DataRecord> data structures so that it can be printed it a Tabular format
 */
@API(Stable)
public class TestDataSourceFormatter {

    private static Logger logger = LoggerFactory.getLogger(TestDataSourceFormatter.class);

    List<Object[]> rows = new LinkedList<>();

    public static int maxWidth = 200;

    private void addRow(Object... cols) {
        rows.add(cols);
    }

    private int[] getColWidths() {
        int cols = -1;

        for (Object[] row : rows)
            cols = Math.max(cols, row.length);

        int[] widths = new int[cols];

        for (Object[] row : rows) {
            for (int colNum = 0; colNum < row.length; colNum++) {
                widths[colNum] = Math.min(Math.max(widths[colNum], StringUtils.length(row[colNum].toString())), maxWidth);
            }
        }

        return widths;
    }

    private int[] getColStartPos(int[] colWidths) {

        int[] colStartPos = new int[colWidths.length];
        colStartPos[0] = 0;
        int columnwidth = 0;
        for (int i = 1; i < colWidths.length; i++) {
            columnwidth = columnwidth + colWidths[i - 1] + 1;
            colStartPos[i] = columnwidth;
        }

        return colStartPos;
    }

    public static void setMaxWidth(int width) {
        maxWidth = width;
    }

    /**
     * Converts the rows List<Object[]> into formatted String
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        int[] colWidths = getColWidths();
        int[] colStartPos = getColStartPos(colWidths);
        int columnwidth = 0;
        for (int col : colWidths) {
            columnwidth += col;
        }
        decorate('*', columnwidth, buf);

        for (Object[] row : rows) {
            Map<Integer, StringBuilder> additionalRows = new HashMap<>();
            for (int colNum = 0; colNum < row.length; colNum++) {
                String columValue = row[colNum].toString();
                if (columValue.length() > colWidths[colNum]) {
                    Iterable<String> result = Splitter.fixedLength(colWidths[colNum]).split(columValue);
                    String[] parts = Iterables.toArray(result, String.class);

                    buf.append(StringUtils.rightPad(StringUtils.defaultString(parts[0]), colWidths[colNum]));
                    buf.append(' ');

                    for (int i = 1; i < parts.length; i++) {
                        StringBuilder addbuf = additionalRows.get(i - 1);
                        if (addbuf == null) {
                            addbuf = new StringBuilder();
                            additionalRows.put(i - 1, addbuf);
                        }

                        addbuf.append(StringUtils.repeat(' ', colStartPos[colNum] - addbuf.length()));
                        addbuf.append(StringUtils.rightPad(StringUtils.defaultString(parts[i]), colWidths[colNum]));
                        addbuf.append(' ');

                    }
                } else {
                    buf.append(StringUtils.rightPad(StringUtils.defaultString(columValue), colWidths[colNum]));
                    buf.append(' ');
                }
            }
            buf.append('\n');
            // Add additional Rows
            for (StringBuilder additionalRow : additionalRows.values()) {
                buf.append(additionalRow);
                buf.append('\n');
            }

        }
        decorate('*', columnwidth, buf);

        return buf.toString();
    }

    public void decorate(char ch, int width, StringBuilder buf) {
        for (int i = 0; i <= width; i++) {
            buf.append(ch);
        }
        buf.append('\n');
    }

    /**
     * Returns the String in the form of tabular format for TestDataSource<DataRecord>
     * Eg:
     *
     * <pre>
     * ****************************************
     *  ts                  seriesid   Supervised
     *  2016-02-01:23:57:09 svc-6-msfm 901
     *  2016-02-01:23:57:10 svc-5-msfm 697
     *  2016-02-01:23:57:19 svc-3-msfm 623
     *  ****************************************
     * </pre>
     *
     * @param testDataSource
     *        <DataRecord>
     * @Output Returns the formatted string
     */
    public static String format(TestDataSource<? extends DataRecord> testDataSource) {

        TestDataSourceFormatter tb = new TestDataSourceFormatter();
        if (!testDataSource.iterator().hasNext()) {
            logger.info("Empty TestDataSource");
            return "";
        }
        DataRecord dataRecordHeader = testDataSource.iterator().next();
        Set<String> setHeaders = dataRecordHeader.getAllFields().keySet();
        if (setHeaders.size() == 0) {
            logger.info("DataSource does not seem to have any columns");
            return "";
        }

        List<Object> headers = new ArrayList<>();
        for (String header : setHeaders) {
            if (header != null)
                headers.add(header);
            else
                headers.add("null");
        }

        tb.addRow(headers.toArray());

        for (DataRecord datarecord : testDataSource) {
            List<Object> values = new ArrayList<>();
            for (Object header : headers) {
                Object value = datarecord.getFieldValue((String) header);
                if (value != null)
                    values.add(datarecord.getFieldValue((String) header));
                else
                    values.add("null");
            }
            tb.addRow(values.toArray());
        }
        return (tb.toString());

    }

}
