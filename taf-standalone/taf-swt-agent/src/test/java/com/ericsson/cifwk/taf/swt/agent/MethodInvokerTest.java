package com.ericsson.cifwk.taf.swt.agent;

import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.sdk.SwtSelect;
import com.ericsson.cifwk.taf.ui.sdk.ViewModel;
import com.ericsson.cifwk.taf.ui.sdk.ViewModelSwtImpl;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MethodInvokerTest {

    private MethodInvoker methodInvoker;

    @Before
    public void setUp() {
        methodInvoker = new MethodInvoker();
    }

    @Test
    public void toClasses() {
        assertArrayEquals(new Class[] { ArrayList.class }, methodInvoker.toClasses(new String[] { "java.util.ArrayList" }));
        assertArrayEquals(new Class[] { int.class }, methodInvoker.toClasses(new String[] { "int" }));
    }

    @Test
    public void toClassesVarArg() {
        assertArrayEquals(new Class[] { String.class, String[].class }, methodInvoker.toClasses(new String[] { "java.lang.String", "[Ljava.lang.String;" }));
    }

    @Test
    public void toObjects() {
        Object[] objects = methodInvoker.toObjects(new String[] { "1", "2", "3", "4" }, new Class[] { Integer.class, Long.class, String.class, char.class });
        assertArrayEquals(new Object[] { 1, 2L, "3", '4'}, objects);
    }

    @Test
    public void toObjectsVarArg() {
        Object[] objects = methodInvoker.toObjects(new String[] { "[1,2,3]" }, new Class[] { Integer[].class });
        assertEquals(1, objects.length);
        assertArrayEquals(new Integer[]{1, 2, 3} , (Object[]) objects[0]);
    }

    @Test
    public void toObjectsVarArgComposite() {
        Object[] objects = methodInvoker.toObjects(new String[] { "1", "['String, with, commas', 'Another string']" }, new Class[] { Integer.class, String[].class });
        assertEquals(2, objects.length);
        assertEquals(1, objects[0]);
        assertArrayEquals(new String[]{"String, with, commas", "Another string"} , (Object[]) objects[1]);
    }

    @Test
    public void toObjectsVarArgCharSequence() {
        Object[] objects = methodInvoker.toObjects(new String[] { "['String, with, commas', 'Another string']" }, new Class[] { CharSequence[].class });
        assertEquals(1, objects.length);
        assertArrayEquals(new String[]{"String, with, commas", "Another string"} , (Object[]) objects[0]);
    }

    @Test(expected = RuntimeException.class)
    public void toObjects_UnsupportedArgumentType() {
        methodInvoker.toObjects(new String[] { "1" }, new Class[] { BigDecimal.class });
    }

    @Test(expected = NumberFormatException.class)
    public void toObjects_ValueNotMatchingDeclaredType() {
        methodInvoker.toObjects(new String[] { "text" }, new Class[] { Integer.class });
    }

    @Test
    public void getMethod() {
        assertNotNull(methodInvoker.getMethod(new ArrayList<String>(), "iterator", new Class[] {}));
        assertNotNull(methodInvoker.getMethod(Math.class, "abs", new Class[] { int.class }));
        assertNotNull(methodInvoker.getMethod(new ArrayList<String>(), "get", new Class[] { int.class }));
        assertNotNull(methodInvoker.getMethod(new B(), "get", new Class[] {}));
    }

    @Test
    public void testInvokeObjectMethodInvocation() {
        List<String> target = Arrays.asList("item");
        MethodInvocation invocation = new MethodInvocation("get", new String[] { "int" }, new String[] { "0" });
        assertEquals("item", methodInvoker.invoke(target, invocation));
    }

    private static class A {
        public void get() {

        }
    }

    private static class B extends A {

    }
}
