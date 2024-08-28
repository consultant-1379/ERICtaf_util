package com.ericsson.cifwk.taf.handlers.netsim;

import com.ericsson.cifwk.taf.handlers.netsim.lang.NetSimConstants;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 *
 */
class ResultProcessor {

    public static List<String> parseLines(String output) {
        Iterable<String> iterable = splitByLine(output);
        Iterable<String> withoutEmptyLines = Iterables.filter(iterable, new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return !input.isEmpty();
            }
        });
        Iterable<String> transformedLines = Iterables.transform(withoutEmptyLines, new Function<String, String>() {
            @Override
            public String apply(String input) {
                if (input != null) {
                    return input.trim();
                }
                return input;
            }
        });
        return Lists.newArrayList(transformedLines);
    }

    public static List<Map<String, String>> parseColumns(String output) {
        Iterable<String> iterable = splitByLine(output);

        List<Map<String, String>> result = new ArrayList<>();
        Iterator<String> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return result;
        }

        String header = iterator.next();
        Iterable<String> fields = Splitter.on(Pattern.compile("\\s{2,}")).split(header);
        String[] fieldArray = Iterables.toArray(fields, String.class);

        while (iterator.hasNext()) {
            String line = iterator.next().trim();
            if (line.isEmpty()) {
                continue;
            }
            Map<String, String> attributes = new TreeMap<>();
            for (int i = 0; i < fieldArray.length; i++) {
                String field = fieldArray[i];
                String nextField = null;
                if (i + 1 < fieldArray.length) {
                    nextField = fieldArray[i + 1];
                }
                int start = Math.min(header.indexOf(field), line.length());

                // CIP-4856
                while (!(start == 0 || start >= line.length()) && line.charAt(start - 1) != ' ') {
                    ++start;
                }

                int stop;
                if (nextField == null) {
                    stop = line.length();
                } else {
                    stop = Math.min(header.indexOf(nextField), line.length());
                }

                // CIP-4856
                while (!(stop == 0 || stop >= line.length()) && line.charAt(stop - 1) != ' ') {
                    ++stop;
                }

                String value = line.substring(start, stop).trim();
                attributes.put(field, value);
            }
            result.add(attributes);
        }

        return result;
    }

    public static Map<String, List<Map<String, String>>> parseSections(String output) {
        Iterable<String> byLine = splitByLine(output);

        SectionsOutputVisitor visitor = new SectionsOutputVisitor();
        visitOutput(byLine, visitor);

        return visitor.getResult();
    }

    private static void visitOutput(Iterable<String> iterable, NetSimOutputVisitor visitor) {
        visitor.onStart();
        for (String line : iterable) {
            if (line.trim().isEmpty()) {
                visitor.onEmptyLine();
            } else {
                visitor.onLine(line.trim());
            }
        }
        visitor.onEnd();
    }

    private static Iterable<String> splitByLine(String output) {
        Iterable<String> lines = Splitter.on(NetSimConstants.NEW_LINE).trimResults(CharMatcher.is('\r')).split(output);

        return Iterables.filter(lines, new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return !input.startsWith(NetSimConstants.COMMAND_EXEC_START)
                        && !input.startsWith(NetSimConstants.RUNNING_COMMAND_SPACES)
                        && !input.equals(NetSimConstants.COMMAND_EXEC_SUCCESS)
                        && !input.equals(NetSimConstants.COMMAND_OUTPUT_END);
            }
        });
    }

    /**
     * Method to parse netsim output into Map where each row is a separate key value pair with row items split into a list
     * <p/>
     * Row items are split by either having multiple spaces separating them or a space followed by a '/' character.
     * The key of a row item is the first String element found which is then not included in the list.
     *
     * @param rawOutput - the output returned after running a netsim command
     * @return map containing the parsed output
     */
    public static Map<String, List<String>> parseRows(String rawOutput) {
        List<String> lines = parseLines(rawOutput);
        Map<String, List<String>> result = Maps.newHashMap();
        for (String line : lines) {
            List<String> splitToList = splitToList(line);
            if (splitToList.isEmpty()) {
                continue;
            }
            String header = splitToList.get(0);
            List<String> rowFields = Lists.newArrayList(splitToList);
            rowFields.remove(header);
            result.put(header, rowFields);
        }

        return result;
    }

    @VisibleForTesting
    protected static List<String> splitToList(String line) {
        List<String> returnList = new ArrayList<>();
        List<String> splitToList = Splitter.on(Pattern.compile("\\s{2,}")).trimResults().splitToList(line);
        for (String item : splitToList) {
            if (item.contains(" /")) {
                String[] tempList = item.split(" /");
                returnList.add(tempList[0]);
                returnList.add("/" + tempList[1]);
            } else {
                returnList.add(item);
            }
        }
        return returnList;
    }
}
