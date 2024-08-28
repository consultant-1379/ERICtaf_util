package com.ericsson.cifwk.taf;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.Description;

import java.lang.annotation.Annotation;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 17/11/2016
 */
public class TafJUnitAnnotationManager extends AbstractTafAnnotationManager {

    private Description description;

    public TafJUnitAnnotationManager(Description description) {
        super(description.getAnnotations());
        this.description = description;
    }

    @Override
    public String getTestCaseTitle() {
        return description.getDisplayName();
    }

    @Override
    protected String getMethodName() {
        return description.getMethodName();
    }

    @Override
    public Annotation getFixtureAnnotation() {
        return getAnyAnnotation(Before.class, BeforeClass.class, After.class, AfterClass.class);
    }

    @Override
    public Annotation getSetUpAnnotation() {
        return getAnyAnnotation(Before.class, BeforeClass.class);
    }

    @Override
    public Annotation getTearDownAnnotation() {
        return getAnyAnnotation(After.class, AfterClass.class);
    }

}
