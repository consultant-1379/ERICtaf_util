package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.google.common.collect.Maps;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.ParametersManager.getMethodParameterNamesIfAvailable;
import static com.ericsson.cifwk.taf.ParametersManager.getNameForParameter;
import static com.ericsson.cifwk.taf.ParametersManager.getParameterName;

/**
 * Class holds basic properties of test case including ID and named parameters.
 */
@API(Internal)
public class TestCaseBean {

    private Map<String, Object> parameters = Maps.newLinkedHashMap();

    private String testId;

    private String title;

    public TestCaseBean(Object[] parameters, Method method) {
        TafAnnotationManagerFactory tafAnnotationManagerFactory = ServiceRegistry.getTafAnnotationManagerFactory();
        TafAnnotationManager annotationManager = tafAnnotationManagerFactory.create(method, parameters);
        this.testId = annotationManager.getTestId();
        this.title = annotationManager.getTestCaseTitle();

        Annotation[][] parametersAnnotations = method.getParameterAnnotations();
        List<String> methodParameterNames = getMethodParameterNamesIfAvailable(method);
        for (int i = 0; i < parameters.length; i++) {
            String parameterName = getParameterName(parametersAnnotations[i]);
            if (parameterName == null) {
                return;
            }
            String name = getNameForParameter(parameterName, methodParameterNames, i);
            this.parameters.put(name, parameters[i]);
        }
    }

    public TestCaseBean(String testId,  LinkedHashMap<String, DataRecord> dataSourcesRecords) { // NOSONAR
        this.testId = testId;
        for (DataRecord dataRecord : dataSourcesRecords.values()) {
            parameters.putAll(dataRecord.getAllFields());
        }
    }

    public TestCaseBean(String testId, String testTitle,  LinkedHashMap<String, DataRecord> dataSourcesRecords) { // NOSONAR
        this.testId = testId;
        this.title = testTitle;
        for (final DataRecord dataRecord : dataSourcesRecords.values()) {
            parameters.putAll(dataRecord.getAllFields());
        }
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String getTestId() {
        return testId;
    }

    public String getTitle() {
        return title;
    }
}
