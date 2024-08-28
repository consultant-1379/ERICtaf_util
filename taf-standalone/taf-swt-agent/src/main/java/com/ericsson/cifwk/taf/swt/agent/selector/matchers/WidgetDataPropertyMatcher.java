package com.ericsson.cifwk.taf.swt.agent.selector.matchers;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.matchers.AbstractMatcher;
import org.hamcrest.Description;

import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class WidgetDataPropertyMatcher extends AbstractMatcher<Widget> {

    private final Map<String, Object> data;

    public WidgetDataPropertyMatcher(Map<String, Object> data) {
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
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Object dataProperty = widget.getData(entry.getKey());
                if (dataProperty == null || !dataProperty.equals(entry.getValue())) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Error while checking SWT object data properties", e);
            throw new RuntimeException("Error while checking SWT object data properties", e);
        }
    }

}
