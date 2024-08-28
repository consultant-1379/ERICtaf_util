package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.swt.agent.selector.SwtWidgetFinder;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.hamcrest.Matcher;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.anyOf;

@API(Internal)
public abstract class AbstractComponentFactory<T extends UiComponent> {

    private SwtWidgetFinder finder;

    private List<SWTBotAbstractComponentFactory<?, ?, ?>> factories = Lists.newArrayList();

    public AbstractComponentFactory(SWTBot bot) {
        finder = new SwtWidgetFinder(bot);
    }

    protected void registerFactory(SWTBotAbstractComponentFactory<?, ?, ? extends T> factory) {
        factories.add(factory);
    }

    protected List<SWTBotAbstractComponentFactory<?, ?, ?>> getFactories() {
        return factories;
    }

    public T create(String selector) {
        Preconditions.checkState(!factories.isEmpty(), "Please register at least one SWT Bot component factory");
        int index = factories.iterator().next().getIndex(selector);

        // combining matchers
        List<Matcher<? extends Widget>> matchers = Lists.newArrayList();
        for (SWTBotAbstractComponentFactory<?, ?, ?> factory : factories) {
            matchers.add(factory.getMatcher(selector));
        }
        Matcher<Widget> matcher = anyOf(matchers);

        // selecting widget
        List<Widget> foundComponents = findComponents(matcher);
        if (foundComponents.size() < index + 1) {
            return null;
        }
        return create(foundComponents.get(index));
    }

    protected T create(Widget widget) {
        // selecting factory for component creation
        for (SWTBotAbstractComponentFactory<?, ?, ?> genericFactory : factories) {
            if (genericFactory.canCreate(widget)) {
                @SuppressWarnings("unchecked")
                SWTBotAbstractComponentFactory<Widget, AbstractSWTBot<Widget>, T> factory = (SWTBotAbstractComponentFactory<Widget, AbstractSWTBot<Widget>, T>) genericFactory;
                AbstractSWTBot<Widget> swtBotComponent = factory.create(widget);
                return factory.create(swtBotComponent);
            }
        }
        return null;
    }

    private List<Widget> findComponents(Matcher<Widget> matcher) {
        return finder.findComponents(matcher);
    }

}
