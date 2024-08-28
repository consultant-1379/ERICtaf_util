package com.ericsson.cifwk.taf.swt.agent.selector.matchers;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WidgetPropertyMatcherTest {

    @Test
    public void testDoMatchHappyPath() {
        Map<String, String> map = Maps.newHashMap();
        map.put("text", "myText");
        map.put("toolTipText", "myToolTip");
        map.put("caretPosition", "443");
        map.put("lineDelimiter", ",");
        map.put("enabled", "true");
        WidgetPropertyMatcher unit = new WidgetPropertyMatcher(map);

        TextWidget widget = mock(TextWidget.class);
        when(widget.isEnabled()).thenReturn(true);
        when(widget.getLineDelimiter()).thenReturn(",");
        when(widget.getText()).thenReturn("myText");
        when(widget.getToolTipText()).thenReturn("myToolTip");
        when(widget.getCaretPosition()).thenReturn(443);

        Assert.assertTrue(unit.doMatch(widget));
    }

    @Test
    public void shouldFailDoMatchOnCustomObjects() {
        Map<String, String> map = Maps.newHashMap();
        map.put("text", "myText");
        map.put("simple", "true");
        WidgetPropertyMatcher unit = new WidgetPropertyMatcher(map);

        TextWidget widget = mock(TextWidget.class);
        when(widget.getText()).thenReturn("myText");
        when(widget.getSimple()).thenReturn(mock(SimpleWidget.class));

        Assert.assertFalse(unit.doMatch(widget));
    }

    @Test
    public void shouldDoMatchNullValues() {
        Map<String, String> map = Maps.newHashMap();
        map.put("text", "myText");
        map.put("toolTipText", "null");
        WidgetPropertyMatcher unit = new WidgetPropertyMatcher(map);

        TextWidget widget = mock(TextWidget.class);
        when(widget.getText()).thenReturn("myText");
        when(widget.getToolTipText()).thenReturn(null);

        Assert.assertTrue(unit.doMatch(widget));
    }

}
