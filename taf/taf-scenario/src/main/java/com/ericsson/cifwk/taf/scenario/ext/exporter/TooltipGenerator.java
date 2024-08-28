/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.cifwk.taf.scenario.ext.exporter;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.GraphNode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.google.common.base.Throwables;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;

/**
 * <pre>
 * Class Name: TooltipGenerator.
 * Description: This class is used to generate ToolTip windows with Nodes information for Scenario visualization.
 * </pre>
 */
@API(Internal)
public class TooltipGenerator {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final int MAX_LENGTH = 80;

    // Tag for HTML page
    private static final String TABLE_OPEN = "<table%s>";
    private static final String TABLE_CLOSE = "</table>";
    private static final String TH_OPEN = "<th>";
    private static final String TH_CLOSE = "</th>";
    private static final String TR_OPEN = "<tr>";
    private static final String TR_CLOSE = "</tr>";
    private static final String TD_OPEN = "<td>";
    private static final String TD_OPEN_NO_BORDER = "<td style='border-right:none;border-left:none;border-bottom:none;border-top:none'>";
    private static final String TD_CLOSE = "</td>";
    private static final String BOLD_OPEN = "<b>";
    private static final String BOLD_CLOSE = "</b>";
    private static final String BREAK = "<br/>";
    private static final String CODE_OPEN = "<code>";
    private static final String CODE_CLOSE = "</code>";
    private static final String VERTICAL_SPACE = "<p> </p>";

    private boolean breakLongLines;

    public TooltipGenerator(final boolean breakLongLines) {
        this.breakLongLines = breakLongLines;
    }

    public String export(final GraphNode node) {
        final HtmlBuilder builder = new HtmlBuilder();
        builder.html(String.format(TABLE_OPEN, " border='1' cellspacing='1' cellpadding='5'"));
        builder.html(TR_OPEN).html(TD_OPEN).html(BOLD_OPEN).html("Name:").html(BOLD_CLOSE).html(TD_CLOSE)
                .html(TD_OPEN).text(node.getName()).html(TD_CLOSE).html(TR_CLOSE);
        builder.html(TR_OPEN).html(TD_OPEN).html(BOLD_OPEN).html("Type:").html(BOLD_CLOSE).html(TD_CLOSE)
                .html(TD_OPEN).text(node.getClass().getSimpleName()).html(TD_CLOSE).html(TR_CLOSE);
        builder.html(TR_OPEN).html(TD_OPEN).html(BOLD_OPEN).html("vUser:").html(BOLD_CLOSE).html(TD_CLOSE)
                .html(TD_OPEN).text(node.getVUser()).html(TD_CLOSE).html(TR_CLOSE);
        builder.html(TR_OPEN).html(TD_OPEN).html(BOLD_OPEN).html("Start Time:").html(BOLD_CLOSE).html(TD_CLOSE)
                .html(TD_OPEN).text(new SimpleDateFormat(DATE_FORMAT).format(node.getStartTime().getTime()))
                .html(TD_CLOSE).html(TR_CLOSE);

        final Long millis = node.getDuration();
        if (millis != null) {
            final DateFormat durationFormat = new SimpleDateFormat("HH:mm:ss.SSS");
            durationFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));

            final String duration = durationFormat.format(new Date(millis));
            builder.html(TR_OPEN).html(TD_OPEN).html(BOLD_OPEN).html("Duration:").html(BOLD_CLOSE).html(TD_CLOSE)
                    .html(TD_OPEN).text(duration).html(TD_CLOSE).html(TR_CLOSE);
        }
        builder.html(TABLE_CLOSE);

        if (node.getDataRecords() != null) {
            builder.html(VERTICAL_SPACE);
            builder.html(String.format(TABLE_OPEN, " border='1' cellspacing='1' cellpadding='2'"))
                    .html(TR_OPEN)
                    .html(TH_OPEN).html("Data Source").html(TH_CLOSE)
                    .html(TH_OPEN).html("Data Record").html(TH_CLOSE)
                    .html(TH_OPEN).html("Value").html(TH_CLOSE)
                    .html(TR_CLOSE);
            for (final Map.Entry<String, DataRecord> dataSourceDataRecord : node.getDataRecords().entrySet()) {
                final String dataSource = dataSourceDataRecord.getKey();
                final DataRecord dataRecord = dataSourceDataRecord.getValue();
                boolean first = true;
                for (final Map.Entry<String, Object> fields : dataRecord.getAllFields().entrySet()) {
                    builder.html(TR_OPEN);
                    if (first) {
                        builder.html(TD_OPEN);
                        builder.text(dataSource);
                        first = false;
                    } else {
                        builder.html(TD_OPEN_NO_BORDER);
                    }
                    builder.html(TD_CLOSE).html(TD_OPEN);
                    builder.text(fields.getKey());
                    builder.html(TD_CLOSE).html(TD_OPEN);
                    builder.text(objectToString(fields.getValue()));
                    builder.html(TD_CLOSE).html(TR_CLOSE);
                }
            }
            builder.html(TABLE_CLOSE);
        }

        if (node.getError() != null) {
            builder.html(VERTICAL_SPACE);
            builder.html(BOLD_OPEN).html("Exception:").html(BOLD_CLOSE).html(BREAK).html(CODE_OPEN);
            final Throwable testStepError = node.getError();
            final String[] errorStrings = Throwables.getStackTraceAsString(testStepError).split("\n");

            final StringBuilder errorBuilder = new StringBuilder();
            for (final String errorString : errorStrings) {
                errorBuilder.append(breakLongLine(errorString, MAX_LENGTH, "\n"));
            }

            builder.text(errorBuilder).html(CODE_CLOSE);
        }

        return builder.toString();
    }

    protected String objectToString(final Object field) {
        if (field == null) {
            return "null";
        }

        final String toString;
        if (field.getClass().isArray()) {
            toString = Arrays.toString((Object[]) field);
        } else {
            toString = field.toString();
        }

        return breakLongLine(toString, MAX_LENGTH, BREAK);
    }

    protected String breakLongLine(final String s, final int maxLength, final String delimiter) {
        if (!breakLongLines) {
            return s;
        }

        final int delimiterLength = delimiter.length();
        final StringBuilder builder = new StringBuilder(s);
        for (int i = 0; i < (s.length() - 1) / maxLength; i++) {
            builder.insert(maxLength + i * (maxLength + delimiterLength), delimiter);
        }

        return builder.toString();
    }

    private String ellipse(final Object value) {
        if (value == null) {
            return "";
        }
        if (value.toString().length() > 50) {
            return value.toString().substring(0, 50) + "...";
        }
        return value.toString();
    }

    private class HtmlBuilder {
        Escaper escaper = HtmlEscapers.htmlEscaper();
        StringBuilder builder = new StringBuilder("<body xmlns=\"http://www.w3.org/1999/xhtml\">");

        public HtmlBuilder html(final Object s) {
            builder.append(s);
            return this;
        }

        public HtmlBuilder text(final Object s) {
            builder.append(escaper.escape(s.toString()));
            return this;
        }

        @Override
        public String toString() {
            return builder.append("</body>").toString();
        }
    }
}
