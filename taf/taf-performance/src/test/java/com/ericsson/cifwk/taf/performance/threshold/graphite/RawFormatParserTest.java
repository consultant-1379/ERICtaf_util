package com.ericsson.cifwk.taf.performance.threshold.graphite;

import com.ericsson.cifwk.taf.performance.threshold.MetricSlice;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class RawFormatParserTest {

    private RawFormatParser parser;

    @Before
    public void setUp() {
        parser = new RawFormatParser();
    }

    @Test
    public void shouldRender() {
        String string = "x.y.z,1384869799,1384869809,1|None,None,None,None,None,None,None,24.0,None,None";

        MetricSlice data = parser.parse(string);

        assertThat(data.getTargetName(), equalTo("x.y.z"));
        assertThat(data.getStartTimestamp(), equalTo(1_384_869_799L));
        assertThat(data.getEndTimestamp(), equalTo(1_384_869_809L));
        assertThat(data.getSeriesStep(), equalTo(1));
        assertThat(data.getSize(), equalTo(10));
        assertThat(data.getData(), equalTo(new Double[]{null, null, null, null, null, null, null, 24.0, null, null}));
    }

}
