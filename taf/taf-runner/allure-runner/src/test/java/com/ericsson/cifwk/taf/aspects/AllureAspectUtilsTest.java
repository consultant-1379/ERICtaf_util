package com.ericsson.cifwk.taf.aspects;

import org.junit.Test;

import static com.ericsson.cifwk.taf.aspects.AllureAspectUtils.renderTemplate;
import static org.junit.Assert.assertEquals;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 25.05.2016
 */
public class AllureAspectUtilsTest {

    @Test
    public void testRenderTemplate() {
        String template = "a {this}.{method} with params {0} and {1}";
        Object[] parameters = {"param1", new Object[]{"param2", "param3"}};
        String result = renderTemplate(template, "methodName", "instance", parameters);
        assertEquals("a instance.methodName with params param1 and [param2, param3]", result);
    }

    @Test
    public void renderTemplateWithQuotes() {
        String template = "\"{0}\" and \'{1}\' and '{2}' and {3}";
        Object[] parameters = {"param1", "param2", "param3", "param4"};
        String result = renderTemplate(template, "", "", parameters);
        assertEquals("\"param1\" and \"param2\" and \"param3\" and param4", result);
    }

    @Test
    public void renderTemplateWithNullParameters() {
        Object[] parameters = {null, new Object[]{null}};
        String result = renderTemplate("a {this}.{method} with params {0} and {1}", null, null, parameters);
        assertEquals("a null.null with params null and [null]", result);

        result = renderTemplate("{0}", null, null, null);
        assertEquals("{0}", result);
    }

}