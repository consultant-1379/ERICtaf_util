package com.ericsson.cifwk.taf.swt.agent.selector.matchers;

import org.eclipse.swt.widgets.Widget;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WidgetDataMatcherTest {

    @Test
    public void testDoMatch() throws Exception {
        Widget widget = mock(Widget.class);
        when(widget.getData()).thenReturn("BigData");

        Assert.assertTrue(new WidgetDataMatcher("BigData").doMatch(widget));
        Assert.assertFalse(new WidgetDataMatcher(null).doMatch(widget));
        Assert.assertFalse(new WidgetDataMatcher("foo").doMatch(widget));
    }
}