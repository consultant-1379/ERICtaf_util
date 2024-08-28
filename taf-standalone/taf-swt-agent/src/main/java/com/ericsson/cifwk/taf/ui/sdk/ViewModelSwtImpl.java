package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.swt.agent.selector.GeneralSelectorParser;
import com.ericsson.cifwk.taf.swt.agent.selector.MatcherBuilder;
import com.ericsson.cifwk.taf.swt.agent.selector.SwtSelector;
import com.ericsson.cifwk.taf.ui.UI;
import com.ericsson.cifwk.taf.ui.core.GenericPredicate;
import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.WaitTimedOutException;
import com.ericsson.cifwk.taf.ui.sdk.factory.ButtonFactory;
import com.ericsson.cifwk.taf.ui.sdk.factory.CheckBoxFactory;
import com.ericsson.cifwk.taf.ui.sdk.factory.LabelFactory;
import com.ericsson.cifwk.taf.ui.sdk.factory.LinkFactory;
import com.ericsson.cifwk.taf.ui.sdk.factory.RadioButtonFactory;
import com.ericsson.cifwk.taf.ui.sdk.factory.SelectFactory;
import com.ericsson.cifwk.taf.ui.sdk.factory.TableFactory;
import com.ericsson.cifwk.taf.ui.sdk.factory.TextBoxFactory;
import com.ericsson.cifwk.taf.ui.sdk.factory.UiComponentFactory;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.internal.ReflectionInvoker;
import org.hamcrest.Matcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.base.Preconditions.checkArgument;
import static org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable.syncExec;

@API(Internal)
public class ViewModelSwtImpl extends ViewModelAdapter {

    private SWTBot swtBot;

    private GeneralSelectorParser selectorParser = new GeneralSelectorParser();

    public ViewModelSwtImpl(SWTBot swtBot) {
        this.swtBot = swtBot;
    }

    @Override
    public Button getButton(String selector) {
        return getButton(SelectorType.DEFAULT, selector);
    }

    @Override
    public Button getButton(SelectorType selectorType, String selector) {
        verifyDefaultSelector(selectorType);
        return new ButtonFactory(swtBot).create(selector);
    }

    @Override
    public CheckBox getCheckBox(String selector) {
        return getCheckBox(SelectorType.DEFAULT, selector);
    }

    @Override
    public CheckBox getCheckBox(SelectorType selectorType, String selector) {
        verifyDefaultSelector(selectorType);
        return new CheckBoxFactory(swtBot).create(selector);
    }

    public Table getTable(String selector) {
        return new TableFactory(swtBot).create(selector);
    }

    @Override
    public Label getLabel(String selector) {
        return getLabel(SelectorType.DEFAULT, selector);
    }

    @Override
    public Label getLabel(SelectorType selectorType, String selector) {
        verifyDefaultSelector(selectorType);
        return new LabelFactory(swtBot).create(selector);
    }

    @Override
    public Link getLink(String selector) {
        return getLink(SelectorType.DEFAULT, selector);
    }

    @Override
    public Link getLink(SelectorType selectorType, String selector) {
        verifyDefaultSelector(selectorType);
        return new LinkFactory(swtBot).create(selector);
    }

    @Override
    public RadioButton getRadioButton(String selector) {
        return getRadioButton(SelectorType.DEFAULT, selector);
    }

    @Override
    public RadioButton getRadioButton(SelectorType selectorType, String selector) {
        verifyDefaultSelector(selectorType);
        return new RadioButtonFactory(swtBot).create(selector);
    }

    @Override
    public Select getSelect(String selector) {
        return getSelect(SelectorType.DEFAULT, selector);
    }

    @Override
    public Select getSelect(SelectorType selectorType, String selector) {
        verifyDefaultSelector(selectorType);
        return new SelectFactory(swtBot).create(selector);
    }

    @Override
    public TextBox getTextBox(String selector) {
        return getTextBox(SelectorType.DEFAULT, selector);
    }

    @Override
    public TextBox getTextBox(SelectorType selectorType, String selector) {
        verifyDefaultSelector(selectorType);
        return new TextBoxFactory(swtBot).create(selector);
    }

    protected MenuItem getMenuItem(String menuName) {
        return new SwtMenuItem(swtBot.menu(menuName));
    }

    @Override
    public UiComponent getViewComponent(String selector) {
        SwtSelector swtSelector = selectorParser.parse(selector);
        List<UiComponent> components = findComponents(swtSelector);
        return components.isEmpty() ? null : components.iterator().next();
    }

