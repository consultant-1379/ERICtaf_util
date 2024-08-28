package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.UiToolkit;
import com.ericsson.cifwk.taf.ui.core.GenericPredicate;
import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMappingDetails;
import com.ericsson.cifwk.taf.ui.core.UiComponentSelector;
import com.ericsson.cifwk.taf.ui.core.UiComponentSize;
import com.ericsson.cifwk.taf.ui.core.WaitHelper;
import com.ericsson.cifwk.taf.ui.spi.ScreenshotProvider;
import com.ericsson.cifwk.taf.ui.spi.UiActions;
import com.ericsson.cifwk.taf.ui.spi.UiComponentStateManager;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.StringResult;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import java.lang.reflect.Method;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtUiComponent implements UiComponent {

    private AbstractSWTBot<?> target;

    private WaitHelper waitHelper = new WaitHelper(ScreenshotProvider.NO_SCREENSHOT_AVAILABLE);

    public SwtUiComponent(AbstractSWTBot<?> target) {
        this.target = target;
    }

    @Override
    public String getId() {
        return target.getId();
    }

    @Override
    public String getComponentName() {
        return target.toString();
    }

    @Override
    public boolean isDisplayed() {
        return target.isVisible();
    }

    @Override
    public boolean exists() {
        return UIThreadRunnable.syncExec(target.display, new BoolResult() {
            @Override
            public Boolean run() {
                return !target.widget.isDisposed();
            }
        });
    }

    @Override
    public boolean isSelected() {
        return target.isActive();
    }

    @Override
    public boolean isEnabled() {
        return target.isEnabled();
    }

    @Override
    public void click() {
        focus();

        try {
            // trying to call click() on target object
            UIThreadRunnable.syncExec(target.widget.getDisplay(), new ReflectionInvoker(target, "click"));
        } catch (Exception ignored) {
            // nothing to do
        }
    }

    @Override
    public void contextClick() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getText() {
        return target.getText();
    }

    @Override
    public void focus() {
        target.setFocus();
    }

    @Override
    public boolean hasFocus() {
        return target.isActive();
    }

    @Override
    public String getProperty(final String propertyName) {
        return UIThreadRunnable.syncExec(target.display, new StringResult() {
            @Override
            public String run() {
                return target.widget.getData(propertyName).toString();
            }
        });
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        for (CharSequence charSequence : keysToSend) {
            for (Character character : charSequence.toString().toCharArray()) {
                if (hasKeystroke(character)) {
                    try {
                        pressShortcut(KeyStroke.getInstance(String.valueOf(character)));
                    } catch (ParseException ignored) {
                        //
                    }
                } else {
                    pressShortcut(character);
                }
            }
        }
    }

    private boolean hasKeystroke(Character character) {
        return Character.isISOControl(character) && !character.equals('\r') && !character.equals('\n');
    }

    protected void pressShortcut(char character) {
        target.pressShortcut(0, character);
    }

    protected void pressShortcut(KeyStroke keyStroke) {
        target.pressShortcut(keyStroke);
    }

    @Override
    public void mouseOver(int... coordinates) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mouseDown(int... coordinates) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mouseUp(int... coordinates) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mouseMove(int... coordinates) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void mouseOut() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UiComponentSize getSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<UiComponent> getChildren() {
        return UIThreadRunnable.syncExec(target.display, new Result<List<UiComponent>>() {
            @Override
            public List<UiComponent> run() {
                if (target.widget instanceof Composite) {
                    Control[] children = ((Composite) target.widget).getChildren();
                    // TODO: MVO: find factories by their base matchers and wrap controls;
                }
                throw new UnsupportedOperationException();
            }
        });
    }

    @Override
    public <T extends UiComponent> Optional<T> getFirstDescendantBySelector(final String s, final Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends UiComponent> Optional<T> getFirstDescendantBySelector(final SelectorType selectorType, final String s, final Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MenuItem getMenuItem(String menuName) {
        try {
            SWTBotMenu contextMenu = target.contextMenu(menuName);
            return new SwtMenuItem(contextMenu);
        } catch (TimeoutException e) {
            return null;
        }
    }

    @Override
    public List<UiComponent> getDescendantsBySelector(String selector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends UiComponent> List<T> getDescendantsBySelector(final String s, final Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<UiComponent> getDescendantsBySelector(SelectorType selectorType, String selector) {
        // TODO: MVO: can be searched recursively for children, filtered by given selector and wrapped back to UI API components with factories
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends UiComponent> List<T> getDescendantsBySelector(final SelectorType selectorType, final String s, final Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UiComponentSelector getComponentSelector() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UiComponentMappingDetails getComponentDetails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends UiComponent> T as(java.lang.Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStateManager(UiComponentStateManager stateManager) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void waitUntil(GenericPredicate condition, long timeoutInMillis) {
        waitHelper.waitUntil(condition, timeoutInMillis);
    }

    @Override
    public void waitUntil(GenericPredicate condition) {
        waitHelper.waitUntil(condition, UiToolkit.getDefaultWaitTimeout());
    }

    @Override
    public <C extends UiComponent> void waitUntil(C component, Predicate<C> condition, long timeoutInMillis) {
        waitHelper.waitUntil(component, condition, timeoutInMillis);
    }

    @Override
    public <C extends UiComponent> void waitUntil(C component, Predicate<C> condition) {
        waitHelper.waitUntil(component, condition, UiToolkit.getDefaultWaitTimeout());
    }

    final class ReflectionInvoker implements StringResult {

        private final Object widget;
        private final String methodName;

        public ReflectionInvoker(Object widget, String methodName) {
            this.widget = widget;
            this.methodName = methodName;
        }

        @Override
        public String run() {
            try {
                Method method = widget.getClass().getMethod(methodName, new Class[] {});
                method.setAccessible(true);
                Object invoke = method.invoke(widget, new Object[0]);
                if (invoke != null) {
                    return invoke.toString();
                }
                return null;
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    @Override
    public UiActions createUiActions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evaluate(String s) {
        throw new UnsupportedOperationException();
    }
}
