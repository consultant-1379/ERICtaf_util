package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResultImpl;
import com.ericsson.cifwk.taf.testapi.utils.TestResultHelper;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 17/11/2016
 */
public class TafTestNGAnnotationManager extends AbstractTafAnnotationManager {

    private final Method method;

    private Object[] parameters;

    public TafTestNGAnnotationManager(ITestResult iTestResult) {
        this(TestResultHelper.getMethod(new TestMethodExecutionResultImpl(iTestResult)), iTestResult.getParameters());
    }

    public TafTestNGAnnotationManager(Method method, Object[] parameters) {
        super(method, parameters);
        this.method = method;
        this.parameters = parameters;
    }

    @Override
    public Annotation getFixtureAnnotation() {
        return getAnyAnnotation(BeforeMethod.class, BeforeClass.class, BeforeTest.class, BeforeSuite.class, AfterMethod.class, AfterClass.class, AfterTest.class, AfterSuite.class);
    }

    @Override
    public Annotation getSetUpAnnotation() {
        return getAnyAnnotation(BeforeMethod.class, BeforeClass.class, BeforeTest.class, BeforeSuite.class);
    }

    @Override
    public Annotation getTearDownAnnotation() {
        return getAnyAnnotation(AfterMethod.class, AfterClass.class, AfterTest.class, AfterSuite.class);
    }

    @Override
    protected String getMethodName() {
        return getMethod().getName();
    }

    public String getTestCaseTitle() {
        return ParametersManager.getParametrizedName(getTitle(), parameters);
    }

    private Method getMethod() {
        return method;
    }

}
