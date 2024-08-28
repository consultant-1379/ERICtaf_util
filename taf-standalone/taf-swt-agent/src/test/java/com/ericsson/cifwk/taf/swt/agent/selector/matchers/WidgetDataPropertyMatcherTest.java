package com.ericsson.cifwk.taf.swt.agent.selector.matchers;

import com.google.common.collect.Maps;
import org.eclipse.swt.widgets.Widget;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WidgetDataPropertyMatcherTest {

    @Test
    public void testDoMatch() throws Exception {
        Map<String, Object> map = Maps.newHashMap();
        map.put("dataKey", "dataObj");
        WidgetDataPropertyMatcher unit = new WidgetDataPropertyMatcher(map);

        Widget widget = mock(Widget.class);
        when(widget.getData(eq("dataKey"))).thenReturn("dataObj");
        Assert.assertTrue(unit.doMatch(widget));

        map.put("dataKey2", "dataObj2");
        Assert.assertFalse(unit.doMatch(widget));

        when(widget.getData(eq("dataKey2"))).thenReturn(null);
        Assert.assertFalse(unit.doMatch(widget));
    }

}