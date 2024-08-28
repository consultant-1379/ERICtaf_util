package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.datadriven.DataDrivenAnnotationTransformer;
import com.ericsson.cifwk.taf.execution.TestNGAnnotationTransformer;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Current version of TestNG supports only one instance of AnnotationTransformer
 * https://github.com/cbeust/testng/issues/270
 * To workaround this feature-bug composite version is created.
 */
public class CompositeAnnotationTransformer implements IAnnotationTransformer {

    private final List<IAnnotationTransformer> transformers = new ArrayList<>();

    public CompositeAnnotationTransformer() {
        addAnnotationTransformer(new TestNGAnnotationTransformer());
        addAnnotationTransformer(new DataDrivenAnnotationTransformer());
    }

    public void addAnnotationTransformer(IAnnotationTransformer transformer) {
        transformers.add(transformer);
    }

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        for (IAnnotationTransformer transformer : transformers) {
            transformer.transform(annotation, testClass, testConstructor, testMethod);
        }
    }
}
