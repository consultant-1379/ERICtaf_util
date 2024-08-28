package com.ericsson.cifwk.taf.tms.dto;


import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@SuppressWarnings("unused")
public class TestStepInfo {

    private String name;

    private List<TestStepInfo> verifies = newArrayList();

    public String getName() {
        return name;
    }

    public List<TestStepInfo> getVerifies() {
        return verifies;
    }
}
