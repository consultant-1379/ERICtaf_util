package com.ericsson.cifwk.taf.swt.agent.selector.matchers;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.matchers.AbstractMatcher;
import org.hamcrest.Description;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class WidgetDataMatcher extends AbstractMatcher<Widget> {

    private final Object data;

    public WidgetDataMatcher(Object data) {
        this.data = data;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("with data '").appendValue(data).appendText("'");
    }

    @Override
    protected boolean doMatch(Object obj) {
        try {
            Widget widget = (Widget) obj;
            return data.equals(widget.getData());
        } catch (Exception e) {
            log.error("Error while checking SWT object data", e);
            return false;
        }
    }

}
