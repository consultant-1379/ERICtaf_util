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
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class ToolbarDropDownButtonCollector implements WidgetCollector {

    private SWTBot bot;

    public ToolbarDropDownButtonCollector(SWTBot bot) {
        this.bot = bot;
    }

    @Override
    public String getType() {
        return "toolbarDropDownButton";
    }

    @Override
    public List<WidgetDto> getTarget(WindowDto container) {
        return container.getToolbarDropDownButtons();
    }

    @Override
    public AbstractSWTBot<?> getWidgetByIndex(int index) {
        return bot.toolbarDropDownButton(index);
    }
}
