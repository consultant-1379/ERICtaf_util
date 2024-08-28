package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.management.TafRunnerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IClassListener;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.TestRunner;
import org.testng.internal.IConfiguration;
import org.testng.internal.thread.graph.IWorker;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class TafTestRunner extends TestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TafTestRunner.class);

    private final ISuite suite;
    protected final XmlTest test;
    private final Collection<IInvokedMethodListener> invokedMethodListeners;
    private final List<IClassListener> classListeners;
    private SuiteReader suiteReader;
    private Invoker<TestRunner> runnerInvoker;
    private TafTestRunnerFactory vuserFactory;
    protected int vUser = 1;
    protected int repeatCount;
    protected long repeatUntil;

    public TafTestRunner(IConfiguration configuration, ISuite suite,
                         XmlTest test, boolean skipFailedInvocationCounts,
                         Collection<IInvokedMethodListener> invokedMethodListeners,
                         List<IClassListener> classListeners) {
        super(configuration, suite, test, skipFailedInvocationCounts, invokedMethodListeners, classListeners);
        this.suite = suite;
        this.test = test;
        this.invokedMethodListeners = invokedMethodListeners;
        this.classListeners = classListeners;
    }

    public TafTestRunner withVUser(int vUser) {
        this.vUser = vUser;
        return this;
    }

    SuiteReader getSuiteReader() {
        if (suiteReader == null) {
            suiteReader = new SuiteReader(suite);
        }
        return suiteReader;
    }

    void setSuiteReader(SuiteReader suiteReader) {
        this.suiteReader = suiteReader;
    }

    Invoker<TestRunner> getRunnerInvoker() {
        if (runnerInvoker == null) {
            runnerInvoker = new Invoker<>(this, TestRunner.class);
        }
        return runnerInvoker;
    }

    void setRunnerInvoker(Invoker<TestRunner> runnerInvoker) {
        this.runnerInvoker = runnerInvoker;
    }

    TafTestRunnerFactory getVuserFactory() {
        if (vuserFactory == null) {
            vuserFactory = new TafTestRunnerFactory();
        }
        return vuserFactory;
    }

    void setVuserFactory(TafTestRunnerFactory vuserFactory) {
        this.vuserFactory = vuserFactory;
    }

    @Override
    public void run() {
        XmlTest currentTest = getTest();
        if (currentTest.isJUnit()) {
            super.run();
            return;
        }

        SuiteReader localSuiteReader = getSuiteReader();
        String cron = localSuiteReader.getCronExpression();
        int vusers = localSuiteReader.getVusers();
        long startDelay = localSuiteReader.getStartDelay();

        boolean isScheduled = cron != null;
        boolean isConcurrent = vusers >= 2;
        delay(startDelay);
        if(isScheduled){
            throw new RuntimeException("Scheduled Task no longer supported due to the removal of quartz");
        }else if (isConcurrent) {
            runConcurrent(vusers);
        } else {
            runSimple();
        }
    }

    private void runSimple() {
        setRepeatCountAndRepeatUntil();
        invokeBeforeRun();
        try {
            do {
                invokePrivateRun();
                repeatCount--;
            } while (shouldRepeat(repeatCount, repeatUntil));
        } finally {
            invokeAfterRun();
        }
    }

    protected boolean shouldRepeat(int repeatCount, long repeatUntil) {
        return (!TafRunnerContext.getContext().isInterrupted())
                && repeatCount > 0 && System.currentTimeMillis() < repeatUntil;
    }

    private void runConcurrent(int vusers) {
        invokeBeforeRun();
        ExecutorService es = Executors.newFixedThreadPool(vusers);
        List<TestRunnerCallable> tasks = new ArrayList<>(vusers);
        for (int user = 1; user <= vusers; user++) {
            TafTestRunner runner = getVuserFactory()
                    .newVUserTestRunner(suite, test, invokedMethodListeners, classListeners)
                    .withVUser(user);
            tasks.add(new TestRunnerCallable(runner));
        }
        try {
            LOGGER.info("Starting concurrent test execution");
            completeAll(es.invokeAll(tasks));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            LOGGER.info("Shutting down VUser pool");
            es.shutdown();
            invokeAfterRun();
        }
    }

    private void completeAll(List<Future<Void>> futures) throws InterruptedException {
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw new VUserException("VUser threw an exception", e);
            }
        }
    }



    protected void invokeBeforeRun() {
        getRunnerInvoker().invoke("beforeRun");
    }

    protected void invokePrivateRun() {
        getRunnerInvoker().invoke("privateRun", test);
    }

    protected void invokeAfterRun() {
        getRunnerInvoker().invoke("afterRun");
    }

    protected void delay(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public List<IWorker<ITestNGMethod>> createWorkers(
            List<ITestNGMethod> methods) {
        List<IWorker<ITestNGMethod>> workers = super.createWorkers(methods);
        List<IWorker<ITestNGMethod>> proxies = new ArrayList<>(
                workers.size());
        for (IWorker<ITestNGMethod> worker : workers) {
            IWorker<ITestNGMethod> proxyWorker = new WorkerProxy(worker);
            proxies.add(proxyWorker);
        }
        return proxies;
    }

    protected void setRepeatCountAndRepeatUntil() {
        boolean repeatCountIsSet = getSuiteReader().isRepeatCountSet();
        boolean repeatUntilIsSet = getSuiteReader().isRepeatUntilSet();

        if (repeatCountIsSet && repeatUntilIsSet) {
            repeatCount = getSuiteReader().getRepeatCount();
            repeatUntil = getSuiteReader().getRepeatUntil();
        } else if (repeatCountIsSet) {
            repeatCount = getSuiteReader().getRepeatCount();
            repeatUntil = SuiteReader.DEFAULT_REPEAT_UNTIL;
        } else if (repeatUntilIsSet) {
            repeatCount = Integer.MAX_VALUE;
            repeatUntil = getSuiteReader().getRepeatUntil();
        } else {
            repeatCount = SuiteReader.DEFAULT_REPEAT_COUNT;
            repeatUntil = SuiteReader.DEFAULT_REPEAT_UNTIL;
        }
    }
}
