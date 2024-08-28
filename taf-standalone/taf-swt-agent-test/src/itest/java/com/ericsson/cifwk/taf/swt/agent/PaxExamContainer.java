/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.swt.agent;

import com.google.common.base.Throwables;
import org.ops4j.pax.exam.junit.PaxExamServer;
import org.ops4j.pax.exam.nat.internal.NativeTestContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class PaxExamContainer {

    private PaxExamServer paxExamServer;

    public PaxExamContainer(Class<?> configurationClass) {
        paxExamServer = new PaxExamServer(configurationClass);
    }

    public void start() {
        invoke(paxExamServer, "before");
    }

    public void stop() {
        invoke(paxExamServer, "after");
    }

    public void startBundle(String bundleId) {
        Bundle bundle = getBundle(bundleId);
        try {
            bundle.start();
        } catch (BundleException e) {
            Throwables.propagate(e);
        }
        sleep(1000);
        while(Bundle.ACTIVE != bundle.getState()){
            sleep(100);
        }
    }

    public void stopBundle(String bundleId) {
        Bundle bundle = getBundle(bundleId);
        try {
            bundle.stop();
        } catch (BundleException e) {
            Throwables.propagate(e);
        }
        sleep(1000);
        while(Bundle.ACTIVE == bundle.getState()){
            sleep(100);
        }
    }

    public Bundle getBundle(String bundleId) {
        Bundle[] bundles = getBundleContext().getBundles();
        for(Bundle bundle : bundles) {
            if (bundle.getSymbolicName().equals(bundleId)) {
                return bundle;
            }
        }
        return null;
    }

    private BundleContext getBundleContext() {
        NativeTestContainer testContainer = getField(paxExamServer, "testContainer");
        Framework framework = getField(testContainer, "framework");
        return framework.getBundleContext();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    private void invoke(Object target, String methodName) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(target);
        } catch (NoSuchMethodException e) {
            Throwables.propagate(e);
        } catch (IllegalAccessException e) {
            Throwables.propagate(e);
        } catch (InvocationTargetException e) {
            Throwables.propagate(e);
        }
    }

    private <T> T getField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (NoSuchFieldException e) {
            throw Throwables.propagate(e);
        } catch (IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

}
