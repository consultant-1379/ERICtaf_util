package com.ericsson.cifwk.taf

import groovy.transform.PackageScope
import org.codehaus.groovy.runtime.StackTraceUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@PackageScope
class StackTraceHelper {
	
	private static Logger logger = LoggerFactory.getLogger(StackTraceHelper)
	
	/**
	 * Get the stack trace for the current Thread
	 * @return An array of stack trace elements
	 */
	static StackTraceElement[] getDefaultStackTrace(){
		return Thread.currentThread().getStackTrace()
	}

	/**
	 * Get a stack trace element 
	 * @param stackTrace - array of stack trace elements
	 * @param interfaces - array of interfaces
	 * @param superClass -  array of super classes
	 * @param nameParts - array of named parts
	 * @return a StackTraceElement object 
	 * @throws NoSuchMethodException
	 */
	static StackTraceElement getFilteredMethod(StackTraceElement[] stackTrace,Class[] interfaces, Class superClass, String[] nameParts) throws NoSuchMethodException{
		ClassLoader cl = StackTraceHelper.getClassLoader()
		logger.trace "Filtering stack trace to match interfaces: $interfaces or parent: $superClass or name parts: $nameParts"
		Closure isMatch = { StackTraceElement element->
			String className = element.getClassName()
			logger.trace "Looking at $className"
			try {
				Class currentClass = cl.loadClass(className)
				boolean isClass = false
				if (interfaces){
					isClass = currentClass.getInterfaces().toList().containsAll(interfaces)
				}
				if (superClass){
					isClass = isClass || currentClass.getSuperclass() == superClass
				}
				if (nameParts){
					isClass = isClass || nameParts.every {	currentClass.getCanonicalName().contains(it)	}
				}
				logger.trace "Element $element ${((isClass)?'':'not')} matches"
				return isClass
			} catch (Throwable e ) {
				logger.trace "Cannot load class: $e"
				return false
			}
		}
		StackTraceElement result = stackTrace.find(isMatch)
		if (! result) throw new NoSuchMethodException("No method in stack trace")
		else return result
	}

	/**
	 * Get the name of the current calling method </br>
	 * here is an example of how to call this method </br> <p>
	 *  <code> String methodName = StackTraceHelper.getCurrentMethodName(new Throwable() ) </code>
	 * @param t new Throwable created in the calling method 
	 * @return String representation of the calling method's name
	 * @throws NoSuchMethodException
	 */
	static String getCurrentMethodName(Throwable t) throws NoSuchMethodException {
		String result
		try {
			result = StackTraceUtils.sanitize(t).stackTrace.first().methodName
		}catch (Throwable othert){
		throw new NoSuchMethodException("No method in stack trace")
		}
		return result 
	}
}
