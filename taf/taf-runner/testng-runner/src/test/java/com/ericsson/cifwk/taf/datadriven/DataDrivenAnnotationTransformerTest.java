package com.ericsson.cifwk.taf.datadriven;

import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

/**
 *
 */
public class DataDrivenAnnotationTransformerTest {

    private static boolean failed;

    @org.junit.Before
    public void setUp() {
        failed = false;
    }

    @org.junit.Test
    public void shouldRunWithData() {
        runTestNg();
        assertTrue(!failed);
    }

    private void runTestNg() {
        TestNG testNG = new TestNG(false);
        testNG.setTestClasses(new Class[]{DataDrivenTest.class});
        testNG.addListener(new DataDrivenAnnotationTransformer());
        testNG.addListener(new FailureListener());
        testNG.run();
    }

    public static class DataDrivenTest {

        @Test
        @DataDriven(name = "calculator")
        public void simpleCase(@Input("x") int x, @Input("y") int y, @Output("z") int z) {
            assertEquals(x + y, z);
        }

        @Test
        @DataDriven(name = "calculator")
        public void typeConversionOfSameParam(@Input("x") String x1, @Input("x") Integer x3) {
            assertEquals(x1, x3.toString());
        }

        @Test
        @DataDriven(name = "calculator")
        public void beanTypeConversion(@Input("calculator") CalculatorData data) {
            assertThat(data.getX() + data.getY(), equalTo(Integer.parseInt(data.getZ())));
        }

        @Test
        @DataDriven(name = "calculator")
        public void missingProperties(@Input("x") Integer x, @Input("q") String missing) {
            assertNotNull(x);
            assertNull(missing);
        }

    }

    public static class CalculatorData {

        private int x;
        private int y;
        private String z;

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public String getZ() {
            return z;
        }

        public void setZ(String z) {
            this.z = z;
        }
    }

    public static class FailureListener extends AbstractTestListener {
        @Override
        public void onTestFailure(ITestResult result) {
            System.out.println(result.getMethod().getMethodName() + "() : failed!");
            failed = true;
        }
    }

}
