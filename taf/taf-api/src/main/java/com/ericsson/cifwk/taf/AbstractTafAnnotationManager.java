package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.annotations.TestId;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 17/11/2016
 */
abstract class AbstractTafAnnotationManager implements TafAnnotationManager {

    private static final String EMPTY_TEST_ID = "WARNING: EMPTY TEST ID";

    private  String testId = TestId.DEFAULT_ID;

    private String title = TestId.DEFAULT_TITLE;

    private final Collection<Annotation> annotations;

    public AbstractTafAnnotationManager(Collection<Annotation> annotations) {
        this(annotations, null, new Object[]{});
    }

    public AbstractTafAnnotationManager(Method method, Object[] parameters) {
        this(asList(method == null ? new Annotation[]{} : method.getAnnotations()), method, parameters);
    }

    private AbstractTafAnnotationManager(Collection<Annotation> annotations, Method method, Object[] parameters) {
        this.annotations = annotations;

        // overwriting Test ID from test method annotation
        if (isTestIdAnnotationPresent()) {
            TestId testIdAnnotation = getAnyAnnotation(TestId.class);
            testId = testIdAnnotation.id();
            title = testIdAnnotation.title();
        }

        // overwriting Test ID from test method parameter annotation
        String testIdFromParam = null;
        if (method != null && parameters.length > 0) {
            testIdFromParam = ParametersManager.getTestIdFromMethodParameters(method, parameters);
        }
        if (testIdFromParam != null) {
            testId = testIdFromParam;
        }

        // raising flag on empty Test ID
        if ("".equals(testId)) {
            testId = EMPTY_TEST_ID;
        }

        // setting title to test case ID
        if (isNullOrEmpty(title) || TestId.DEFAULT_TITLE.equals(title)) {
            title = testId;
        }
    }

    public String getTestId() {
        return testId;
    }

    public boolean isTestIdAnnotationPresent() {
        return isAnnotationPresent(TestId.class);
    }

    public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationType) {
        for (Annotation each : annotations) {
            if (each.annotationType().equals(annotationType)) {
                return true;
            }
        }
        return false;
    }

    public Annotation getAnyAnnotation(Class<? extends Annotation>... annotationTypes) {
        for (Class<? extends Annotation> annotationType : annotationTypes) {
            Annotation annotation = getAnyAnnotation(annotationType);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    public <T extends Annotation> T getAnyAnnotation(Class<T> annotationType) {
        for (Annotation each : annotations) {
            if (each.annotationType().equals(annotationType)) {
                return annotationType.cast(each);
            }
        }
        return null;
    }

    @Override
    public boolean isSetUpMethod() {
        return getSetUpAnnotation() != null;
    }

    @Override
    public boolean isTearDownMethod() {
        return getTearDownAnnotation() != null;
    }

    protected String getTitle() {
        return title;
    }

    public abstract String getTestCaseTitle();

    protected abstract String getMethodName();

    @Override
    public String getFixtureName() {
        Annotation configurationAnnotation = getFixtureAnnotation();
        String methodName = getMethodName();
        String fixture = configurationAnnotation.annotationType().getSimpleName();
        return "@" + fixture + ": " + methodName;
    }
}
