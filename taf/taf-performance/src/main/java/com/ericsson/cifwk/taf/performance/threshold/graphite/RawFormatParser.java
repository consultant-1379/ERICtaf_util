package com.ericsson.cifwk.taf.performance.threshold.graphite;

import com.ericsson.cifwk.taf.performance.threshold.MetricSlice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders raw format of Graphite
 * https://graphite.readthedocs.org/en/latest/render_api.html
 *
 * Format is the following:
 * <target name>,<start timestamp>,<end timestamp>,<series step>|[data]*
 */
public class RawFormatParser implements GraphiteParser {

    private Pattern headerPattern = Pattern.compile("([\\w\\.]+),([0-9]+),([0-9]+),([0-9]+)\\|(.*)$");

    @Override
    public MetricSlice parse(String raw) {
        MetricSlice result = new MetricSlice();
        Matcher headerMatcher = headerPattern.matcher(raw);
        while (headerMatcher.find()) {
            result.setTargetName(headerMatcher.group(1));
            result.setStartTimestamp(Long.parseLong(headerMatcher.group(2)));
            result.setEndTimestamp(Long.parseLong(headerMatcher.group(3)));
            result.setSeriesStep(Integer.parseInt(headerMatcher.group(4)));
            String[] strings = headerMatcher.group(5).split(",");
            Double[] data = new Double[strings.length];
            for (int i = 0; i < strings.length; i++) {
                String value = strings[i];
                if (!"None".equals(value)) {
                    data[i] = Double.valueOf(value);
                }
            }
            result.setData(data);
        }

        return result;
    }

}
