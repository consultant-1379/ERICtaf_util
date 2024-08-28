package com.ericsson.cifwk.taf.swt.agent.selector;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtSelectorParser implements SelectorParser<SwtSelector> {

    private static final Pattern JSON_SELECTOR = Pattern.compile("^.*?(\\{(.*)\\}).?$");

    private static final Gson gson = new Gson();

    @Override
    public SwtSelector parse(String selector) {
        Matcher matcher = JSON_SELECTOR.matcher(selector);
        if (!matcher.matches()) {
            return null;
        }
        String selectorJson = matcher.group(1);
        SwtSelector swtSelector = gson.fromJson(selectorJson, SwtSelector.class);
        String selectorBody = matcher.group(2);
        Map<String, String> widgetProperties = extractWidgetProperties(selectorBody);
        swtSelector.setWidgetProperties(widgetProperties);
        return swtSelector;
    }

    Map<String, String> extractWidgetProperties(String selector) {
        Map<String, String> result = Maps.newHashMap();
        Iterable<String> propertyDefinitions = Splitter.on(",").omitEmptyStrings().trimResults().split(selector);
        Splitter propertySplitter = Splitter.on("=").omitEmptyStrings().trimResults(CharMatcher.is('\'').or(CharMatcher.WHITESPACE));
        for (String propertyDefinition : propertyDefinitions) {
            List<String> parts = propertySplitter.splitToList(propertyDefinition);
            if (parts.size() == 2) {
                result.put(parts.get(0), parts.get(1));
            }
        }
        return result;
    }

    @Override
    public boolean canParse(String selector) {
        return JSON_SELECTOR.matcher(selector).matches();
    }

}
