package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtTable;
import org.eclipse.swtbot.swt.finder.SWTBot;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class TableFactory extends AbstractComponentFactory<SwtTable> {

    public TableFactory(SWTBot bot) {
        super(bot);
        registerFactory(new SWTBotTableFactory());
    }

}
