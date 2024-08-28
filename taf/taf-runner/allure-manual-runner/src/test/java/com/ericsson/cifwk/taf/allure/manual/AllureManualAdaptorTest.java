package com.ericsson.cifwk.taf.allure.manual;

import com.ericsson.cifwk.taf.tms.TmsClient;
import com.ericsson.cifwk.taf.tms.dto.TestCampaignInfo;
import com.ericsson.cifwk.taf.tms.dto.TestCampaignItemInfo;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static com.ericsson.cifwk.taf.allure.manual.MockTestCampaigns.mockTestCampaign;
import static com.ericsson.cifwk.taf.allure.manual.MockTestCampaigns.mockTestCampaignItem;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AllureManualAdaptorTest {

    @Mock
    TmsClient tmsClient;

    @InjectMocks
    AllureManualAdaptor adaptor = new AllureManualAdaptor();

    @Before
    public void setUp() {
        TestCampaignItemInfo testCampaignItem = mockTestCampaignItem();
        TestCampaignInfo testCampaign = mockTestCampaign(testCampaignItem);
        when(tmsClient.getTestCampaign(456)).thenReturn(testCampaign);
    }

    @Test
    public void generateXmlReport() throws IOException {

        // generating result XML if doesn't exist
        File resultFolder = new File("target/allure-results");
        String resultFolderPath = resultFolder.getAbsolutePath();

        // cleaning up report folder on demand
        if (resultFolder.exists()) {
            for (File file : resultFolder.listFiles()) {
                file.delete();
            }
        }
        adaptor.generateXmlReport(456);

        // reading result XML
        File[] resultXmls = resultFolder.listFiles();
        assertEquals(format("Folder %s does not contain single file", resultFolderPath), 1, resultXmls.length);
        File resultXml = resultXmls[0];
        String xml = FileUtils.readFileToString(resultXml);

        // asserting XML content
        assertThat(xml, containsString("<name>Manual Test Campaign: 456</name>"));
        assertThat(xml, containsString("<title>Manual: planName</title>"));
        assertThat(xml, containsString("<name>TEST-123</name>"));
        assertThat(xml, containsString("<title>testTitle</title>"));
        assertThat(xml, containsString("<description type=\"markdown\">## Test Case"));
        assertThat(xml, containsString("Requirements: [REQ-123](http://jira-oss.lmera.ericsson.se/browse/REQ-123) [REQ-456](http://jira-oss.lmera.ericsson.se/browse/REQ-456)"));
        assertThat(xml, containsString("Type: **Functional**"));
        assertThat(xml, containsString("Package: **package**"));
        assertThat(xml, containsString("Component: **component1**"));
        assertThat(xml, containsString("Component: **component2**"));
        assertThat(xml, containsString("Test Status: **Preliminary**"));
        assertThat(xml, containsString("Execution Status: **Pass**"));
        assertThat(xml, containsString("&gt; comment"));
        assertThat(xml, containsString("<name>stepName1</name>"));
        assertThat(xml, containsString("<label name=\"issue\" value=\"BUG-123\"/>"));
        assertThat(xml, containsString("<label name=\"issue\" value=\"BUG-456\"/>"));
        assertThat(xml, containsString("<label name=\"testId\" value=\"TEST-123\"/>"));
        assertThat(xml, containsString("<label name=\"story\" value=\"REQ-123: Requirement summary\"/>"));
        assertThat(xml, containsString("<label name=\"story\" value=\"REQ-456: Requirement summary\"/>"));
        assertThat(xml, containsString("<label name=\"feature\" value=\"component1\"/>"));
        assertThat(xml, containsString("<label name=\"feature\" value=\"component2\"/>"));
        assertThat(xml, containsString("<label name=\"severity\" value=\"blocker\"/>"));
        assertThat(xml, containsString("<label name=\"framework\" value=\"TAF\"/>"));
    }
}