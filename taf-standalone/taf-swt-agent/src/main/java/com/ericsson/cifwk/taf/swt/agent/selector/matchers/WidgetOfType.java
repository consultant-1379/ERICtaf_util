/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.swt.agent.selector.matchers;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.matchers.AbstractMatcher;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class WidgetOfType extends AbstractMatcher<Widget> {

    private String type;

    private WidgetOfType(String type) {
        this.type = type;
    }

    public static Matcher<? extends Widget> instance(String typeName) {
        try {
            return WidgetMatcherFactory.widgetOfType(Class.forName(typeName).asSubclass(Widget.class));
        } catch (ClassNotFoundException e) {
            return new WidgetOfType(typeName);
        }
    }

    @Override
    protected boolean doMatch(Object obj) {
        return type.equals(obj.getClass().getName()); // NOSONAR
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("of type '").appendText(type).appendText("'");
    }

}
