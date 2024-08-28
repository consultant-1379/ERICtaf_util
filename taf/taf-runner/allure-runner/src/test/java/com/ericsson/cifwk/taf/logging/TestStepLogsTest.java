package com.ericsson.cifwk.taf.logging;

import org.junit.Assert;
import org.junit.Test;

import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.is;

public class TestStepLogsTest {

    @Test
    public void singleThread() {
        PrintStream out = new PrintStream(TestStepLogs.INSTANCE);
        out.print("Before...");
        TestStepLogs.addLog();
        out.print("TestStep|");
        TestStepLogs.addLog();
        out.print("NestedTestStep|");
        Assert.assertThat(new String(TestStepLogs.removeLog()), is("NestedTestStep|"));
        Assert.assertThat(new String(TestStepLogs.removeLog()), is("TestStep|NestedTestStep|"));
        Assert.assertThat(new String(TestStepLogs.removeLog()), is(""));
    }

    @Test
    public void parallelExecution() throws InterruptedException {
        final PrintStream out = new PrintStream(TestStepLogs.INSTANCE);
        out.print("Before...");
        TestStepLogs.addLog();
        out.print("Test|");

        LoggingThread loggingThread = new LoggingThread(out, "1");
        loggingThread.start();
        loggingThread.join();

        Assert.assertThat(new String(TestStepLogs.removeLog()), is("Test|"));
        Assert.assertThat(new String(TestStepLogs.removeLog()), is(""));
    }

    private static class LoggingThread extends Thread {

        private PrintStream out;

        private LoggingThread(PrintStream out, String name) {
            super(name);
            this.out = out;
        }

        @Override
        public void run() {
            TestStepLogs.addLog();
            out.print("child thread: " + getName());
            TestStepLogs.removeLog();
        }

    }
}
