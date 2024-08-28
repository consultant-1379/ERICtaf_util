package com.ericsson.cifwk.taf;

import com.google.common.base.Preconditions;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Keeps track of current test suite result and its relation to any nested test suites.
 *
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         07/09/2015
 */
public class TestSuiteStorage {

    private ThreadLocal<TestSuiteHolder> suiteHolder = new InheritableThreadLocal<>();

    /*
    *  Valid test suite levels:
    *
    *  0 - no suite started
    *  1 - parent suite
    *  2 - child suite
    * */
    private ThreadLocal<Integer> suiteLevel = new InheritableThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public TestSuiteResult getCurrentSuite() {
        TestSuiteHolder testSuiteHolder = suiteHolder.get();
        if (testSuiteHolder != null) {
            return testSuiteHolder.getSuite();
        } else {
            return null;
        }
    }

    protected TestSuiteHolder getCurrentSuiteHolder() {
        return suiteHolder.get();
    }

    public synchronized void setCurrentSuite(TestSuiteResult testSuite) {
        Preconditions.checkState(suiteLevel.get() < 2, "Suite level should be less than 2");
        suiteLevel.set(suiteLevel.get() + 1);

        if (suiteHolder.get() == null) {
            suiteHolder.set(new TestSuiteHolder());
        } else {
            TestSuiteHolder parentHolder = suiteHolder.get();
            TestSuiteHolder childHolder = new TestSuiteHolder(parentHolder);
            suiteHolder.set(childHolder);
        }
        suiteHolder.get().setSuite(testSuite);
    }

    public synchronized void removeCurrentSuite() {
        Preconditions.checkState(suiteLevel.get() > 0, "Suite level should be greater than 0");
        suiteLevel.set(suiteLevel.get() - 1);

        if (suiteLevel.get() == 0) {
            suiteHolder.remove();
        } else {
            TestSuiteHolder testSuiteHolder = suiteHolder.get();
            if (testSuiteHolder != null && !testSuiteHolder.isMultithreaded()) {
                suiteHolder.set(testSuiteHolder.getParentSuiteHolder());
            }
        }
    }

    public TestCaseResult getLatestTestCase(TestCaseResult defaultValue) {
        return getLatestTestCase(getCurrentSuiteHolder(), defaultValue);
    }

    private TestCaseResult getLatestTestCase(TestSuiteStorage.TestSuiteHolder testSuiteHolder, TestCaseResult defaultValue) {
        if (testSuiteHolder == null) {
            return null;
        }
        TestSuiteResult testSuite = testSuiteHolder.getSuite();
        if (testSuite == null) {
            return null;
        }
        List<TestCaseResult> testCases = testSuite.getTestCases();
        if (!testCases.isEmpty()) {
            return testCases.get(testCases.size() - 1);
        } else {
            List<TestSuiteStorage.TestSuiteHolder> suiteHolders = testSuiteHolder.getChildSuites();
            ListIterator<TestSuiteHolder> iterator = suiteHolders.listIterator(suiteHolders.size());
            while (iterator.hasPrevious()) {
                TestSuiteStorage.TestSuiteHolder subHolder = iterator.previous();
                TestCaseResult testCase = getLatestTestCase(subHolder, null);
                if (testCase != null) {
                    return testCase;
                }
            }
        }
        return defaultValue;
    }

    protected static class TestSuiteHolder {

        private TestSuiteResult testSuiteResult = new TestSuiteResult();
        private List<TestSuiteHolder> childSuites = new LinkedList<>();
        private TestSuiteHolder parentSuite;
        private Thread creatorThread;

        public TestSuiteHolder() {
            creatorThread = Thread.currentThread();
        }

        public TestSuiteHolder(TestSuiteHolder parentSuite) {
            this();
            parentSuite.addChildSuite(this);
            this.setParentSuite(parentSuite);
        }

        public TestSuiteResult getSuite() {
            return testSuiteResult;
        }

        public void setSuite(TestSuiteResult testSuiteResult) {
            this.testSuiteResult = testSuiteResult;
        }

        public synchronized List<TestSuiteHolder> getChildSuites() {
            return childSuites;
        }

        public synchronized void addChildSuite(TestSuiteHolder childSuiteHolder) {
            childSuites.add(childSuiteHolder);
        }

        public TestSuiteHolder getParentSuiteHolder() {
            return parentSuite;
        }

        public void setParentSuite(TestSuiteHolder parentSuite) {
            this.parentSuite = parentSuite;
        }

        public boolean isMultithreaded() {
            return parentSuite != null && parentSuite.creatorThread != creatorThread;
        }
    }
}
