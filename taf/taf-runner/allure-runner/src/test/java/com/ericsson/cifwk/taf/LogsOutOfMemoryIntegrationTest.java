package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.annotations.TestStep;
import org.junit.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.UUID;

import static com.ericsson.cifwk.taf.AllureTestUtils.runTestNg;

/**
 * Generates max log of 7 MB.
 * Test doesn't throw OutOfMemory after fix on TAF side.
 * Fix on Allure side remains to be implemented (API limitation).
 *
 * Test is excluded from CI as it requires setting JVM launching options.
 */
@Ignore
public class LogsOutOfMemoryIntegrationTest {

    @org.junit.Test
    // should be launched with VM options: -ea -Xms64m -Xmx64m
    public void outOfMemory() throws IOException {
        runTestNg(TestClass.class);
    }

    public static class TestClass {

        @Test
        public void parallelExecution() {
            generateLog(1);
            subStep();
            generateLog(1);
        }

        @TestStep(id = "sub-step")
        public void subStep() {
            generateLog(5);
        }

        @TestStep(id = "generate-logs")
        public void generateLog(int mB) {
            for (int i = 0; i < mB; i++) {
                System.out.println("Generated " + i + " kB");
                generate1MbLogs();
            }
        }

        private void generate1MbLogs() {
            for (int i = 0; i < 10 * 1024; i++) {
                System.out.println("Long long line having approximate size of 100 bytes randomized by UUID: " + UUID.randomUUID().toString());
            }
        }

    }

}
