package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.testng.ITestResult;
import org.testng.TestNG;
import ru.yandex.qatools.allure.commons.AllureFileUtils;
import ru.yandex.qatools.allure.model.Attachment;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.model.Parameter;
import ru.yandex.qatools.allure.model.Step;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static com.ericsson.cifwk.taf.ReadableIsIterableContainingInAnyOrder.containsInAnyOrder;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Collections2.transform;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 27.11.2015
 */
public class AllureTestUtils {

    public static final String ALLURE_RESULTS = "target/allure-results/";

    public static boolean runTestNg(Class testClass) {
        return runTestNg(null, testClass);
    }

    public static boolean runTestNg(String suiteName, Class... testClasses) {
        Preconditions.checkArgument(testClasses.length > 0, "At least one test class should be provided");
        TestNG testNG = new TestNG(false);
        testNG.setTestClasses(testClasses);
        if (suiteName != null) {
            testNG.setDefaultSuiteName(suiteName);
        }
        return runTestNg(testNG);
    }

    public static boolean runTestNg(String suiteFileName) throws IOException {
        TestNG testNG = new TestNG(false);
        List<String> suites = Lists.newArrayList();
        URL resource = AllureTestUtils.class.getResource("/" + suiteFileName);
        Preconditions.checkNotNull(resource, "Suite file %s not found", suiteFileName);
        suites.add(resource.getFile());
        testNG.setTestSuites(suites);
        return runTestNg(testNG);
    }

    private static boolean runTestNg(TestNG testNG) {
        SuccessListener successListener = new SuccessListener();
        testNG.addListener(successListener);
        testNG.run();
        return successListener.isPassed();
    }

    public static TestSuiteResult getLatestTestSuite(final String suiteName) {
        List<TestSuiteResult> suites = getSuiteResults();
        ImmutableList<TestSuiteResult> foundSuites = FluentIterable.from(suites).filter(new Predicate<TestSuiteResult>() {
            @Override
            public boolean apply(TestSuiteResult input) {
                return suiteName.equals(input.getName());
            }
        }).toSortedList(new Comparator<TestSuiteResult>() {
            @Override
            public int compare(TestSuiteResult o1, TestSuiteResult o2) {
                return -new Long(o1.getStop()).compareTo(o2.getStop());
            }
        });
        if (foundSuites.isEmpty()) {
            Collection<String> allSuitesInFolder = Collections2.transform(suites, new Function<TestSuiteResult, String>() {
                @Override
                public String apply(TestSuiteResult suite) {
                    return suite.getName();
                }
            });
            throw new RuntimeException(String.format("Suite '%s' was not found among suites [%s]", suiteName, allSuitesInFolder));
        }
        // multiple suites are possible (e.g. mvn clean wasn't executed before previous execution of the same test)
        return foundSuites.get(0);
    }

    public static List<Attachment> getAttachmentsByType(TestCaseResult testCase, final String mimeType) {
        return FluentIterable.from(testCase.getAttachments()).filter(new Predicate<Attachment>() {
            @Override
            public boolean apply(Attachment input) {
                return mimeType.equals(input.getType());
            }
        }).toList();
    }

    public static TestCaseResult getTestCase(TestSuiteResult testSuite, final String testCaseName) {
        List<TestCaseResult> testCases = getTestCases(testSuite, testCaseName);
        String errorMessage = "Expected 1 (test case %s), actual %s test cases";
        checkState(testCases.size() == 1, errorMessage, testCaseName, testCases.size());
        return testCases.iterator().next();
    }

    public static List<TestCaseResult> getTestCases(TestSuiteResult testSuite, final String testCaseName) {
        FluentIterable<TestCaseResult> testCases = FluentIterable.from(testSuite.getTestCases()).filter(new Predicate<TestCaseResult>() {
            @Override
            public boolean apply(TestCaseResult input) {
                return input.getName().equals(testCaseName);
            }
        });
        return Arrays.asList(Iterables.toArray(testCases, TestCaseResult.class));
    }

    public static String getAttachmentContent(Attachment attachment) {
        String source = attachment.getSource();
        try {
            return FileUtils.readFileToString(new File(ALLURE_RESULTS + source));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static void assertLabelNames(TestCaseResult testCase, String... expectedLabelNames) {
        assertLabels(testCase, new Function<Label, String>() {
            @Override
            public String apply(Label label) {
                return label.getName();
            }
        }, expectedLabelNames);
    }

    public static void assertLabelValues(TestCaseResult testCase, String... expectedLabelValues) {
        assertLabels(testCase, new Function<Label, String>() {
            @Override
            public String apply(Label label) {
                return label.getValue();
            }
        }, expectedLabelValues);
    }

    private static void assertLabels(TestCaseResult testCase, Function<Label, String> transformer, String... expectedValues) {
        ImmutableList<String> labelNames = FluentIterable.from(testCase.getLabels()).transform(transformer).toList();
        assertThat(labelNames, containsInAnyOrder(expectedValues));
    }

    public static Collection<String> getTestCaseNames(TestSuiteResult testSuite) {
        return transform(testSuite.getTestCases(), new Function<TestCaseResult, String>() {
            @Override
            public String apply(TestCaseResult input) {
                return input.getName();
            }
        });
    }

    public static Collection<String> getTestCaseTitles(TestSuiteResult testSuite) {
        return transform(testSuite.getTestCases(), new Function<TestCaseResult, String>() {
            @Override
            public String apply(TestCaseResult input) {
                return input.getTitle();
            }
        });
    }

    public static Collection<String> getTestStepNames(TestSuiteResult testSuite, final String testCaseName) {
        return transform(
                FluentIterable.from(testSuite.getTestCases()).firstMatch(new Predicate<TestCaseResult>() {
                    @Override
                    public boolean apply(TestCaseResult input) {
                        return input.getName().equals(testCaseName);
                    }
                }).get().getSteps(), new Function<Step, String>() {
                    @Override
                    public String apply(Step input) {
                        return input.getTitle();
                    }
                }
        );
    }

    private static List<TestSuiteResult> getSuiteResults() {
        File folder = new File(ALLURE_RESULTS);
        try {
            return AllureFileUtils.unmarshalSuites(folder);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static void assertParameter(TestCaseResult testCase, final String parameterName, String parameterValue) {
        Optional<Parameter> first = FluentIterable.from(testCase.getParameters()).filter(new Predicate<Parameter>() {
            @Override
            public boolean apply(Parameter parameter) {
                return parameter.getName().equals(parameterName);
            }
        }).first();
        assertTrue(first.isPresent());
        Parameter parameter = first.get();
        assertEquals(parameterValue, parameter.getValue());
        assertEquals("ARGUMENT", parameter.getKind().name());
    }

    private static class SuccessListener extends AbstractTestListener {

        private boolean passed = true;

        @Override
        public void onTestFailure(ITestResult result) {
            passed = false;
        }

        @Override
        public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
            passed = false;
        }

        public boolean isPassed() {
            return passed;
        }
    }

    public static class VerySpecialException extends RuntimeException {
    }
}
