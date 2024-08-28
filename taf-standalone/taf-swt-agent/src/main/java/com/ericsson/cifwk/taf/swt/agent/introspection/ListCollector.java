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
public class ListCollector implements WidgetCollector {

    private SWTBot bot;

    public ListCollector(SWTBot bot) {
        this.bot = bot;
    }

    @Override
    public String getType() {
        return "list";
    }

    @Override
    public List<WidgetDto> getTarget(WindowDto container) {
        return container.getLists();
    }

    @Override
    public AbstractSWTBot<?> getWidgetByIndex(int index) {
        return bot.list(index);
    }
}
