package com.ericsson.cifwk.taf.tms.dto;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Mihails Volkovs <mihails.volkovs@ericsson.com>
 * 2015.07.29
 */
public class TestExecutionResultTest {

    ReferenceDataItem result = mock(ReferenceDataItem.class);

    @Test
    public void valueOf() {

        // not found values
        assertStatus(TestExecutionResult.NOT_DEFINED, null);
        assertStatus(TestExecutionResult.UNKNOWN, "0");
        assertStatus(TestExecutionResult.UNKNOWN, "7");

        // found values
        assertStatus(TestExecutionResult.NOT_STARTED, "1");
        assertStatus(TestExecutionResult.PASS, "2");
        assertStatus(TestExecutionResult.PASSED_WITH_EXCEPTION, "3");
        assertStatus(TestExecutionResult.FAIL, "4");
        assertStatus(TestExecutionResult.WIP, "5");
        assertStatus(TestExecutionResult.BLOCKED, "6");
    }

    private void assertStatus(TestExecutionResult expected, String statusId) {
        ReferenceDataItem incomingStatus = null;
        if (statusId != null) {
            when(result.getId()).thenReturn(statusId);
            incomingStatus = result;
        }
        assertEquals(expected, TestExecutionResult.toEnum(incomingStatus));
    }

}