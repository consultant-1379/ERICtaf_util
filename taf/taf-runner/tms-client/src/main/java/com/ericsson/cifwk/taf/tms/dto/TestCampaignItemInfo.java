package com.ericsson.cifwk.taf.tms.dto;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by ekonsla on 05/08/2016.
 */
public class TestCampaignItemInfo {

    private TestCaseInfo testCase;

    private ReferenceDataItem result;

    private List<String> defectIds = newArrayList();

    public TestCaseInfo getTestCase() {
        return testCase;
    }

    public ReferenceDataItem getResult() {
        return result;
    }

    public List<String> getDefectIds() {
        return defectIds;
    }
}
