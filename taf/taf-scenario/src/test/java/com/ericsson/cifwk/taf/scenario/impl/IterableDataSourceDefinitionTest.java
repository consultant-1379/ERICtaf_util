package com.ericsson.cifwk.taf.scenario.impl;


import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hamcrest.core.IsEqual;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class IterableDataSourceDefinitionTest {

    @Test
    public void testProvideIterator() throws Exception {
        List<Map<String, Object>> iterable = Lists.newArrayList();
        Map<String, Object> record = Maps.newHashMap();
        record.put("x", 1);
        iterable.add(record);
        IterableDataSourceDefinition definition = new IterableDataSourceDefinition("name", iterable);

        Supplier<Optional<DataRecord>> supplier = definition.provideSupplier(mock(ScenarioDataSourceContext.class), Maps.<String, DataRecord>newHashMap());
        Optional<DataRecord> next = supplier.get();
        assertThat(next.isPresent(), is(true));
        assertThat(next.get().getAllFields().get("x"), IsEqual.<Object>equalTo(1));
        assertThat(supplier.get().isPresent(), is(false));
    }

}