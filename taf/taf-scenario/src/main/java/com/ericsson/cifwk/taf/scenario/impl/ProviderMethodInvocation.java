package com.ericsson.cifwk.taf.scenario.impl;

import javax.inject.Provider;
import java.lang.reflect.Method;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.scenario.api.TestStepDefinition.findAnnotatedMethod;

import com.ericsson.cifwk.meta.API;

/**
 *
 */
@API(Internal)
public final class ProviderMethodInvocation extends MethodInvocation {

    private final Provider provider;

    private final String testStepName;

    public ProviderMethodInvocation(Provider provider, String testStepName) {
        super(testStepName);
        this.provider = provider;
        this.testStepName = testStepName;
    }

    @Override
    protected Object getInstance() {
        return provider.get();
    }

    @Override
    protected Method getMethod() {
        return findAnnotatedMethod(getInstance(), testStepName);
    }

}
