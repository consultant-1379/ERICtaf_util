/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.ext.exporter;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.scenario.impl.RunnableInvocation;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertThat;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.TestStepNode;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;

public class TooltipGeneratorTest {
    final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    final TooltipGenerator tooltipGenerator = new TooltipGenerator(true);

    @Test
    public void testComplete() throws Exception {
        final TooltipGenerator tooltipGenerator = new TooltipGenerator(true);

        ImmutableMap<String, Object> map = new ImmutableMap.Builder<String, Object>()
                .put("one", 1)
                .put("two", new String[]{"aaa", "bbb", "ccc"})
                .put("three", 3)
                .build();

        DataRecordImpl dataRecord = new DataRecordImpl(map);

        LinkedHashMap<String, DataRecord> dataSources = new LinkedHashMap<>(new ImmutableMap.Builder<String, DataRecord>()
                .put("ds", dataRecord)
                .build());

        TestStepNode node = new TestStepNode(new RunnableInvocation(new Runnable() {
            @Override
            public void run() {

            }
        }), dataSources, 1, null);

        String result = tooltipGenerator.export(node);
        assertThat(result, containsString("<td>one</td>"));
        assertThat(result, containsString("<td>[aaa, bbb, ccc]</td>"));
        assertThat(result, containsString("<td>three</td>"));
    }

    @Test
    public void testObjectToString() throws Exception {
        assertThat(
                tooltipGenerator.objectToString(new String[]{"aaa", "bbb", "ccc"}),
                equalTo("[aaa, bbb, ccc]")
        );
        assertThat(
                tooltipGenerator.objectToString(null),
                equalTo("null")
        );
    }

    @Test
    public void testBreakLongLine() throws Exception {
        String d = "<br/>";
        assertThat(
                tooltipGenerator.breakLongLine("123456789012345678901234567890123", 10, d),
                equalTo("1234567890" + d + "1234567890" + d + "1234567890" + d + "123")
        );
        assertThat(
                tooltipGenerator.breakLongLine("123456789012345678901234567890", 10, d),
                equalTo("1234567890" + d + "1234567890" + d + "1234567890")
        );
        assertThat(
                tooltipGenerator.breakLongLine("123456789", 10, d),
                equalTo("123456789")
        );
        assertThat(
                tooltipGenerator.breakLongLine("1234567890", 10, d),
                equalTo("1234567890")
        );
        assertThat(
                tooltipGenerator.breakLongLine("", 10, d),
                equalTo("")
        );
    }
}