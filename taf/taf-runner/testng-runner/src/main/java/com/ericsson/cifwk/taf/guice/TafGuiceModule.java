package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Eager;
import com.ericsson.cifwk.taf.annotations.VUserScoped;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.execution.TestStepExecutor;
import com.ericsson.cifwk.taf.execution.TestStepExecutorImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * TAF customization for Dependency injection.
 * Supports eager singletons and allows to inject TestContext and TafConfiguration
 */
@API(Internal)
public final class TafGuiceModule extends AbstractModule {

    private Set<Class> classes;

    public TafGuiceModule() {
        this(Collections.<Class>emptySet());
    }

    public TafGuiceModule(Set<Class> classes) {
        this.classes = classes;
    }

    @Override
    public void configure() {
        bindScope(VUserScoped.class, TafScopes.VUSER);

        bindOperators(binder());
        bindTestStepExecutor(binder());
        requestStaticInjection(GuiceBeanManager.class);

        bindListener(Matchers.any(), new TafHostTypeListener());
        bindListener(Matchers.any(), new TafConfigurationTypeListener());
    }

    private void bindTestStepExecutor(Binder binder) {
        binder.bind(TestStepExecutor.class).to(TestStepExecutorImpl.class);
    }

    private void bindOperators(Binder binder) {
        for (Class clazz : classes) {
            Annotation eager = clazz.getAnnotation(Eager.class);
            Annotation singleton = clazz.getAnnotation(Singleton.class);

            if (eager != null && singleton != null) {
                binder.bind(clazz).asEagerSingleton();
            } else {
                binder.bind(clazz);
            }
        }
    }

    @Provides
    public TestContext provideTestContext() {
        return TafTestContext.getContext();
    }

    @Provides
    public TafConfiguration provideTafConfiguration() {
        return TafConfigurationProvider.provide();
    }

}

