package com.ericsson.cifwk.taf.performance.sample.impl;

import com.ericsson.cifwk.taf.performance.sample.Sample;
import com.ericsson.cifwk.taf.performance.sample.SampleBuilder;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class AmqpSampleWriterTest {

    private AmqpSampleWriter writer;

    @Before
    public void setUp() {
        writer = new AmqpSampleWriter(mock(AmqpClient.class));
    }

    @Test
    public void testSerializeSample() throws Exception {
        Kryo kryo = new Kryo();
        SampleBuilder builder = Sample.builder();
        builder
                .eventTime(new Date())
                .testCase("tc")
                .testSuite("ts")
                .executionId("ID")
                .protocol("http")
                .requestType("GET")
                .target(URI.create("http://domain.com"))
                .vuserId("1");
        Sample sample = builder.build();

        byte[] bytes = writer.serializeSample(kryo, sample);

        Sample result = kryo.readObject(new Input(bytes), Sample.class);
        assertThat(result.equals(sample), is(true));
    }

}