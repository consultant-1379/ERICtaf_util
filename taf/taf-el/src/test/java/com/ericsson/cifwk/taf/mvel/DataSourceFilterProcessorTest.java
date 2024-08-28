package com.ericsson.cifwk.taf.mvel;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataSourceFilterProcessorTest {

    @Test
    public void blankFilterApplies() throws Exception {
        Map<String, Object> row = Maps.newHashMap();
        row.put("x", 1);
        row.put("y", "false");
        row.put("z", "lolcat");

        assertTrue(applyFilterFor(row, null));
        assertTrue(applyFilterFor(row, ""));
        assertTrue(applyFilterFor(row, " "));
    }

    @Test
    public void testApplySimpleFilter() throws Exception {
        Map<String, Object> row = Maps.newHashMap();
        row.put("x", 1);
        row.put("y", "false");
        row.put("z", "lolcat");

        assertTrue(applyFilterFor(row, "x==1"));
        assertTrue(applyFilterFor(row, "x=='1'"));
        assertFalse(applyFilterFor(row, "x==2"));
        assertTrue(applyFilterFor(row, "x>-1"));

        assertTrue(applyFilterFor(row, "y==false"));
        assertTrue(applyFilterFor(row, "y!=true"));

        assertFalse(applyFilterFor(row, "x==1 && z=='lol'"));
        assertTrue(applyFilterFor(row, "x==1 && z=='lolcat'"));
        assertTrue(applyFilterFor(row, "x==1 || z=='lolcat'"));
    }

    @Test
    public void testApplyVuserFilter() throws Exception {
        Map<String, Object> row = Maps.newHashMap();
        row.put("w", "$#@");
        row.put("x", 1);
        row.put("y", 123);
        row.put("z", 456);

        final int vUser = 123;

        ServiceRegistry.getTestContextProvider().initialize(vUser);
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        assertEquals(vUser, context.getVUser());

        assertFalse(applyFilterFor(row, "x==1 && z==$VUSER"));
        assertTrue(applyFilterFor(row, "x==1 && y==$VUSER"));
        assertTrue(applyFilterFor(row, "(x==1 && y==$VUSER) && w=='$#@'"));

        context.setAttribute("myAttr", "myAttrValue");
        assertTrue(applyFilterFor(row, "x==1 && myAttr=='myAttrValue'"));
        assertFalse(applyFilterFor(row, "x==1 && myAttr=='foo'"));
    }

    @Test
    public void testObjectInvocationInFilter() throws Exception {
        Map<String, Object> row = Maps.newHashMap();
        MyPojo myPojo = new MyPojo("a", -678);
        row.put("myObj", myPojo);
        String[] myArray = new String[] { "arrElt1", "arrElt2" };
        row.put("myArray", myArray);
        List<Long> myList = Lists.newArrayList();
        myList.add(11L);
        row.put("myList", myList);
        Map<String, Object> myMap = Maps.newHashMap();
        myMap.put("userName", "Sven");
        myMap.put("myMapPojo", myPojo);
        row.put("myMap", myMap);

        // EL based on MVEL, examples here: http://mvel.codehaus.org/MVEL+2.0+Property+Navigation
        assertTrue(applyFilterFor(row, "myObj.myString=='a' && myObj.myInt==-678"));
        assertTrue(applyFilterFor(row, "myList[0]==11 && myArray[1]=='arrElt2' && myArray[0]!='arrElt2'"));
        assertTrue(applyFilterFor(row, "myMap['userName']=='Sven' && myMap['myMapPojo'].myString==myObj.myString"));
    }

    @Test
    public void testMvelTemplate() {
        Map<String, Object> attributes = Maps.newHashMap();
        attributes.put("vuser", 123);
        assertEquals("name=123", filterFromTemplate("name=@{vuser}", attributes));
        assertEquals("name=123 && x==y && z==34.1", filterFromTemplate("name=@{vuser} && x==y && z==34.1", attributes));

        attributes.put("vuser", "user1");
        assertEquals("name=user1", filterFromTemplate("name=@{vuser}", attributes));
        assertEquals("name=user1 && x==y && z==34.1", filterFromTemplate("name=@{vuser} && x==y && z==34.1", attributes));
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailIfNotPossibleToParse() {
        Map<String, Object> row = Maps.newHashMap();
        assertFalse(applyFilterFor(row, "unknownVar==2"));
    }

    private boolean applyFilterFor(Map<String, Object> row, String filter) {
        return DataSourceFilterProcessor.applyFilter(TestDataSourceFactory.createDataRecord(row), filter);
    }

    private String filterFromTemplate(String tpl, Map<String, Object> attributes) {
        CompiledTemplate compiledTemplate = TemplateCompiler.compileTemplate(tpl);
        return (String) TemplateRuntime.execute(compiledTemplate, attributes);
    }

    private class MyPojo {
        private final String myString;
        private final Integer myInt;

        MyPojo(String myString, Integer myInt) {
            this.myString = myString;
            this.myInt = myInt;
        }

        public String getMyString() {
            return myString;
        }

        public Integer getMyInt() {
            return myInt;
        }
    }
}
