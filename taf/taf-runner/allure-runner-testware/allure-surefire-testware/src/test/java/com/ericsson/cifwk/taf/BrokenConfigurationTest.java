package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.annotations.TestId;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Mihails Volkovs
 *         Date: 09.04.15
 */
public class BrokenConfigurationTest {

    private static int counter = 0;

    @BeforeSuite
    public void setUpSuite() {
        System.out.println("setUpSuite");
        setUpSuiteStep();
//        throw new RuntimeException("Exception by design");
    }

    @BeforeTest
    public void setUpTest() {
        System.out.println("setUpTest");
        setUpTestStep();
//        throw new RuntimeException("Exception by design");
    }

    @BeforeClass
    public void setUpClass() {
        System.out.println("setUpClass");
        setUpClassStep();
//        throw new RuntimeException("Exception by design");
    }

    @BeforeMethod
    public void setUpMethod() {
        System.out.println("setUpMethod");
        setUpMethodStep();
//        if (++counter == 2) {
//            throw new RuntimeException("Exception by design");
//        }
    }

    @Test
    @TestId(id = "TORF-49968")
    public void success1() {
        System.out.println("test");
        testStep();
    }

    @Test
    @TestId(id = "TORF-49968")
    public void success2() {
        System.out.println("test");
        testStep();
    }

    @Test
    @TestId(id = "TORF-49968")
    public void success3() {
        System.out.println("test");
        testStep();
    }

    @AfterMethod
    public void tearDownMethod() {
        System.out.println("tearDownMethod");
        tearDownMethodStep();
//        throw new RuntimeException("Exception by design");
    }

    @AfterClass
    public void tearDownClass() {
        System.out.println("tearDownClass");
        tearDownClassStep();
//        throw new RuntimeException("Exception by design");
    }

    @AfterTest
    public void tearDownTest() {
        System.out.println("tearDownTest");
        tearDownTestStep();
//        throw new RuntimeException("Exception by design");
    }

    @AfterSuite
    public void tearDownSuite() {
        System.out.println("tearDownSuite");
        tearDownSuiteStep();
//        throw new RuntimeException("Exception by design");
    }

//    @TestStep(id = "setUp")
    public void setUpSuiteStep() {
        // do nothing
    }

//    @TestStep(id = "tearDown")
    public void tearDownSuiteStep() {
        // do nothing
    }

//    @TestStep(id = "setUp")
    public void setUpTestStep() {
        // do nothing
    }

//    @TestStep(id = "tearDown")
    public void tearDownTestStep() {
        // do nothing
    }

//    @TestStep(id = "setUp")
    public void setUpClassStep() {
        // do nothing
    }

//    @TestStep(id = "tearDown")
    public void tearDownClassStep() {
        // do nothing
    }

//    @TestStep(id = "setUp")
    public void setUpMethodStep() {
        // do nothing
    }

//    @TestStep(id = "tearDown")
    public void tearDownMethodStep() {
        // do nothing
    }

//    @TestStep(id = "test")
    public void testStep() {
        // do nothing
    }

}
