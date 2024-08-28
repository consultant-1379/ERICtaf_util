package com.ericsson.cifwk.taf.performance.metric.graphite;

import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.ericsson.cifwk.taf.performance.metric.MetricsName;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
public class AmqpMetricsTest {

    @Test
    public void testName() {
        assertThat(
                AmqpMetrics.name("Prefix_1",
                        MetricsName.builder().suite("suite  1")
                                .group("group 1").test("test  1").build()),
                not(containsString(" ")));
    }

}
