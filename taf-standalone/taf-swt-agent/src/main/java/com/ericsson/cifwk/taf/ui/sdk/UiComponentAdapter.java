package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.core.GenericPredicate;
import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMappingDetails;
import com.ericsson.cifwk.taf.ui.core.UiComponentSelector;
import com.ericsson.cifwk.taf.ui.core.UiComponentSize;
import com.ericsson.cifwk.taf.ui.spi.UiActions;
import com.ericsson.cifwk.taf.ui.spi.UiComponentStateManager;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class UiComponentAdapter implements UiComponent {

    @Override
    public void click() {
        throw unsupported();
    }

    @Override
    public void contextClick() {
        throw unsupported();
    }

    @Override
    public List<UiComponent> getDescendantsBySelector(String selector) {
        throw unsupported();
    }

    @Override
    public <T extends UiComponent> List<T> getDescendantsBySelector(final String s, final Class<T> aClass) {
        throw unsupported();
    }

    @Override
    public <T extends UiComponent> T as(Class<T> clazz) {
        throw unsupported();
    }

    @Override
    public void setStateManager(UiComponentStateManager stateManager) {
        throw unsupported();
    }

    @Override
    public void waitUntil(GenericPredicate condition, long timeoutInMillis) {
        throw unsupported();
    }

    @Override
    public void waitUntil(GenericPredicate condition) {
        throw unsupported();
    }

    @Override
    public <C extends UiComponent> void waitUntil(C component, Predicate<C> condition, long timeoutInMillis) {
        throw unsupported();
    }

    @Override
    public <C extends UiComponent> void waitUntil(C component, Predicate<C> condition) {
        throw unsupported();
    }

    @Override
    public boolean exists() {
        throw unsupported();
    }

    @Override
    public void focus() {
        throw unsupported();
    }

    @Override
    public boolean hasFocus() {
        throw unsupported();
    }

    @Override
    public List<UiComponent> getChildren() {
        throw unsupported();
    }

    @Override
    public <T extends UiComponent> Optional<T> getFirstDescendantBySelector(final String s, final Class<T> aClass) {
        throw unsupported();
    }

    @Override
    public <T extends UiComponent> Optional<T> getFirstDescendantBySelector(final SelectorType selectorType, final String s, final Class<T> aClass) {
        throw unsupported();
    }

    @Override
    public String getComponentName() {
        throw unsupported();
    }

    @Override
    public UiComponentSelector getComponentSelector() {
        throw unsupported();
    }

    @Override
    public UiComponentMappingDetails getComponentDetails() {
        return null;
    }

    @Override
    public List<UiComponent> getDescendantsBySelector(SelectorType selectorType, String selector) {
        throw unsupported();
    }

    @Override
    public <T extends UiComponent> List<T> getDescendantsBySelector(final SelectorType selectorType, final String s, final Class<T> aClass) {
        throw unsupported();
    }

    @Override
    public String getId() {
        throw unsupported();
    }

    @Override
    public String getProperty(String propertyName) {
        throw unsupported();
    }

    @Override
    public UiComponentSize getSize() {
        throw unsupported();
    }

    @Override
    public String getText() {
        throw unsupported();
    }

    @Override
    public boolean isDisplayed() {
        throw unsupported();
    }

    @Override
    public boolean isEnabled() {
        throw unsupported();
    }

    @Override
    public boolean isSelected() {
        throw unsupported();
    }

    @Override
    public void mouseDown(int... coordinates) {
        throw unsupported();
    }

    @Override
    public void mouseMove(int... coordinate) {
        throw unsupported();
    }

    @Override
    public void mouseOut() {
        throw unsupported();
    }

    @Override
    public void mouseOver(int... coordinate) {
        throw unsupported();
    }

    @Override
    public void mouseUp(int... coordinate) {
        throw unsupported();
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        throw unsupported();
    }

    @Override
    public MenuItem getMenuItem(String menuName) {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UiActions createUiActions() {
        throw unsupported();
    }

    @Override
    public Object evaluate(String s) {
        throw unsupported();
    }

}