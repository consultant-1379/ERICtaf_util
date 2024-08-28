package com.ericsson.cifwk.taf.scenario.impl;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import java.lang.reflect.Method;

import com.ericsson.cifwk.meta.API;

/**
 *
 */
@API(Internal)
public final class InstanceMethodInvocation extends MethodInvocation {

    private final Object instance;

    private final Method method;

    public InstanceMethodInvocation(Object instance, Method method, String testStepName) {
        super(testStepName);
        this.instance = instance;
        this.method = method;
    }

    @Override
    protected Object getInstance() {
        return instance;
    }

    @Override
    protected Method getMethod() {
        return method;
    }

}
