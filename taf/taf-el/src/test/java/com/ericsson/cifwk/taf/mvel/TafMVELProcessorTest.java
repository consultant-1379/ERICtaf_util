package com.ericsson.cifwk.taf.mvel;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.spi.ConfigurationProvider;
import com.ericsson.cifwk.taf.spi.TestContextProvider;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

public class TafMVELProcessorTest {

    @Test
    public void testEvalIfExpression() throws Exception {
        assertThat(TafMVELProcessor.evalIfExpression("1", Integer.class), equalTo(1));
        assertThat(TafMVELProcessor.evalIfExpression("[a,b,c]", String[].class), equalTo(new String[]{"a", "b", "c"}));
        assertThat(TafMVELProcessor.evalIfExpression("${1+1}", Integer.class), equalTo(2));
    }

    @Test
    public void testEvalIfExpression_emptyStrings() throws Exception {
        assertThat(TafMVELProcessor.evalIfExpression("", Integer.class), equalTo(0));
        assertThat(TafMVELProcessor.evalIfExpression("", String.class), equalTo(""));
        assertThat(TafMVELProcessor.evalIfExpression("${}", Integer.class), nullValue());
        assertThat(TafMVELProcessor.evalIfExpression("${}", String.class), nullValue());
    }

    @Test
    public void testEvalIfExpression_TafTestContext() throws Exception {
        ServiceRegistry.getTestContextProvider().get().setAttribute("attribute", "value");
        assertThat(TafMVELProcessor.evalIfExpression("${attribute}", String.class), equalTo("value"));
        assertThat(TafMVELProcessor.evalIfExpression("attribute", String.class), equalTo("attribute"));
    }

    @Test
    public void testDoNotInitializeContext() throws Exception {
        TestContextProvider testContextProvider = ServiceRegistry.getTestContextProvider();
        testContextProvider.removeContext();
        assertFalse("Unable to check that context is not initialized by TafMVELProcessor" +
                " because context already initialized", testContextProvider.isContextInitialized());

        TafMVELProcessor.eval("1", new HashMap<String, Object>(), String.class);

        assertFalse(testContextProvider.isContextInitialized());
    }

    @Test
    public void testEvalIfExpression_TafConfiguration() throws Exception {

        ConfigurationProvider configurationProvider = ServiceRegistry.getConfigurationProvider();
        configurationProvider.get().setProperty("array", new String[]{"a", "b", "c"});
        assertThat(TafMVELProcessor.evalIfExpression("${configuration['array'][1]}", String.class), equalTo("b"));

        configurationProvider.get().setProperty("property", 1);
        assertThat(TafMVELProcessor.evalIfExpression("${configuration['property']}", Integer.class), equalTo(1));
        assertThat(TafMVELProcessor.evalIfExpression("${(configuration['property']+1) * 100}", Integer.class), equalTo(200));
        assertThat(TafMVELProcessor.evalIfExpression("configuration['property']", String.class), equalTo("configuration['property']"));
    }

    @Test
    public void testEval() throws Exception {
        final int VUSER = 3;
        final String ATTR = "attr";
        final String VALUE = "value";

        TestContextProvider testContextProvider = ServiceRegistry.getTestContextProvider();
        testContextProvider.initialize(VUSER);
        ImmutableMap<String, Object> additionalParams = ImmutableMap.of(ATTR, (Object) VALUE);

        assertThat(TafMVELProcessor.eval("$VUSER", additionalParams, Integer.class), equalTo(VUSER));
        assertThat(TafMVELProcessor.eval(ATTR, additionalParams, String.class), equalTo(VALUE));
    }
}
