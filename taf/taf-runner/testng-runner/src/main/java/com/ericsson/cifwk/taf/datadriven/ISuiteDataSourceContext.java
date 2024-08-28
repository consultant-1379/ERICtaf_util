package com.ericsson.cifwk.taf.datadriven;

import com.ericsson.cifwk.taf.datasource.DataSourceContext;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import org.testng.ISuite;

import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 */
public class ISuiteDataSourceContext implements DataSourceContext {

    private static final String ATTRIBUTE_DATA_SOURCE = "dataProvider";

    private final ISuite suite;
    private final Method method;

    public ISuiteDataSourceContext(ISuite suite, Method testMethod) {
        this.suite = suite;
        this.method = testMethod;
    }

    @Override
    public Map<String, TestDataSource> getDataSources() {
         String attributeName = attributeName();
         return (Map<String, TestDataSource>) suite.getAttribute(attributeName);
    }

    @Override
    public void setDataSources(Map<String, TestDataSource> dataSources) {
        synchronized (suite) {
            Map<String, TestDataSource> existing = getDataSources();
            if (existing == null) {
                suite.setAttribute(attributeName(), dataSources);
            } else {
                existing.putAll(dataSources);
            }
        }
    }

    private String attributeName() {
        return String.format("%s.%s.%s", ATTRIBUTE_DATA_SOURCE, method.getDeclaringClass().getName(), method.getName());
    }

}
