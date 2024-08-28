package com.ericsson.cifwk.taf.ui;

import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import com.google.common.collect.Lists;
import org.testng.IConfigurationListener;
import org.testng.ITestResult;

import java.util.List;

import static junit.framework.TestCase.fail;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 15/01/2016
 */
class UiCloseAllWindowsFailureListener extends AbstractTestListener implements IConfigurationListener {

    private List<Throwable> throwables = Lists.newArrayList();

    @Override
    public void onTestFailure(ITestResult result) {
        if (result.getThrowable() != null) {
            result.getThrowable().printStackTrace();
            throwables.add(result.getThrowable());
        }
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
        throwables.add(itr.getThrowable());
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
    }

    void failOnException() {
        if (!throwables.isEmpty()) {
            for (Throwable throwable : throwables) {
                throwable.printStackTrace();
            }
            fail("Exception in TestNG");
        }
    }
}
