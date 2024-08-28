/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.swt.agent.introspection;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

@API(Stable)
public class WindowIntrospector {

    public WindowDto introspect(SWTBot bot) {
        WindowDto dto = new WindowDto();

        collect(dto, new ArrowButtonCollector(bot));
        collect(dto, new ButtonCollector(bot));
        collect(dto, new BrowserCollector(bot));
        collect(dto, new CheckBoxCollector(bot));
        collect(dto, new ComboBoxCollector(bot));
        collect(dto, new DateTimeCollector(bot));
        collect(dto, new ExpandBarCollector(bot));
        collect(dto, new LabelCollector(bot));
        collect(dto, new LinkCollector(bot));
        collect(dto, new ListCollector(bot));
        collect(dto, new RadioCollector(bot));
        collect(dto, new ScaleCollector(bot));
        collect(dto, new SliderCollector(bot));
        collect(dto, new SpinnerCollector(bot));
        collect(dto, new StyledTextCollector(bot));
        collect(dto, new TabItemCollector(bot));
        collect(dto, new TableCollector(bot));
        collect(dto, new TextBoxCollector(bot));
        collect(dto, new ToggleButtonCollector(bot));
        collect(dto, new TrayItemCollector(bot));
        collect(dto, new TreeCollector(bot));

        collect(dto, new CComboBoxCollector(bot));
        collect(dto, new CLabelCollector(bot));
        collect(dto, new CTabItemCollector(bot));

        collect(dto, new ToolbarDropDownButtonCollector(bot));
        collect(dto, new ToolbarRadioButtonCollector(bot));
        collect(dto, new ToolbarToggleButtonCollector(bot));

        return dto;
    }

    private void collect(WindowDto dto, WidgetCollector widgetCollector) {
        String type = widgetCollector.getType();
        List<WidgetDto> targetCollection = widgetCollector.getTarget(dto);
        int index = 0;
        while (true) {
            try {
                AbstractSWTBot<?> widget = widgetCollector.getWidgetByIndex(index);
                targetCollection.add(introspect(type, index++, widget));
            } catch (IndexOutOfBoundsException e) {
                break;
            } catch (WidgetNotFoundException e) {
                break;
            }
        }
    }

    private WidgetDto introspect(String type, int index, AbstractSWTBot<?> swtBotWidget) {
        String id = swtBotWidget.getId();
        String text = swtBotWidget.getText();
        String toolTipText = swtBotWidget.getToolTipText();
        WidgetDto dto = new WidgetDto(type, index, id, text, toolTipText);
        dto.setActive(swtBotWidget.isActive());
        dto.setEnabled(swtBotWidget.isEnabled());
        dto.setVisible(swtBotWidget.isVisible());
        return dto;
    }

}
