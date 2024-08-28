package com.ericsson.cifwk.taf.swt.agent.selector;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.swt.agent.selector.matchers.WidgetDataMatcher;
import com.ericsson.cifwk.taf.swt.agent.selector.matchers.WidgetDataPropertyMatcher;
import com.ericsson.cifwk.taf.swt.agent.selector.matchers.WidgetOfType;
import com.ericsson.cifwk.taf.swt.agent.selector.matchers.WidgetPropertyMatcher;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsAnything;

import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;

@API(Internal)
public class MatcherBuilder {

    private Matcher<Widget> matcher = new IsAnything<Widget>();

    public MatcherBuilder() {
        // parameterless constructor
    }

    public MatcherBuilder(SwtSelector swtSelector) {
        withType(swtSelector.getType())
                .withLabel(swtSelector.getLabel())
                .withText(swtSelector.getText())
                .withId(swtSelector.getId())
                .withCustomId(swtSelector.getCustomIdKey(), swtSelector.getCustomIdValue())
                .withMnemonicText(swtSelector.getMnemonicText())
                .withTooltip(swtSelector.getTooltip())
                .withWidgetProperties(swtSelector.getWidgetProperties())
                .withData(swtSelector.getData())
                .withDataProperties(swtSelector.getDataProperties())
                .toMatcher();
    }

    @SuppressWarnings("unchecked")
    public MatcherBuilder withMatcher(Matcher<? extends Widget> matcher) {
        if (matcher != null) {
            this.matcher = allOf(this.matcher, matcher);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public MatcherBuilder withType(String type) {
        if (!isNullOrEmpty(type)) {
            matcher = allOf(matcher, WidgetOfType.instance(type));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public MatcherBuilder withId(String id) {
        if (!isNullOrEmpty(id)) {
            matcher = allOf(matcher, WidgetMatcherFactory.withId(id));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public MatcherBuilder withCustomId(String customIdKey, String customIdValue) {
        if (!isNullOrEmpty(customIdKey) && !isNullOrEmpty(customIdValue)) {
            matcher = allOf(matcher, WidgetMatcherFactory.withId(customIdKey, customIdValue));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public MatcherBuilder withLabel(String label) {
        if (!isNullOrEmpty(label)) {
            matcher = allOf(matcher, WidgetMatcherFactory.withLabel(label));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public MatcherBuilder withTooltip(String tooltip) {
        if (!isNullOrEmpty(tooltip)) {
            matcher = allOf(matcher, WidgetMatcherFactory.withTooltip(tooltip));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public MatcherBuilder withText(String text) {
        if (!isNullOrEmpty(text)) {
            matcher = allOf(matcher, WidgetMatcherFactory.withText(text));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public MatcherBuilder withMnemonicText(String text) {
        if (!isNullOrEmpty(text)) {
            matcher = allOf(matcher, WidgetMatcherFactory.withMnemonic(text));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
	public MatcherBuilder withData(final Object data) {
        if (data != null) {
            matcher = allOf(matcher, new WidgetDataMatcher(data));
        }
        return this;
	}

    public Matcher<Widget> toMatcher() {
        return matcher;
    }

    @SuppressWarnings("unchecked")
    public MatcherBuilder withDataProperties(Map<String, Object> dataProperties) {
        if (dataProperties != null) {
            matcher = allOf(matcher, new WidgetDataPropertyMatcher(dataProperties));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private MatcherBuilder withWidgetProperties(Map<String, String> widgetProperties) {
        if (widgetProperties != null && !widgetProperties.isEmpty()) {
            matcher = allOf(matcher, new WidgetPropertyMatcher(widgetProperties));
        }
        return this;
    }
}
