package com.ericsson.cifwk.taf.swt.agent.selector.matchers;

import org.eclipse.swt.widgets.Widget;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class WidgetOfTypeTest {

    @Test
    public void instance_shouldInstantiate_eclipseWidgetOfType_whenTypeName_isWidgetSubclass() throws Exception {
        String typeName = Widget.class.getName();

        Matcher<? extends Widget> result = WidgetOfType.instance(typeName);

        assertThat(result, instanceOf(org.eclipse.swtbot.swt.finder.matchers.WidgetOfType.class));
    }

    @Test
    public void instance_shouldInstantiate_tafWidgetOfType_whenTypeName_notValidClass() throws Exception {
        String typeName = "foo";

        Matcher<? extends Widget> result = WidgetOfType.instance(typeName);

        assertThat(result, instanceOf(WidgetOfType.class));
    }
}