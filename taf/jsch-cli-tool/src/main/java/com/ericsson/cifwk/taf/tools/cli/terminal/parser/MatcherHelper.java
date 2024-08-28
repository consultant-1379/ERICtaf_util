package com.ericsson.cifwk.taf.tools.cli.terminal.parser;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

public class MatcherHelper {

    public static Matcher between(int i1, int i2) {
        return between((char) i1, (char) i2);
    }

    public static Matcher between(char c1, char c2) {
        return both(greaterThanOrEqualTo(c1)).and(lessThanOrEqualTo(c2));
    }

    public static Matcher anyOf(Matcher... marchers) {
        return org.hamcrest.Matchers.anyOf(marchers);
    }

    public static Matcher equalTo(int i) {
        return equalTo((char) i);
    }

    public static Matcher equalTo(char c) {
        return org.hamcrest.Matchers.equalTo(c);
    }

}
