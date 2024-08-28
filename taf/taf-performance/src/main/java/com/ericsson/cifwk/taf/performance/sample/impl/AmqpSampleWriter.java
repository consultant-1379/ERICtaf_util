package com.ericsson.cifwk.taf.performance.sample.impl;

import com.ericsson.cifwk.taf.performance.sample.Sample;
import com.ericsson.cifwk.taf.performance.sample.SampleWriter;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;

public final class AmqpSampleWriter implements SampleWriter {

    private final ThreadLocal<Kryo> kryo = new ThreadLocal<>();
    private final AmqpClient amqp;

    public static AmqpSampleWriter create(AmqpClient client) {
        return new AmqpSampleWriter(client);
    }

    AmqpSampleWriter(AmqpClient amqp) {
        this.amqp = amqp;
    }

    @Override
    public void initialize() throws IOException {
        amqp.connect();
    }

    @Override
    public void write(Sample sample) throws IOException {
        Kryo serializer = initializeKryo();
        byte[] bytes = serializeSample(serializer, sample);
        amqp.send(bytes);
    }

    private Kryo initializeKryo() {
        Kryo instance = kryo.get();
        if (instance == null) {
            kryo.set(new Kryo());
        }
        return kryo.get();
    }

    byte[] serializeSample(Kryo serializer, Sample sample) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes;
        try {
            Output output = new Output(outputStream);
            serializer.writeObject(output, sample);
            output.flush();
        } finally {
            bytes = outputStream.toByteArray();
            close(outputStream);
        }
        return bytes;
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void flush() throws IOException {
        // Handled by AMQP client
    }

    @Override
    public void close() throws IOException {
        amqp.shutdown();
    }
}
