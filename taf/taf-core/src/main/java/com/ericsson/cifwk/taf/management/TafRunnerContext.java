package com.ericsson.cifwk.taf.management;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public final class TafRunnerContext {

    private static final TafRunnerContext context;

    private final List<Closeable> closeables = new ArrayList<>();

    static {
        String tafName = System.getProperty("taf.groupId", "com.ericsson.cifwk.taf") +
                ":" + System.getProperty("taf.artifactId", "testcases") +
                ":" + System.getProperty("taf.version", "0");
        context = new TafRunnerContext(tafName);
    }

    private TafRunnerContext() {
    }

    public static TafRunnerContext getContext() {
        return context;
    }

    private volatile boolean interrupted = false;
    private volatile boolean terminated = false;
    private String name;

    public TafRunnerContext(String name) {
        this.name = name;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public void interrupt() {
        this.interrupted = true;
    }

    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public String getName() {
        return name;
    }

    public synchronized void addCloseable(Closeable closeable) {
        closeables.add(closeable);
    }

    public synchronized List<Closeable> getCloseables() {
        return new ArrayList<>(closeables);
    }

}
