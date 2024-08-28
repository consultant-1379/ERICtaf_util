package com.ericsson.cifwk.taf.eiffel.testng;

import com.ericsson.cifwk.taf.eiffel.EiffelAdapter;
import com.ericsson.cifwk.taf.testapi.TestGroupResult;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;
import com.ericsson.cifwk.taf.testapi.utils.TestResultHelper;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TafEiffelListenerTest {

    @Mock
    TestResultHelper resultHelper;

    @Mock
    EiffelAdapter adapterMock;

    @Spy
    @InjectMocks
    private TafEiffelListener listener;

    @Test
    public void shouldGetResultCode_Suite_Fail() {
        List<TestGroupResult> results = Lists.newArrayList(createSuiteResult(1));
        ResultCode resultCode = listener.getResultCode(results);
        assertThat(resultCode, equalTo(ResultCode.FAILURE));
    }

    @Test
    public void shouldGetResultCode_Suite_ok() {
        List<TestGroupResult> results = Lists.newArrayList(createSuiteResult(0));
        ResultCode resultCode = listener.getResultCode(results);
        assertThat(resultCode, equalTo(ResultCode.SUCCESS));
    }

    @Test
    public void shouldGetResultCode_Test() {
        ResultCode resultCode = listener.getResultCode(TestEvent.ExecutionState.FAILED);
        assertThat(resultCode, equalTo(ResultCode.FAILURE));
    }

    @Test
    public void shouldGetTestWarePackage() throws Exception {
        assertThat(listener.getTestwarePackage(createTestResult("/proj/testware.jar/classes/")), is("testware"));
        assertThat(listener.getTestwarePackage(createTestResult("/proj/testware/classes/")), nullValue());
    }

    private TestGroupResult createSuiteResult(int failedTests) {
        TestGroupResult suiteResult = mock(TestGroupResult.class);
        when(suiteResult.getFailedTestCount()).thenReturn(failedTests);
        return suiteResult;
    }

    private TestMethodExecutionResult createTestResult(String sourceUrl){
        TestMethodExecutionResult result = mock(TestMethodExecutionResult.class);
        doReturn(sourceUrl).when(listener).getSourceUrl(result);
        return result;
    }

}
