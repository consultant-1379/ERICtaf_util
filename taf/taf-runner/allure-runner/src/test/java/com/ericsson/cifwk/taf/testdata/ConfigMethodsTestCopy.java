package com.ericsson.cifwk.taf.testdata;

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
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         26/08/2015
 */
public class ConfigMethodsTestCopy {

    @BeforeSuite
    public void setUpSuite_C2() {
        System.out.println("setUpSuite");
    }

    @BeforeTest
    public void setUpTest_C2() {
        System.out.println("setUpTest");
    }

    @BeforeClass
    public void setUpClass_C2() {
        System.out.println("setUpClass");
    }

    @BeforeMethod
    public void setUpMethod_C2() {
        System.out.println("setUpMethod");
    }

    @Test
    @TestId(id = "TORF-66667_Func_1")
    public void success1_C2() {
        System.out.println("test");
    }

    @Test
    @TestId(id = "TORF-66667_Func_1")
    public void success2_C2() {
        System.out.println("test");
    }

    @Test
    @TestId(id = "TORF-66667_Func_1")
    public void success3_C2() {
        System.out.println("test");
    }

    @AfterMethod
    public void tearDownMethod_C2() {
        System.out.println("tearDownMethod");
    }

    @AfterClass
    public void tearDownClass_C2() {
        System.out.println("tearDownClass");
    }

    @AfterTest
    public void tearDownTest_C2() {
        System.out.println("tearDownTest");
    }

    @AfterSuite
    public void tearDownSuite_C2() {
        System.out.println("tearDownSuite");
    }
}
