package com.ericsson.cifwk.taf.testng;

import org.testng.TestRunner;

import java.util.concurrent.Callable;

public class TestRunnerCallable implements Callable<Void> {

    private final TestRunner runner;

    public TestRunnerCallable(TestRunner runner) {
        this.runner = runner;
    }

    @Override
    public Void call() throws Exception {
        runner.run();
        return null;
    }

}
