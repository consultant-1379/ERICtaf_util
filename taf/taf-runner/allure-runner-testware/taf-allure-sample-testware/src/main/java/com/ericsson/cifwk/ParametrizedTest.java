package com.ericsson.cifwk;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.annotations.TestId;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Created by emihvol on 5/15/2015.
 */
public class ParametrizedTest {

    @Test(dataProvider = "dataProvider", enabled = true)
    public void parametrizedTest(@TestId @Input("Test ID") String testId, @Input("#") int parameter, @Output("Expected value") String expected) {
        // do nothing
    }

    @DataProvider
    public Object[][] dataProvider() {
        return new Object[][]{
                {"UNKNOWN_ID", 1, "String"},
                {"", 2, ""},
                {"TORF-37961_Func-2", 3, "Long text here very long text here very long text here very long text here very long text here very long text here very long text here very long text here very long text here very long text here very long text here very"},
                {"TORF-37961_Func-2", 4, "Something"},
                {"TORF-37961_Func-2", 5, "Some Value"}
        };
    }

}
