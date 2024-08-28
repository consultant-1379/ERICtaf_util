package com.ericsson.cifwk.taf.datadriven;

import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.DataProviders;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 *
 */
public class DataDrivenAnnotationTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {
        if (testMethod != null && isDataDriven(testMethod)) {
            annotation.setDataProviderClass(GenericDataProvider.class);
            annotation.setDataProvider("default");
        }
    }

    private boolean isDataDriven(Method testMethod) {
        return testMethod.isAnnotationPresent(DataDriven.class) || testMethod.isAnnotationPresent(DataProviders.class);
    }

}
