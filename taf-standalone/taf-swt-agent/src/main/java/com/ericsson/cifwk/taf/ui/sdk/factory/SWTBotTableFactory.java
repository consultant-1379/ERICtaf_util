package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtTable;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

@API(Internal)
public class SWTBotTableFactory extends SWTBotAbstractComponentFactory<Table, SWTBotTable, SwtTable> {

    @Override
    protected Matcher<Table> getBaseMatcher() {
        return widgetOfType(Table.class);
    }

    @Override
    public SWTBotTable create(Table widget) {
        return new SWTBotTable(widget);
    }

    @Override
    protected SwtTable create(SWTBotTable swtBotWidget) {
        return new SwtTable(swtBotWidget);
    }
}
