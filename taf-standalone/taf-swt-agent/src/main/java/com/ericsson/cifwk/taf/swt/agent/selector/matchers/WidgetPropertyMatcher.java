package com.ericsson.cifwk.taf.swt.agent.selector.matchers;

import com.ericsson.cifwk.meta.API;
import com.google.common.collect.Sets;
import org.apache.commons.beanutils.ConvertUtils;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.matchers.AbstractMatcher;
import org.hamcrest.Description;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class WidgetPropertyMatcher extends AbstractMatcher<Widget> {

    private static final String ERROR_WHILE_CHECKING_PROPERTIES = "Error while checking SWT widget properties";

    private static final Set<String> ignoredProperties = Sets.newHashSet("class", "data");

    private final Map<String, String> properties;

    public WidgetPropertyMatcher(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("with data '").appendValue(properties).appendText("'");
    }

    @Override
    protected boolean doMatch(Object obj) {
        try {
            Widget widget = (Widget) obj;
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String propertyName = entry.getKey();
                String entryValue = entry.getValue();
                Object propertyValueStr = "null".equals(entryValue) ? null : entryValue;
                BeanInfo beanInfo = Introspector.getBeanInfo(widget.getClass());
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    if (!ignoredProperties.contains(propertyName) && propertyName.equals(propertyDescriptor.getName())) {
                        Method readMethod = propertyDescriptor.getReadMethod();
                        if (readMethod != null) {
                            Class<?> returnType = readMethod.getReturnType();
                            Object propertyValue = ConvertUtils.convert(propertyValueStr, returnType);
                            Object realValue = readMethod.invoke(widget);
                            if (propertyValue == null && realValue == null) {
                                continue;
                            }
                            if (propertyValue == null || !propertyValue.equals(realValue)) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            log.error(ERROR_WHILE_CHECKING_PROPERTIES, e);
            throw new RuntimeException("Error while checking SWT object widget properties", e);
        }
    }

}
