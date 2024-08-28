package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.annotations.Attachment;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.annotations.TestStep;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class SampleTest {

    @Test
    @TestId(id = "OW-2_Func_2", title = "Testing attachment generation")
    public void testAttachments() {
        assertTrue(true);
        makeAttachment("attachment parameter");
        makeSimpleAttachment();
    }

    @Attachment(type = "text/plain", value = "Attachment name with parameter \"{0}\"")
    public byte[] makeAttachment(String parameter) {
        return ("Attachment content with parameter " + parameter).getBytes();
    }

    @Attachment
    public String makeSimpleAttachment() {
        return "Attachment without parameters";
    }

    @Test
    @TestId(id = "TORF-13991_Func_1", title = "Testing TE integration with TMS")
    public void testSteps() {
        System.out.println("Running a successful TAF test.");
        testStep1();
        testStep2();
        assertTrue(true);
        makeAttachment("Test");
    }

    @TestStep(id = "TS1")
    public void testStep1() {
        System.out.println("Inside test step 1.");
        nestedTestStep(11);
        makeAttachment("Test Step");
    }

    @TestStep(id = "TS2")
    public void testStep2() {
        System.out.println("Inside test step 2.");
        nestedTestStep(22);
        makeAttachment("Test Step");
    }

    @TestStep(id = "NTS", description = "NTS with {0} as step parameter")
    public void nestedTestStep(int arg) {
        System.out.println("Inside nested test step " + arg);
        makeSimpleAttachment();
        makeAttachment("Nested Test Step");
    }

    @Test
    @TestId(id = "TORF-13991_Func_2")
    public void tmsUs1() {
        assertTrue(true);
    }

    @Test
    @TestId(id = "TORF-13991_Func_3")
    public void tmsUs11() {
        assertTrue(true);
    }

    @Test
    @TestId(id = "TORF-13991_Func_4")
    public void tmsUs2() {
        assertTrue(true);
    }

    @Test
    @TestId(id = "TORF-13991_Func_5")
    public void testTe1() {
        assertTrue(true);
    }

    @Test
    @TestId(id = "TORF-13991_Func_NON_EXISTING")
    public void testTmsTeIntegration() {
        assertTrue(true);
    }

}
