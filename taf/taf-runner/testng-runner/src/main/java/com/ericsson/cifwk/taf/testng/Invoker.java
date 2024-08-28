package com.ericsson.cifwk.taf.testng;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Invoker<T> {

	private final Class<T> klass;
	private final Object self;

	public Invoker(Object self, Class<T> klass) {
		this.self = self;
		this.klass = klass;
	}

	@SuppressWarnings("unchecked")
	public <R> R invoke(String methodName, Object... parameters) {
		Class[] classes = getClasses(parameters);
		try {
			Method method = klass.getDeclaredMethod(methodName, classes);
			method.setAccessible(true);
			return (R) method.invoke(self, parameters);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e); // NOSONAR
		}
    }

	private static Class[] getClasses(Object[] objects) {
		Class[] classes = new Class[objects.length];
		for (int i = 0; i < objects.length; i++) {
			classes[i] = objects[i].getClass();
		}
		return classes;
	}

}
