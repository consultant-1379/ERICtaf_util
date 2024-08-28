package com.ericsson.cifwk.taf.swt.agent.selector;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
class CssSelector {

    private final Type type;

    private final String value;

    private final String optionalValue;

    enum Type {

        INDEX, ID, CUSTOM_ID, LABEL, TOOLTIP, TEXT;

    }

    public CssSelector(Type type, String value) {
        this(type, value, null);
    }

    public CssSelector(Type type, String value, String optionalValue) {
        this.type = type;
        this.value = value;
        this.optionalValue = optionalValue;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getOptionalValue() {
        return optionalValue;
    }

}