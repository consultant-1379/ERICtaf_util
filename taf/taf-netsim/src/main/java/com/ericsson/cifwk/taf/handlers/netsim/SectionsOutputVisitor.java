package com.ericsson.cifwk.taf.handlers.netsim;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ericsson.cifwk.taf.handlers.netsim.lang.NetSimConstants;

/**
 *
 */
class SectionsOutputVisitor implements NetSimOutputVisitor {

    final Map<String, List<Map<String, String>>> result = new TreeMap<>();
    final StringBuilder content = new StringBuilder();
    boolean nextLineIsHeader;
    String header;

    @Override
    public void onStart() {
        nextLineIsHeader = true;
    }

    @Override
    public void onEmptyLine() {
        processContent();
        nextLineIsHeader = true;
    }

    @Override
    public void onLine(String line) {
        if (line.startsWith("=======")) {
            return;
        }

        if (nextLineIsHeader) {
            header = line;
            nextLineIsHeader = false;
        } else {
            content.append(line).append(NetSimConstants.NEW_LINE);
        }
    }

    @Override
    public void onEnd() {
        processContent();
    }

    private void processContent() {
        if (content.length() == 0) {
            return;
        }

        List<Map<String,String>> items = ResultProcessor.parseColumns(this.content.toString());
        content.setLength(0);
        result.put(header, items);
    }

    public Map<String, List<Map<String, String>>> getResult() {
        return result;
    }
}
