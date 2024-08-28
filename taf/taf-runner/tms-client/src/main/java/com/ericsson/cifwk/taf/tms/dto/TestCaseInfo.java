/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.tms.dto;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@SuppressWarnings("unused")
public class TestCaseInfo {

    private long id;

    private String testCaseId;

    private String title;

    private String description;

    private List<ReferenceDataItem> technicalComponents;

    private ReferenceDataItem priority;

    private ReferenceDataItem type;

    private ReferenceDataItem executionType;

    private ReferenceDataItem testCaseStatus;

    private List<ReferenceDataItem> contexts = newArrayList();

    private List<ReferenceDataItem> groups = newArrayList();

    private List<RequirementInfo> requirements = newArrayList();

    private List<TestStepInfo> testSteps = newArrayList();

    /*
     * Optional fields
     */
    private String precondition;
    private String comment;
    private String packageName;
    /*
     * End of optional fields
     */

    public long getId() {
        return id;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    public String getTitle() {
        return title;
    }

    public List<ReferenceDataItem> getTechnicalComponents() {
        return technicalComponents;
    }

    public ReferenceDataItem getPriority() {
        return priority;
    }

    public ReferenceDataItem getType() {
        return type;
    }

    public List<RequirementInfo> getRequirements() {
        return requirements;
    }

    public String getDescription() {
        return description;
    }

    public ReferenceDataItem getExecutionType() {
        return executionType;
    }

    public String getPrecondition() {
        return precondition;
    }

    public String getComment() {
        return comment;
    }

    public String getPackageName() {
        return packageName;
    }

    public ReferenceDataItem getTestCaseStatus() {
        return testCaseStatus;
    }

    public List<ReferenceDataItem> getContexts() {
        return contexts;
    }

    public List<TestStepInfo> getTestSteps() {
        return testSteps;
    }

    public List<ReferenceDataItem> getGroups() {
        return groups;
    }
}
