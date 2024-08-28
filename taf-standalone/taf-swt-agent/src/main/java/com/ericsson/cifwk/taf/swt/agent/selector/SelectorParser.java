package com.ericsson.cifwk.taf.swt.agent.selector;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public interface SelectorParser<T> {

    boolean canParse(String selector);

    T parse(String selector);

}
