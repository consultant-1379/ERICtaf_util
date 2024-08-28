package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.sdk.SwtControl;
import com.ericsson.cifwk.taf.ui.sdk.SwtUiComponent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class UiComponentFactory extends AbstractComponentFactory<UiComponent> {

    public UiComponentFactory(SWTBot bot) {
        super(bot);
        registerFactory(new ButtonFactory(bot));
        registerFactory(new CheckBoxFactory(bot));
        registerFactory(new LabelFactory(bot));
        registerFactory(new LinkFactory(bot));
        registerFactory(new RadioButtonFactory(bot));
        registerFactory(new SelectFactory(bot));
        registerFactory(new TextBoxFactory(bot));
        registerFactory(new TableFactory(bot));
    }

    @Override
    public UiComponent create(Widget widget) {
        UiComponent result = super.create(widget);
        return result == null ? createGenericComponent(widget) : result;
    }

    private UiComponent createGenericComponent(Widget widget) {
        if (Control.class.isAssignableFrom(widget.getClass())) {
            Control control = (Control) widget;
            return new SwtControl(new SWTBotControl(control));
        }
        return new SwtUiComponent(new SWTBotWidget(widget));
    }

    private void registerFactory(AbstractComponentFactory<? extends UiComponent> factory) {
        List<SWTBotAbstractComponentFactory<?, ?, ?>> factories = factory.getFactories();
        for (SWTBotAbstractComponentFactory<?, ?, ?> swtBotFactory : factories) {
            registerFactory(swtBotFactory);
        }
    }

    private static class SWTBotControl extends AbstractSWTBotControl<Control> {

        public SWTBotControl(Control widget) throws WidgetNotFoundException {
            super(widget);
        }

    }

    private static class SWTBotWidget extends AbstractSWTBot<Widget> {

        public SWTBotWidget(Widget widget) throws WidgetNotFoundException {
            super(widget);
        }

    }

}
