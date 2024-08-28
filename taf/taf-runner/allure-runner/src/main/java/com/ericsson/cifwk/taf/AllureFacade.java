package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.logging.Attachments;
import com.ericsson.cifwk.taf.logging.TestStepLogs;
import com.ericsson.cifwk.taf.tms.TmsClient;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.config.AllureModelUtils;
import ru.yandex.qatools.allure.events.AddParameterEvent;
import ru.yandex.qatools.allure.events.MakeAttachmentEvent;
import ru.yandex.qatools.allure.events.StepEvent;
import ru.yandex.qatools.allure.events.StepFailureEvent;
import ru.yandex.qatools.allure.events.StepFinishedEvent;
import ru.yandex.qatools.allure.events.StepStartedEvent;
import ru.yandex.qatools.allure.events.TestCaseCanceledEvent;
import ru.yandex.qatools.allure.events.TestCaseFailureEvent;
import ru.yandex.qatools.allure.events.TestCaseFinishedEvent;
import ru.yandex.qatools.allure.events.TestCaseStartedEvent;
import ru.yandex.qatools.allure.events.TestSuiteEvent;
import ru.yandex.qatools.allure.events.TestSuiteFinishedEvent;
import ru.yandex.qatools.allure.events.TestSuiteStartedEvent;
import ru.yandex.qatools.allure.model.Attachment;
import ru.yandex.qatools.allure.model.Failure;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.model.ParameterKind;
import ru.yandex.qatools.allure.model.Status;
import ru.yandex.qatools.allure.model.Step;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;
import ru.yandex.qatools.allure.storages.TestCaseStorage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.currentTimeMillis;
import static ru.yandex.qatools.allure.utils.AllureResultsUtils.writeAttachmentSafely;

/**
 * @author Mihails Volkovs (mihails.volkovs@ericsson.com)
 *         Date: 5/6/2015.
 */
public class AllureFacade {

    private static AllureFacade instance = new AllureFacade();

    private static Map<String, String> suiteUid = new ConcurrentHashMap<>();

    //evlailj: test names are not cleared and may clash with test names from another suite, possible bug
    private static Set<String> startedTestNames = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    private static TestCaseStorage testCaseStorage = new TestCaseStorage();

    private static TestSuiteStorage testSuiteStorage = new TestSuiteStorage();

    private static SuiteEventStorage suiteEventStorage = new SuiteEventStorage();

    private static StepEventStorage commonSetupConfigEvents = new StepEventStorage();

