package com.ericsson.cifwk.taf.scenario.impl;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;
import com.google.common.collect.Sets;

@API(Internal)
public class FlowExecution {
    private AtomicInteger count = new AtomicInteger();
    private Set<DataSourceDefinition> dataSources = Sets.newConcurrentHashSet();
    private String name;

    public FlowExecution(final String name) {
        this.name = name;
    }

    public void add(int add, DataSourceDefinition[] dataSources) {
        this.count.addAndGet(add);
        this.dataSources.addAll(Arrays.asList(dataSources));
    }

    public int getExecutionCount() {
        return count.intValue();
    }

    public String getName() {
        return name;
    }

    public List<String> getFlowDataSourceNames() {
        List<String> flowDataSourceNames = new ArrayList<>();
        for(DataSourceDefinition def: dataSources){
            flowDataSourceNames.add(def.getName());
        }
        return flowDataSourceNames;
    }

    public boolean checkAllDataSourcesAllowEmpty() {
        for (DataSourceDefinition ds : dataSources) {
            if (!ds.allowsEmpty()) {
                return false;
            }
        }
        return true;
    }
}
