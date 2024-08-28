package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.management.TafRunnerContext;
import org.testng.ITestNGMethod;
import org.testng.internal.thread.graph.IWorker;

import java.util.List;

public class WorkerProxy implements IWorker<ITestNGMethod> {

    IWorker<ITestNGMethod> worker;

    public WorkerProxy(IWorker<ITestNGMethod> worker) {
        this.worker = worker;
    }

    @Override
    public void run() {
        if (!TafRunnerContext.getContext().isInterrupted()) {
            worker.run(); // NOSONAR
        }
    }

    @Override
    public List<ITestNGMethod> getTasks() {
        return worker.getTasks();
    }

    @Override
    public long getTimeOut() {
        return worker.getTimeOut();
    }

    @Override
    public int getPriority() {
        return worker.getPriority();
    }

    @Override
    public int compareTo(IWorker<ITestNGMethod> o) {
        return worker.compareTo(o);
    }

}
