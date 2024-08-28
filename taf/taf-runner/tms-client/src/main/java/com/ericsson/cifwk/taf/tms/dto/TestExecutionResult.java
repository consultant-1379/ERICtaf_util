package com.ericsson.cifwk.taf.tms.dto;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by Mihails Volkovs <mihails.volkovs@ericsson.com>
 * 2015.07.29
 */
public enum TestExecutionResult {

    NOT_STARTED(1, "Not started"),
    PASS(2, "Pass"),
    PASSED_WITH_EXCEPTION(3, "Passed with exception"),
    FAIL(4, "Fail"),
    WIP(5, "Work in progress"),
    BLOCKED(6, "Blocked"),
    NOT_DEFINED(-1, "No execution result defined in TMS"),
    UNKNOWN(-2, "Unsupported test execution result value");

    private final Integer id;

    private final String name;

    public static final Map<String, TestExecutionResult> RESULTS_BY_ID = Maps.newHashMap();

    static {
        for (TestExecutionResult result : TestExecutionResult.values()) {
            RESULTS_BY_ID.put("" + result.getId(), result);
        }
    }

    TestExecutionResult(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static TestExecutionResult toEnum(ReferenceDataItem referenceData) {
        if (referenceData == null) {
            return NOT_DEFINED;
        }
        TestExecutionResult result = RESULTS_BY_ID.get(referenceData.getId());
        return result == null ? UNKNOWN : result;
    }

}
