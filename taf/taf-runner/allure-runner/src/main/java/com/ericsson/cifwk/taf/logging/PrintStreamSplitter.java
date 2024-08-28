package com.ericsson.cifwk.taf.logging;

import org.apache.commons.io.output.TeeOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author  Mihails Volkovs mihails.volkovs@ericsson.com
 *          2015.03.05.
 */
public class PrintStreamSplitter extends PrintStream {

    private ByteArrayOutputStream content;

    public PrintStreamSplitter(OutputStream branch) {
        this(new ByteArrayOutputStream(), branch);
    }

    public PrintStreamSplitter(ByteArrayOutputStream out, OutputStream branch) {
        super(new TeeOutputStream(out, branch));
        content = out;
    }

    public PrintStreamSplitter(OutputStream out, OutputStream branch) {
        super(new TeeOutputStream(out, branch));
        content = new ByteArrayOutputStream();
    }

    public byte[] toContent() {
        return content.toByteArray();
    }
}
