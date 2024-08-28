package com.ericsson.cifwk.taf

import org.junit.Test

import static com.ericsson.cifwk.taf.StackTraceHelper.getDefaultStackTrace
import static com.ericsson.cifwk.taf.StackTraceHelper.getFilteredMethod
import static org.junit.Assert.assertNotNull

class StackTraceHelperTest extends StackTraceHelperTestSuperClass implements StackTraceHelperTestingInterface  {

    @Test(expected = NoSuchMethodException )
    void verifyException(){
        assertNotNull(getFilteredMethod(defaultStackTrace, [GroovyInterceptable, StackTraceHelperTestingInterface] as Class[], null, null))
    }
    @Test
    void verifyFindByInterface(){
        assertNotNull(getFilteredMethod(defaultStackTrace, [GroovyObject, StackTraceHelperTestingInterface] as Class[], null, null))
    }

    @Test
    void verifyBySuperClass(){
        assertNotNull(getFilteredMethod(defaultStackTrace, null, StackTraceHelperTestSuperClass, null))
        assertNotNull(getFilteredMethod(defaultStackTrace, [GroovyInterceptable] as Class[], StackTraceHelperTestSuperClass, null))
    }
    @Test(expected = NoSuchMethodException )
    void verifyExceptionFromSuperClass(){
        assertNotNull(getFilteredMethod(defaultStackTrace, null, GroovyObject, null))
    }
    @Test
    void verifyByName(){
        assertNotNull(getFilteredMethod(defaultStackTrace, null, null, ["Stack","Helper"] as String[]))
    }
    @Test(expected = NoSuchMethodException )
    void verifyExceptionFromNameSearch(){
        assertNotNull(getFilteredMethod(defaultStackTrace, null, null, ["NoName"] as String[]))
    }

    @Test(expected = NoSuchMethodException )
    void verifyExceptionWhenAllIsNull(){
        assertNotNull(getFilteredMethod(defaultStackTrace, null, null, null))
    }

}
