package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtMenuItem extends UiComponentAdapter implements MenuItem {

    private SWTBotMenu target;

    public SwtMenuItem(SWTBotMenu target) {
        this.target = target;
    }

    @Override
    public void click() {
        target.click();
    }

    @Override
    public MenuItem getMenuItem(String menuName) {
        return new SwtMenuItem(target.menu(menuName));
    }

}