    @Override
    public <T extends UiComponent> T getViewComponent(String selector, Class<T> componentClass) {
        return getViewComponent(SelectorType.DEFAULT, selector, componentClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends UiComponent> T getViewComponent(SelectorType selectorType, String selector, Class<T> componentClass) {
        String methodName = "get" + componentClass.getSimpleName();
        try {
            Method declaredMethod = this.getClass().getDeclaredMethod(methodName, String.class);
            return (T) declaredMethod.invoke(this, selector);
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public <T extends UiComponent> List<T> getViewComponents(String selector, Class<T> componentClass) {
        return getViewComponents(SelectorType.DEFAULT, selector, componentClass);
    }

    @Override
    public <T extends UiComponent> List<T> getViewComponents(SelectorType selectorType, String selector, Class<T> componentClass) {
        SwtSelector swtSelector = selectorParser.parse(selector);
        swtSelector.setIndex(-1);
        List<UiComponent> uiComponents = findComponents(swtSelector);
        List<T> result = Lists.newArrayList();
        for (UiComponent uiComponent : uiComponents) {
            if (componentClass.isAssignableFrom(uiComponent.getClass())) {
                result.add((T) uiComponent);
            }
        }
        return result;
    }

    private void verifyDefaultSelector(SelectorType selectorType) {
        checkArgument(SelectorType.DEFAULT.equals(selectorType));
    }

    private List<UiComponent> findComponents(SwtSelector swtSelector) {

        // building matcher
        Matcher<Widget> matcher = new MatcherBuilder(swtSelector).toMatcher();

        // searching for widget
        int index = swtSelector.getIndex();
        List<Widget> foundControls = swtBot.getFinder().findControls(matcher);

        // single control required?
        if (index >= 0) {

            // getting by index
            if (foundControls.size() < index + 1) {
                return Lists.newArrayList();
            }
            Widget widget = foundControls.get(index);
            foundControls = Arrays.asList(widget);

            // init actions if single control requested
            init(swtSelector, widget);
        }

        // wrapping to according TAF UI API component
        return convert(swtSelector, foundControls);
    }

    protected void init(SwtSelector swtSelector, Widget widget) {

        // wrapping if required
        String[] initActions = swtSelector.getInitActions();
        if (initActions != null) {
            String wrapperType = swtSelector.getWrapperType();
            Object wrapper = wrapToSwtBotComponent(widget, wrapperType);

            // initializing actions
            for (String initAction : initActions) {
                syncExec(widget.getDisplay(), new ReflectionInvoker(wrapper, initAction));
            }
        }
    }

    protected Object wrapToSwtBotComponent(Object widget, String wrapperType) {
        if (wrapperType == null) {
            wrapperType = getSwtBotWrapperClassName(widget.getClass());
        }

        try {
            Class<?> wrapperClass = Class.forName(wrapperType);
            Constructor<?>[] constructors = wrapperClass.getConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(widget.getClass())) {
                    return constructor.newInstance(widget);
                }
            }
            return widget;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    protected String getSwtBotWrapperClassName(Class<?> widgetClass) {
        return "org.eclipse.swtbot.swt.finder.widgets.SWTBot" + widgetClass.getSimpleName();
    }

    private List<UiComponent> convert(SwtSelector swtSelector, List<Widget> foundControls) {
        List<UiComponent> result = Lists.newArrayList();
        for (Widget widget : foundControls) {
            UiComponent uiComponent = convert(widget, swtSelector);
            if (uiComponent != null) {
                result.add(uiComponent);
            }
        }
        return result;
    }

    private UiComponent convert(Widget widget, SwtSelector swtSelector) {
        if (swtSelector.isNativeWidget()) {
            return new WidgetWrapper(widget);
        }
        return new UiComponentFactory(swtBot).create(widget);
    }

    @Override
    public UiComponent waitUntilComponentIsDisplayed(String selector) {
        return waitUntilComponentIsDisplayed(selector, UI.getDefaultWaitTimeout());
    }

    @Override
    public UiComponent waitUntilComponentIsDisplayed(SelectorType selectorType, String selector) {
        return waitUntilComponentIsDisplayed(selectorType, selector, UI.getDefaultWaitTimeout());
    }

    @Override
    public UiComponent waitUntilComponentIsDisplayed(UiComponent component) {
        return waitUntilComponentIsDisplayed(component, UI.getDefaultWaitTimeout());
    }

    @Override
    public UiComponent waitUntilComponentIsHidden(UiComponent component) {
        return waitUntilComponentIsHidden(component, UI.getDefaultWaitTimeout());
    }

    @Override
    public <C extends UiComponent> C waitUntil(C component, Predicate<C> predicate) throws WaitTimedOutException {
        return waitUntil(component, predicate, UI.getDefaultWaitTimeout());
    }

    @Override
    public void waitUntil(GenericPredicate predicate) throws WaitTimedOutException {
        waitUntil(predicate, UI.getDefaultWaitTimeout());
    }

}
