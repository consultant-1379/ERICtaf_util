package com.ericsson.cifwk.taf;

/**
 * <p>Java class for TAF specific label-name.
 *
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 04/08/2015
 *         <p/>
 */
public enum TafLabelName {
    COMMENT("comment"),
    GROUP("group"),
    EXECUTION_TYPE("execution_type");

    private final String value;

    TafLabelName(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TafLabelName fromValue(String v) {
        for (TafLabelName c : TafLabelName.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
