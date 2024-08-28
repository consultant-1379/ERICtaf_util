package com.ericsson.cifwk.taf.swt.agent.selector;

import static com.ericsson.cifwk.taf.swt.agent.selector.CssSelector.Type.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class CssSelectorParserTest {

    private CssSelectorParser parser;

    @Before
    public void setUp() {
        parser = new CssSelectorParser();
    }

    @Test
    public void parseInternal() {
        assertSelector("Mihails", TEXT, "Mihails", null);
        assertSelector("#123", INDEX, "123", null);
        assertSelector("#firstName", ID, "firstName", null);
        assertSelector("[custom.key=firstName]", CUSTOM_ID, "custom.key", "firstName");
        assertSelector("custom.key#firstName", CUSTOM_ID, "custom.key", "firstName");
        assertSelector("Should be exactly as in your passport:hover", TOOLTIP, "Should be exactly as in your passport", null);
        assertSelector("First name:+", LABEL, "First name:", null);
    }

    @Test
    public void parse() {
        assertEquals("Mihails", parser.parse("Mihails").getText());
        assertEquals(123, parser.parse("#123").getIndex());
        assertEquals("firstName", parser.parse("#firstName").getId());
        assertEquals("custom.key", parser.parse("[custom.key=firstName]").getCustomIdKey());
        assertEquals("firstName", parser.parse("[custom.key=firstName]").getCustomIdValue());
        assertEquals("Should be exactly as in your passport", parser.parse("Should be exactly as in your passport:hover").getTooltip());
        assertEquals("First name:", parser.parse("First name:+").getLabel());
    }

    private void assertSelector(String selector, CssSelector.Type expectedType, String... values) {
        CssSelector cssSelector = parser.parseInternal(selector);
        assertEquals(expectedType, cssSelector.getType());
        assertEquals(values[0], cssSelector.getValue());
        assertEquals(values[1], cssSelector.getOptionalValue());
    }

}
