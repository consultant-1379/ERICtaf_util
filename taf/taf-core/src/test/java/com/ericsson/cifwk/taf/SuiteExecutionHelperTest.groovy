package com.ericsson.cifwk.taf

import groovy.util.logging.Log4j
import org.junit.Test

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import static org.junit.Assert.assertTrue;

@Log4j
class SuiteExecutionHelperTest {

    private static BlockingQueue<String> results = new ArrayBlockingQueue<String>(8)
    private static BlockingQueue<String> runners = new ArrayBlockingQueue<String>(3)
    class ParallelRunner extends Thread {

        public void run() {
            Thread.currentThread().name = "ParallelRunner" + Thread.currentThread().id
            SuiteExecutionHelper.requestParallelRun()
            results.add(Thread.currentThread().name)
            sleep(3)
            SuiteExecutionHelper.finishExecution()
        }
    }

    class ExplicitRunner extends Thread {
        public void run() {
            Thread.currentThread().name = "ExplicitRunner" + Thread.currentThread().id
            SuiteExecutionHelper.requestExclusiveRun()
            runners << 1
            SuiteExecutionHelper.requestParallelRun()
            while (results.peek()?.startsWith("ParallelRunner")) results.poll()
            sleep(3)
            results.add(Thread.currentThread().name)
            SuiteExecutionHelper.finishExecution()
            SuiteExecutionHelper.releaseExclusiveRun()
        }
    }

    private void prepareParallelThreads(int threadCount) {
        5.times {
            new ParallelRunner().with { setDaemon(true); start() }
        }
        while (results.size() < threadCount) {
        }
    }

    private void prepareExclussiveThreads(int threadCount) {
        threadCount.times {
            new ExplicitRunner().with { setDaemon(true); start() }
        }
        while (runners.size() < threadCount) {
        }
    }

    private void checkOrderOfResults(int explicitRunners, int parallelRunners) {
        explicitRunners.times {
            assertTrue(results.poll()?.contains("ExplicitRunner"))
        }
        parallelRunners.times {
            assertTrue(results.poll()?.contains("ParallelRunner"))
        }
    }

    @Test
    public void should_execute_threads_in_proper_order() {

        prepareParallelThreads(5)
        prepareExclussiveThreads(3)
        prepareParallelThreads(8)
        checkOrderOfResults(3, 5)
    }
}
