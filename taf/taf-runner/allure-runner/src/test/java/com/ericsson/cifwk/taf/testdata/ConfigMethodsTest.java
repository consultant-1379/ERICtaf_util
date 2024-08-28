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
public class ConfigMethodsTest {

    @BeforeSuite
    public void setUpSuite() {
        System.out.println("setUpSuite");
    }

    @BeforeTest
    public void setUpTest() {
        System.out.println("setUpTest");
    }

    @BeforeClass
    public void setUpClass() {
        System.out.println("setUpClass");
    }

    @BeforeMethod
    public void setUpMethod() {
        System.out.println("setUpMethod");
    }

    @Test
    @TestId(id = "TORF-49968")
    public void success1() {
        System.out.println("test");
    }

    @Test
    @TestId(id = "TORF-49968")
    public void success2() {
        System.out.println("test");
    }

    @Test
    @TestId(id = "TORF-49968")
    public void success3() {
        System.out.println("test");
    }

    @AfterMethod
    public void tearDownMethod() {
        System.out.println("tearDownMethod");
    }

    @AfterClass
    public void tearDownClass() {
        System.out.println("tearDownClass");
    }

    @AfterTest
    public void tearDownTest() {
        System.out.println("tearDownTest");
    }

    @AfterSuite
    public void tearDownSuite() {
        System.out.println("tearDownSuite");
    }
}
