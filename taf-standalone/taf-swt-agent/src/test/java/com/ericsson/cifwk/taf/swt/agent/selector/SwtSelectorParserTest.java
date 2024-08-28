package com.ericsson.cifwk.taf.swt.agent.selector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class SwtSelectorParserTest {

    private SwtSelectorParser parser;

    @Before
    public void setUp() {
        parser = new SwtSelectorParser();
    }

    @Test
    public void getJson() {
        assertNotNull(parser.parse("{type = 'org.eclipse.swt.widgets.Shell'}"));
        assertNotNull(parser.parse("some id {type = 'org.eclipse.swt.widgets.Shell'}"));
        assertNotNull(parser.parse("some id {nonExistingPtoperty = 'value'}"));
        assertNotNull(parser.parse("{}"));
        assertNotNull(parser.parse("sdakfjh{}"));
        assertNotNull(parser.parse("{type = 'type', parent = {type = 'containerType'}}"));
        assertNull(parser.parse("name containing {} symbols"));
        assertNull(parser.parse("type = 'org.eclipse.swt.widgets.Shell'"));

        SwtSelector selector = parser.parse("some id {type = 'type', text = 'text', container = 'container'}");
        assertEquals("type", selector.getType());
        assertEquals("text", selector.getText());
        assertEquals("container", selector.getContainer());
    }

    @Test
    public void getJsonWithWidgetProperties() {
        SwtSelector selector = parser.parse("{type = 'org.eclipse.swt.widgets.Text', text='Delete me', visible='true'}");
        Map<String, String> widgetProperties = selector.getWidgetProperties();
        assertEquals(3, widgetProperties.size());
        assertEquals("org.eclipse.swt.widgets.Text", widgetProperties.get("type"));
        assertEquals("Delete me", widgetProperties.get("text"));
        assertEquals("true", widgetProperties.get("visible"));
    }

    @Test
    public void shouldExtractWidgetProperties() {
        assertThat(parser.extractWidgetProperties(""), equalTo(Collections.EMPTY_MAP));
        assertThat(parser.extractWidgetProperties("x="), equalTo(Collections.EMPTY_MAP));
        Map<String, String> widgetProperties =
                parser.extractWidgetProperties("a=,b= ,c=2,d");
        Assert.assertEquals(1, widgetProperties.size());
        Assert.assertEquals("2", widgetProperties.get("c"));
    }

    @Test
    public void shouldExtractWidgetPropertiesHappyPath() {
        Map<String, String> widgetProperties =
                parser.extractWidgetProperties("type = 'org.company.type', text = 'text', number=-123.12, boolean=false, nullOne=null");
        Assert.assertEquals(5, widgetProperties.size());
        Assert.assertEquals("org.company.type", widgetProperties.get("type"));
        Assert.assertEquals("text", widgetProperties.get("text"));
        Assert.assertEquals("-123.12", widgetProperties.get("number"));
        Assert.assertEquals("false", widgetProperties.get("boolean"));
        Assert.assertEquals("null", widgetProperties.get("nullOne"));
    }

}