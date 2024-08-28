package com.ericsson.cifwk.taf.tms;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Mihails Volkovs <mihails.volkovs@ericsson.com>
 * 17/07/2015
 */
public class TafConfigTest {

    private static final String HOST = "https://taftm.seli.wh.rnd.internal.ericsson.com";

    private static final String BASE_API_URL = HOST + "/tm-server/api/";

    private static final String BASE_WEB_URL = HOST + "/#tm/";

    private static final String TEST_CASES_API = BASE_API_URL + "test-cases/";

    private static final String TEST_PLANS_API = BASE_API_URL + "test-campaigns/";

    private static final String TEST_PLANS_WEB = BASE_WEB_URL + "viewTestCampaign/";

    private TafConfig tafConfig;

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Before
    public void setUp() {
        tafConfig = TafConfig.newInstance();
    }

    @Test
    public void getApiUrlForTestCase() {
        assertEquals(TEST_CASES_API + "?view=detailed", tafConfig.getApiUrlForTestCase(""));
        assertEquals(TEST_CASES_API + "TEST-1?view=detailed", tafConfig.getApiUrlForTestCase("TEST-1"));
        assertEquals(TEST_CASES_API + "TEST%2B1?view=detailed", tafConfig.getApiUrlForTestCase("TEST+1"));
        assertEquals(TEST_CASES_API + "TEST%201?view=detailed", tafConfig.getApiUrlForTestCase("TEST 1"));
        assertEquals(TEST_CASES_API + "TEST%201%201?view=detailed", tafConfig.getApiUrlForTestCase("TEST 1 1"));
    }

    @Test(expected = NullPointerException.class)
    public void getApiUrlForTestCaseExceptional() {
        tafConfig.getApiUrlForTestCase(null);
    }

    @Test
    public void getDefaultApiUrlForTestCampaign() {
        assertEquals(TEST_PLANS_API + "1?view=detailedItems", tafConfig.getApiUrlForTestCampaign(1L));
    }

    @Test
    public void getApiUrlForTestCampaign() {
        System.setProperty("taf.tms.api.test-campaign", "https://my.campaign.ericsson.se/#tm/viewTestCampaign/%s");
        System.setProperty("taf.tms.api.test-plan", "https://my.testplan.ericsson.se/#tm/viewTestCampaign/%s");
        tafConfig = TafConfig.newInstance();
        assertEquals("https://my.campaign.ericsson.se/#tm/viewTestCampaign/" + "1", tafConfig.getApiUrlForTestCampaign(1L));
    }

    @Test
    public void getApiUrlForTestPlan() {
        System.setProperty("taf.tms.api.test-plan", "https://my.testplan.ericsson.se/#tm/viewTestCampaign/%s");
        tafConfig = TafConfig.newInstance();
        assertEquals("https://my.testplan.ericsson.se/#tm/viewTestCampaign/" + "1", tafConfig.getApiUrlForTestCampaign(1L));
    }

    @Test
    public void getDefaultWebUrlForTestCampaign() {
        assertEquals(TEST_PLANS_WEB + "1", tafConfig.getWebUrlForTestCampaign(1L));
    }

    @Test
    public void getWebUrlForTestCampaign () {
        System.setProperty("taf.tms.web.test-campaign", "https://my.campaign.ericsson.se/#tm/viewTestCampaign/%s");
        System.setProperty("taf.tms.web.test-plan", "https://my.testplan.ericsson.se/#tm/viewTestCampaign/%s");
        tafConfig = TafConfig.newInstance();
        assertEquals("https://my.campaign.ericsson.se/#tm/viewTestCampaign/" + "1", tafConfig.getWebUrlForTestCampaign(1L));
    }

    @Test
    public void getWebUrlForTestPlan () {
        System.setProperty("taf.tms.web.test-plan", "https://my.testplan.ericsson.se/#tm/viewTestCampaign/%s");
        tafConfig = TafConfig.newInstance();
        assertEquals("https://my.testplan.ericsson.se/#tm/viewTestCampaign/" + "1", tafConfig.getWebUrlForTestCampaign(1L));
    }

    @Test
    public void isDisabledByTestPlan() {
        System.setProperty("taf.tms.api.test-case", "null");
        System.setProperty("taf.tms.web.test-plan", "null");
        System.setProperty("taf.tms.api.test-plan", "null");
        tafConfig = TafConfig.newInstance();
        assertFalse(tafConfig.isEnabled());
    }

    @Test
    public void isDisabledByTestCampaign() {
        System.setProperty("taf.tms.api.test-case", "null");
        System.setProperty("taf.tms.web.test-campaign", "null");
        System.setProperty("taf.tms.api.test-campaign", "null");
        tafConfig = TafConfig.newInstance();
        assertFalse(tafConfig.isEnabled());
    }

    @Test
    public void isEnabled() {
        System.setProperty("taf.tms.api.test-case", "null");
        System.setProperty("taf.tms.web.test-plan", "null");
        System.setProperty("taf.tms.api.test-campaign", "null");
        tafConfig = TafConfig.newInstance();
        assertTrue(tafConfig.isEnabled());
    }
}