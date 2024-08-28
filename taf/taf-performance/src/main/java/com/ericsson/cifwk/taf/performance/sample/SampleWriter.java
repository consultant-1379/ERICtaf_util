package com.ericsson.cifwk.taf.performance.sample;

import java.io.Closeable;
import java.io.IOException;

public interface SampleWriter extends Closeable {

    void initialize() throws IOException;

    void write(Sample sample) throws IOException;

    void flush() throws IOException;

}
