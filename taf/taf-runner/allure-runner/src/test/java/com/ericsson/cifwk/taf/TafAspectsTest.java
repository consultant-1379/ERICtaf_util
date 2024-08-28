package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.annotations.Attachment;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.StepFinishedEvent;
import ru.yandex.qatools.allure.events.StepStartedEvent;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class TafAspectsTest {

    private static final String SUITE_NAME = AttachmentInSetUpTest.class.getName();
    private static final String IMAGE_PNG = "image/png";
    private static final String ATTACHMENT_CONTENT = "AttachmentContent";

    private Allure allure;
    private AllureFacade allureFacade;
    private AllureFacade allureFacadeSpy;
    private OperatorImpl operator;

    private static final Logger LOGGER = LoggerFactory.getLogger(TafAspectsTest.class);

    @Before
    public void setUp() {
        allure = spy(AllureProvider.singletone());
        AllureProvider.instance = allure;

        allureFacade = AllureFacade.getInstance();
        allureFacadeSpy = spy(allureFacade);
        AllureFacade.setInstance(allureFacadeSpy);

        operator = new OperatorImpl();
    }

    @After
    public void tearDown() {
        AllureProvider.instance = Allure.LIFECYCLE;
        AllureFacade.setInstance(allureFacade);
    }

    @Test
    public void shouldTriggerOnTestStep() {
        operator.doStuff();

        verify(allure).fire(any(StepStartedEvent.class));
        verify(allure).fire(any(StepFinishedEvent.class));
    }

    @Test
    public void shouldTriggerOnAttachment() {
        operator.attachment();

        verify(allureFacadeSpy).addAttachment(eq(IMAGE_PNG), anyString(), any(byte[].class));
    }

    @Test
    public void shouldTriggerOnAttachmentInSetUp() {
        AllureTestUtils.runTestNg(SUITE_NAME, AttachmentInSetUpTest.class);
        
        verify(allureFacadeSpy).addAttachment(eq(IMAGE_PNG), anyString(), any(byte[].class));

        TestSuiteResult suite = AllureTestUtils.getLatestTestSuite(SUITE_NAME);
        TestCaseResult testCase = AllureTestUtils.getTestCase(suite, "no test ID provided");
        assertThat(testCase.getAttachments(), not(empty()));
        ru.yandex.qatools.allure.model.Attachment attachment = testCase.getAttachments().get(0);
        assertThat(attachment.getType(), is(IMAGE_PNG));
        assertThat(AllureTestUtils.getAttachmentContent(attachment), is(ATTACHMENT_CONTENT));
    }

    public static class OperatorImpl {

        @TestStep(id = "doOtherStuff")
        public void doStuff() {

        }

        @Attachment(type = IMAGE_PNG, value = "{0}")
        public byte[] attachment() {
            LOGGER.info("Attachment created");
            return ATTACHMENT_CONTENT.getBytes();
        }

    }

    public static class AttachmentInSetUpTest {

        @BeforeSuite(alwaysRun = true)
        public void setUpScenario(){
            TestScenario scenario = scenario().addFlow(flow("").addTestStep(annotatedMethod(this, "myTestStep"))).build();
            runner().build().start(scenario);
        }

        @TestStep(id="myTestStep")
        public void testStep(){
            OperatorImpl operator = new OperatorImpl();
            operator.attachment();
        }

        @org.testng.annotations.Test
        public void doSomething(){
        }
    }

}
