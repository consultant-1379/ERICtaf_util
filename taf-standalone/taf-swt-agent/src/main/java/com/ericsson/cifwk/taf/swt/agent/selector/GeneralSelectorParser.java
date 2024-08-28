package com.ericsson.cifwk.taf.swt.agent.selector;

import com.ericsson.cifwk.meta.API;
import com.google.common.collect.Lists;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class GeneralSelectorParser implements SelectorParser<SwtSelector> {

    private List<SelectorParser<SwtSelector>> parsers = Lists.newArrayList();

    public GeneralSelectorParser() {
        parsers.add(new CssSelectorParser());
        parsers.add(new SwtSelectorParser());
    }

    @Override
    public boolean canParse(String selector) {
        for (SelectorParser<SwtSelector> parser : parsers) {
            if (parser.canParse(selector)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SwtSelector parse(String selector) {
        for (SelectorParser<SwtSelector> parser : parsers) {
            if (parser.canParse(selector)) {
                return parser.parse(selector);
            }
        }
        throw new IllegalArgumentException("Unsupported selector");
    }

}
