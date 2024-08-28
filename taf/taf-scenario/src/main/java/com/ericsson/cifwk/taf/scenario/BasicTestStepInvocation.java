package com.ericsson.cifwk.taf.scenario;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;

@API(Internal)
public abstract class BasicTestStepInvocation implements TestStepInvocation {
    protected final long id = idGenerator.incrementAndGet();
    protected final String name;
    protected final boolean allowAlwaysRun;
    protected boolean alwaysRun = false;

    public BasicTestStepInvocation(String name, boolean allowAlwaysRun) {
        this.name = name;
        this.allowAlwaysRun = allowAlwaysRun;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void addParameter(String key, Object value) {

    }

    @Override
    public void alwaysRun() {
        if (!allowAlwaysRun) {
            throw new UnsupportedOperationException();
        }
        alwaysRun = true;
    }

    @Override
    public boolean isAlwaysRun() {
        return alwaysRun;
    }

}
