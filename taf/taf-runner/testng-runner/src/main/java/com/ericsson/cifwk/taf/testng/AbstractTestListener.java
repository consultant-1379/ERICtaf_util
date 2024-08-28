package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.meta.API;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * @deprecated use event-based listening via {@link TafTestApiEventDispatcher}.
 * Left until all ENM testware is on new TAF version that has TAF Test API on board.
 */
@API(API.Quality.Deprecated)
@Deprecated
// TODO: remove when Test API is used everywhere in TAF instead of TestNG
public abstract class AbstractTestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
    }

    @Override
    public void onTestSuccess(ITestResult result) {
    }

    @Override
    public void onTestFailure(ITestResult result) {
    }

    @Override
    public void onTestSkipped(ITestResult result) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onFinish(ITestContext context) {
    }

}
