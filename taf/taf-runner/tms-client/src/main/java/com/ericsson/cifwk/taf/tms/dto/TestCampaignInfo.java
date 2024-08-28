package com.ericsson.cifwk.taf.tms.dto;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
/**
 * Created by ekonsla on 05/08/2016.
 */
public class TestCampaignInfo {

    private long id;

    private String name;

    private String description;

    private String environment;

    private ProjectInfo project;

    private List<TestCampaignItemInfo> testCampaignItems = newArrayList();

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getEnvironment() {
        return environment;
    }

    public ProjectInfo getProject() {
        return project;
    }

    public List<TestCampaignItemInfo> getTestCampaignItems() {
        return testCampaignItems;
    }
}
