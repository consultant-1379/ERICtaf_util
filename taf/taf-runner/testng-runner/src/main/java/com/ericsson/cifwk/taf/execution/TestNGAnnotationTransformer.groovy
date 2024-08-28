/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.execution

import com.ericsson.cifwk.taf.annotations.VUsers
import com.ericsson.cifwk.taf.management.TafContext
import groovy.util.logging.Log4j
import org.testng.IAnnotationTransformer
import org.testng.annotations.ITestAnnotation

import java.lang.reflect.Constructor
import java.lang.reflect.Method
/**
 * This class is used to transform annotations on TestNG TCs. In this specific implementation it is used to set Invocation count for
 * a TC Method based on VUser and Context Annotations.
 * @author qshatus
 * 
 */
@Log4j
class TestNGAnnotationTransformer implements IAnnotationTransformer {

    private static final int DEFAULT_INVOCATION_COUNT = 1

    public static final int DEFAULT_VUSER = 1

	/**
	 * This method will be invoked by TestNG to give you a chance to modify a TestNG annotation read from your test classes. 
	 * You can change the values you need by calling any of the setters on the ITest interface. 
	 * @param annotation The annotation that was read from your test class.
	 * @param testClass If the annotation was found on a class, this parameter represents this class (null otherwise).
	 * @param testConstructor If the annotation was found on a constructor, this parameter represents this constructor (null otherwise).
	 * @param testMethod If the annotation was found on a method, this parameter represents this method (null otherwise).	
	 */
	@Override
	void transform(ITestAnnotation annotation,
                          Class testClass,
			              Constructor testConstructor,
                          Method testMethod){
        if (testMethod == null) {
            // Looking at test annotation at class level
            return
        }

        int[] vusers = [DEFAULT_VUSER];
        testMethod.getAnnotations().each {
            if (it instanceof VUsers) {
                vusers = it.vusers()
            }
        }

        // Preparing list of all possible combinations for test method
        // Could be easily extended with additional dimensions
        List matrix = [vusers].combinations()
        TafContext.methodAttributes.put(testMethod, matrix)

        def invocations = getInvocationNumber(vusers.length)
        annotation.setInvocationCount(invocations)

		log.debug "Method $testMethod will be called for $vusers.length vusers"
	}

	/**
	 * Get the Number of Invocations for the Test Case based on size of Vusers
	 * @param vuserArraySize - Size of Vuser Array
	 * @return int - Value of Invocations Required for the TC
	 */
	int getInvocationNumber(int vuserArraySize){
		if(vuserArraySize != 0 ){
			return vuserArraySize
        } else {
			return DEFAULT_INVOCATION_COUNT
        }
	}

}