    private static StepEventStorage methodSetupConfigEvents = new StepEventStorage();
    private static StepEventStorage attachmentEvents = new StepEventStorage(){
        @Override
        protected List<StepEvent> childValue(List<StepEvent> parentValue) {
            return parentValue;
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(AllureFacade.class);

    private static List<Label> runtimeLabels = new ArrayList<>();

    private static final String KPI_LABEL_PREFIX = "KPI-";

    private AllureFacade() {
    }

    public static AllureFacade getInstance() {
        return instance;
    }

    @VisibleForTesting
    static void setInstance(AllureFacade instance) {
        AllureFacade.instance = instance;
    }

    private static ThreadLocal<TmsClient> tmsClient = new InheritableThreadLocal<TmsClient>() {
        @Override
        protected TmsClient initialValue() {
            return new TmsClient();
        }
    };

    public static void startSuite(String suiteName) {
        suiteEventStorage.get(); // init

        if (tmsClient.get() == null) {
            tmsClient.set(new TmsClient());
        } else {
            LOG.warn("TMS Client has already been set");
        }
        TestSuiteStartedEvent event = new TestSuiteStartedEvent(
                getSuiteUid(suiteName), suiteName
        ) {
            @Override
            public void process(TestSuiteResult testSuite) {
                super.process(testSuite);
                testSuiteStorage.setCurrentSuite(testSuite);
            }
        };
        getLifecycle().fire(event.withTitle(
                suiteName
        ).withLabels(
                AllureModelUtils.createTestFrameworkLabel("TAF")
        ));
    }

    public static void finishSuite(String suiteName) {
        TestSuiteFinishedEvent suiteFinishedEvent = new TestSuiteFinishedEvent(getSuiteUid(suiteName));
        suiteEventStorage.add(suiteFinishedEvent);
        testSuiteStorage.removeCurrentSuite();
        if (testSuiteStorage.getCurrentSuite() == null) {
            suiteEventStorage.flush();
            cleanUpStorage();

            tmsClient().close();
            tmsClient.remove();
        }
    }

    private static void cleanUpStorage() {
        suiteEventStorage.remove();
        testCaseStorage.remove();
        commonSetupConfigEvents.remove();
        methodSetupConfigEvents.remove();
        attachmentEvents.remove();
    }

    public static String getCurrentSuiteName() {
        return testSuiteStorage.getCurrentSuite().getName();
    }

    public static void startTestCase(String suiteName, String testName, String testId, TestCaseBean bean) {
        suiteEventStorage.flush();
        startedTestNames.add(testName);

        TestCaseStartedEvent event = new TestCaseStartedEvent(getSuiteUid(suiteName), testId) {
            @Override
            public void process(TestCaseResult testCase) {
                super.process(testCase);
                testCaseStorage.set(testCase);
            }
        };

        TestCaseEnricher enricher = new TestCaseEnricher(tmsClient());
        enricher.enrich(bean, event);

        TestStepLogs.addLog();
        getLifecycle().fire(event);

        fireAddParameterEvents(bean);

        fireConfigEvents();
    }

    public static void finishTestCase() {
        addLabelsToAllure();
        Attachments.addLogAttachment();
        getLifecycle().fire(new TestCaseFinishedEvent());
    }

    public static void failTestCase(Throwable throwable) {
        getLifecycle().fire(new TestCaseFailureEvent().withThrowable(throwable));
    }

    public static void skipTestCase(Throwable throwable) {
        getLifecycle().fire(new TestCaseCanceledEvent().withThrowable(throwable));
    }


    private static TestCaseResult getLatestTestCase() {
      return getLatestTestCase(new TestCaseResult());
    }

    private static TestCaseResult getLatestTestCase(TestCaseResult defaultValue) {
        TestCaseResult testCase = testCaseStorage.get();
        if (testCase.getName() != null) {
            return testCase;
        } else {
            testCase = testSuiteStorage.getLatestTestCase(defaultValue);
            return testCase;
        }
    }

    private static Step createStep(String name, Status status) {
        return new Step().withName(name).withStatus(status).withStart(currentTimeMillis()).withStop(currentTimeMillis());
    }

    public static void fireConfigEvents() {
        commonSetupConfigEvents.flush();
        methodSetupConfigEvents.flush();
        attachmentEvents.flush();
    }

    public static void startConfig(String fixtureName, ConfigMethodType configMethodType) {
        if (isSetUpMethod(configMethodType)) {
            // initialize thread-local storage
            attachmentEvents.get();
        }
    }

    public static void passConfig(String fixtureName, ConfigMethodType methodType) {
        if (isSetUpMethod(methodType)) {
            AllureFacade.addSetupConfig(fixtureName, getConfigEventStorageByType(methodType));
        } else if (isTearDownMethod(methodType)) {
            addStep(fixtureName, Status.PASSED);
        }
    }

    public static void failConfig(Throwable throwable, String fixtureName, ConfigMethodType type) {
        Failure failure = new Failure().withMessage(throwable.getMessage()).withStackTrace(getStackTrace(throwable));
        Status status = throwable instanceof AssertionError ? Status.FAILED : Status.BROKEN;

        if (isSetUpMethod(type)) {
            failSetupConfig(throwable, fixtureName, getConfigEventStorageByType(type));
        } else if (isTearDownMethod(type)) {
            Step step = createStep(fixtureName, status);
            getLatestTestCase().withStatus(status).withFailure(failure).getSteps().add(step);
        }
    }

    public static void attachToLatestTestCase(String name, byte[] content, String type) {
        TestCaseResult latestTestCase = getLatestTestCase(null);
        if(latestTestCase != null) {
            Attachment attachment = writeAttachmentSafely(content, name, type);
            latestTestCase.getAttachments().add(attachment);
        } else {
            attachmentEvents.add(new MakeAttachmentEvent(content, name, type));
        }
    }

    public static void skipConfig(String fixtureName, ConfigMethodType methodType) {
        if (isSetUpMethod(methodType)) {
            skipSetupConfig(fixtureName, getConfigEventStorageByType(methodType));
        } else if (isTearDownMethod(methodType)) {
            AllureFacade.addStep(fixtureName, Status.SKIPPED);
        }
    }

    private static void addSetupConfig(String fixtureName, StepEventStorage eventStorage) {
        eventStorage.add(new StepStartedEvent(fixtureName));
        eventStorage.add(new StepFinishedEvent());
    }

    private static void failSetupConfig(Throwable throwable, String fixtureName, StepEventStorage eventStorage) {
        eventStorage.add(new StepStartedEvent(fixtureName));
        eventStorage.add(new StepFailureEvent().withThrowable(throwable));
        eventStorage.add(new StepFinishedEvent());
    }

    private static void skipSetupConfig(String fixtureName, StepEventStorage eventStorage) {
        eventStorage.add(new StepStartedEvent(fixtureName));
        eventStorage.add(new StepSkippedEvent());
        eventStorage.add(new StepFinishedEvent());
    }

    private static void addStep(String name, Status status) {
        Step step = createStep(name, status);
        getLatestTestCase().getSteps().add(step);
    }

    public static String getTestName(String testName, Object[] parameters) {
        return ParametersManager.getParametrizedName(testName, parameters);
    }

    public static boolean isTestStarted(String testName) {
        return startedTestNames.contains(testName);
    }

    public static TmsClient tmsClient() {
        TmsClient tmsClient = AllureFacade.tmsClient.get();
        if (tmsClient == null) {
            LOG.warn("onStart() wasn't called by current thread, creating extra TMS client");
            tmsClient = new TmsClient();
            AllureFacade.tmsClient.set(tmsClient);
        }
        return tmsClient;
    }

    private static Allure getLifecycle() {
        return AllureProvider.singletone();
    }

    private static String getSuiteUid(String suiteName) {
        if (suiteUid.containsKey(suiteName)) {
            return suiteUid.get(suiteName);
        } else {
            String uid = UUID.randomUUID().toString();
            suiteUid.put(suiteName, uid);
            return uid;
        }
    }

    public void addAttachment(String type, String title, byte[] bytes) {
        MakeAttachmentEvent attachmentEvent = new MakeAttachmentEvent(bytes, title, type);
        boolean isTestCaseStarted = testCaseStorage.get().getName() != null;
        if (isTestCaseStarted) {
            getLifecycle().fire(attachmentEvent);
        } else {
            attachmentEvents.add(attachmentEvent);
        }
    }

    private static class StepEventStorage extends InheritableThreadLocal<List<StepEvent>> {

        @Override
        protected List<StepEvent> initialValue() {
            return Lists.newArrayList();
        }

        @Override
        protected List<StepEvent> childValue(List<StepEvent> parentValue) {
            return Lists.newArrayList(parentValue);
        }

        public void flush() {
            List<StepEvent> stepEvents = get();
            for (StepEvent stepEvent : stepEvents) {
                if (stepEvent instanceof StepStartedEvent) {
                    AllureProvider.singletone().fire((StepStartedEvent) stepEvent);
                } else if (stepEvent instanceof StepFinishedEvent) {
                    AllureProvider.singletone().fire((StepFinishedEvent) stepEvent);
                } else {
                    AllureProvider.singletone().fire(stepEvent);
                }
            }
            remove();
        }

        public void add(StepEvent stepEvent) {
            List<StepEvent> stepEvents = get();
            stepEvents.add(stepEvent);
        }

    }

    private static class SuiteEventStorage extends InheritableThreadLocal<List<TestSuiteEvent>> {

        @Override
        protected List<TestSuiteEvent> initialValue() {
            return Lists.newArrayList();
        }

        public synchronized void flush() {
            List<TestSuiteEvent> testSuiteEvents = get();
            for (TestSuiteEvent event : testSuiteEvents) {
                if (event instanceof TestSuiteFinishedEvent) {
                    AllureProvider.singletone().fire((TestSuiteFinishedEvent) event);
                } else {
                    AllureProvider.singletone().fire(event);
                }
            }
            remove();
        }

        public synchronized void add(TestSuiteEvent suiteEvent) {
            List<TestSuiteEvent> testSuiteEvents = get();
            testSuiteEvents.add(suiteEvent);
        }

    }

    private static class StepSkippedEvent implements StepEvent {

        @Override
        public void process(Step step) {
            step.setStatus(Status.SKIPPED);
        }

    }

    private static StepEventStorage getConfigEventStorageByType(ConfigMethodType type) {
        StepEventStorage eventStorage;
        if (isBeforeMethodConfigMethod(type)) {
            eventStorage = methodSetupConfigEvents;
            methodSetupConfigEvents.remove();
        } else {
            eventStorage = commonSetupConfigEvents;
        }
        return eventStorage;
    }

    public enum ConfigMethodType {
        BEFORE_TEST_METHOD("BeforeTest"), BEFORE_CLASS("BeforeClass"),
        BEFORE_GROUPS("BeforeGroups"), AFTER_TEST("AfterTest"),
        AFTER_CLASS("AfterClass"), AFTER_GROUPS("AfterGroups");

        private String title;

        ConfigMethodType(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public String getName() {
            return "@" + this.getTitle();
        }
    }

    private static boolean isSetUpMethod(ConfigMethodType type) {
        return type.getTitle().startsWith("Before");
    }

    private static boolean isTearDownMethod(ConfigMethodType type) {
        return type.getTitle().startsWith("After");
    }

    private static boolean isBeforeMethodConfigMethod(ConfigMethodType type) {
        return type.equals(ConfigMethodType.BEFORE_TEST_METHOD);
    }

    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw); // NOSONAR
        return sw.getBuffer().toString();
    }

    /**
     * Creates test case parameters in XML in case of parametrized test (see TestNG @DataProvider).
     **/
    private static void fireAddParameterEvents(TestCaseBean testCase) {
        for (Map.Entry<String, Object> parameter : testCase.getParameters().entrySet()) {
            String name = parameter.getKey();
            String value = parameter.getValue() == null ? "<null>" : parameter.getValue().toString();
            AllureProvider.singletone().fire(new AddParameterEvent(name, value, ParameterKind.ARGUMENT));
        }
    }

    public static void addDatasourceLabelToTestCase(String kpiId, TestDataSource<? extends DataRecord> dataSource) {
        Iterator<? extends DataRecord> iterator = dataSource.iterator();
        while(iterator.hasNext()) {
            DataRecord dataRecord = iterator.next();
            Map<String, Object> dataRecordFields = dataRecord.getAllFields();
            String id = generateKpiUID(kpiId);
            LOG.debug("KpiId = {}", id);
            for(Map.Entry<String, Object> dataRecordField : dataRecordFields.entrySet()) {
                Label label = new Label().withName(id + ":::" + dataRecordField.getKey()).withValue(dataRecordField.getValue().toString());
                LOG.debug("Adding label {} = {}", label.getName(), label.getValue());
                runtimeLabels.add(label);
            }
        }
    }

    synchronized static void addLabelsToAllure() {
        TestCaseResult testCaseResult = getLatestTestCase();
        List<Label> testCaseResultLabels = testCaseResult.getLabels();
        LOG.debug("Adding labels to allure testCase {}", testCaseResult.getName());
        testCaseResultLabels.addAll(runtimeLabels);
        testCaseResult.setLabels(testCaseResultLabels);
        runtimeLabels.clear();
    }

    private static String generateKpiUID(String kpiId) {
        Random rand = new Random();
        return String.valueOf("KPI::" + kpiId + "::" + Math.abs(rand.nextInt()));
    }
}
