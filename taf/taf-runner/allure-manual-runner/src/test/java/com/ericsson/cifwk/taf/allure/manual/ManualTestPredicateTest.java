package com.ericsson.cifwk.taf.allure.manual;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.ericsson.cifwk.taf.tms.dto.TestCampaignItemInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Mihails Volkovs <mihails.volkovs@ericsson.com>
 * 20/07/2015
 */
public class ManualTestPredicateTest {

    private ManualTestPredicate predicate;

    private TestCampaignItemInfo testCampaignItem;

    @Before
    public void setUp() {
        predicate = new ManualTestPredicate();
        testCampaignItem = mock(TestCampaignItemInfo.class, RETURNS_DEEP_STUBS);

    }

    @Test
    public void apply() {
        when(testCampaignItem.getTestCase().getExecutionType().getTitle()).thenReturn("Manual");
        assertTrue(predicate.apply(testCampaignItem));

        when(testCampaignItem.getTestCase().getExecutionType().getTitle()).thenReturn("Automated");
        assertFalse(predicate.apply(testCampaignItem));
    }

    @Test
    public void applyExceptional() {
        when(testCampaignItem.getTestCase().getExecutionType()).thenReturn(null);
        assertTrue(predicate.apply(testCampaignItem));
    }
}