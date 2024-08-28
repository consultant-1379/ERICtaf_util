package com.ericsson.cifwk.taf.logging;

import org.apache.commons.io.output.NullOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author  Mihails Volkovs mihails.volkovs@ericsson.com
 *          2015.03.05.
 */
public class TestStepLogs extends OutputStream {

    public static final TestStepLogs INSTANCE = new TestStepLogs();

    private static DequeThreadLocal printStreams = new DequeThreadLocal();

    private TestStepLogs() {
        // hiding constructor
    }

    public static void addLog() {
        PrintStream printStream = getPrintStream();
        printStreams.get().push(new PrintStreamSplitter(printStream));
    }

    public static byte[] removeLog() {
        Deque<PrintStreamSplitter> stack = getStack();
        if (!stack.isEmpty()) {
            PrintStreamSplitter printStream = stack.pop();
            return printStream.toContent();
        }
        return new byte[0];
    }

    private static PrintStream getPrintStream() {
        Deque<PrintStreamSplitter> stack = printStreams.get();
        if (stack.isEmpty()) {
            return new PrintStream(new NullOutputStream());
        }
        return stack.peek();
    }

    private static Deque<PrintStreamSplitter> getStack() {
        return printStreams.get();
    }

    @Override
    public void write(int i) throws IOException {
        getPrintStream().write(i);
    }

    private static class DequeThreadLocal extends ThreadLocal<Deque<PrintStreamSplitter>> {

        @Override
        protected Deque<PrintStreamSplitter> initialValue() {
            return new ArrayDeque<>();
        }
    }

}
