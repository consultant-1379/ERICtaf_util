package com.ericsson.cifwk.taf.swt.agent.selector;

import com.ericsson.cifwk.meta.API;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.swt.agent.selector.CssSelector.Type.CUSTOM_ID;
import static com.ericsson.cifwk.taf.swt.agent.selector.CssSelector.Type.ID;
import static com.ericsson.cifwk.taf.swt.agent.selector.CssSelector.Type.INDEX;
import static com.ericsson.cifwk.taf.swt.agent.selector.CssSelector.Type.LABEL;
import static com.ericsson.cifwk.taf.swt.agent.selector.CssSelector.Type.TEXT;
import static com.ericsson.cifwk.taf.swt.agent.selector.CssSelector.Type.TOOLTIP;

@API(Internal)
class CssSelectorParser implements SelectorParser<SwtSelector> {

    private static final Pattern INDEX_PATTERN = Pattern.compile("#(\\d+)");

    private static final Pattern ID_PATTERN = Pattern.compile("#(.+)");

    private static final Pattern CUSTOM_ID_PATTERN = Pattern.compile("\\[(.+)=(.+)\\]");

    private static final Pattern CUSTOM_ID_PATTERN_SIMPLIFIED = Pattern.compile("(.+)#(.+)");

    private static final Pattern LABEL_PATTERN = Pattern.compile("(.+)\\+");

    private static final Pattern TOOLTIP_PATTERN = Pattern.compile("(.+):hover");

    @Override
    public SwtSelector parse(String selector) {
        CssSelector cssSelector = parseInternal(selector);
        return convert(cssSelector);
    }

    @Override
    public boolean canParse(String selector) {
        return !selector.trim().endsWith("}");
    }

    protected CssSelector parseInternal(String selector) {
        Matcher matcher = INDEX_PATTERN.matcher(selector);
        if (matcher.matches()) {
            return new CssSelector(INDEX, matcher.group(1));
        }
        matcher = ID_PATTERN.matcher(selector);
        if (matcher.matches()) {
            return new CssSelector(ID, matcher.group(1));
        }
        matcher = CUSTOM_ID_PATTERN.matcher(selector);
        if (matcher.matches()) {
            return new CssSelector(CUSTOM_ID, matcher.group(1), matcher.group(2));
        }
        matcher = CUSTOM_ID_PATTERN_SIMPLIFIED.matcher(selector);
        if (matcher.matches()) {
            return new CssSelector(CUSTOM_ID, matcher.group(1), matcher.group(2));
        }
        matcher = LABEL_PATTERN.matcher(selector);
        if (matcher.matches()) {
            return new CssSelector(LABEL, matcher.group(1));
        }
        matcher = TOOLTIP_PATTERN.matcher(selector);
        if (matcher.matches()) {
            return new CssSelector(TOOLTIP, matcher.group(1));
        }
        return new CssSelector(TEXT, selector);
    }

    private SwtSelector convert(CssSelector cssSelector) {
        SwtSelector result = new SwtSelector();

        CssSelector.Type selectorType = cssSelector.getType();
        String value = cssSelector.getValue();

        switch (selectorType) {
        case INDEX:
            result.setIndex(Integer.parseInt(value));
            break;
        case ID:
            result.setId(value);
            break;
        case CUSTOM_ID:
            result.setCustomIdKey(value);
            result.setCustomIdValue(cssSelector.getOptionalValue());
            break;
        case LABEL:
            result.setLabel(value);
            break;
        case TOOLTIP:
            result.setTooltip(value);
            break;
        case TEXT:
            result.setText(value);
            break;
        default:
            throw new IllegalArgumentException("Unsupported selector type: " + selectorType);
        }

        return result;
    }

}
