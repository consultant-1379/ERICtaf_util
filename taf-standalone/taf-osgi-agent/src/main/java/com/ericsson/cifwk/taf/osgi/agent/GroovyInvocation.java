package com.ericsson.cifwk.taf.osgi.agent;

import com.ericsson.cifwk.meta.API;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyRuntimeException;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Reflected Groovy class wrapper with instantiation and invocations
 * configurable from HTTP request objects.
 */
@API(Internal)
class GroovyInvocation {

    static final String SOURCE_PARAM = "source";
    static final String METHOD_PARAM = "method";
    static final String ARG_COUNT_PARAM = "argCount";
    static final String ARG_PARAM = "arg";

    private final Class<?> klass;
    private final Object target;

    public GroovyInvocation(Class<?> klass, Object target) {
        this.klass = klass;
        this.target = target;
    }

    /**
     * Creates an invocation wrapper from HTTP request parameters.
     * 
     * @param request
     *            source request object
     * @return invocation instance
     * @throws GroovyInvocationException
     */
    public static GroovyInvocation fromRequest(HttpServletRequest request) throws GroovyInvocationException {
        String source = request.getParameter(SOURCE_PARAM);
        if (source == null) {
            throw new GroovyInvocationException("class source missing");
        }
        return fromSource(source);
    }

    /**
     * Creates an invocation wrapper from a Groovy class source.
     * 
     * @param source
     *            Groovy class source
     * @return invocation instance
     * @throws GroovyInvocationException
     */
    public static GroovyInvocation fromSource(String source) throws GroovyInvocationException {
        GroovyClassLoader classLoader = new GroovyClassLoader(GroovyInvocation.class.getClassLoader());
        Class<?> klass;
        try {
            klass = classLoader.parseClass(new GroovyCodeSource(source, "", ""));
        } catch (GroovyRuntimeException e) {
            throw new GroovyInvocationException(e);
        }

        Object target;
        try {
            target = klass.newInstance();
        } catch (Exception e) {
            throw new GroovyInvocationException("could not create instance", e);
        }

        return new GroovyInvocation(klass, target);
    }

    /**
     * Invokes a method on the instance of the underlying class, getting the
     * method name and arguments from HTTP request parameters.
     * 
     * @param request
     *            source request object
     * @return invocation result
     * @throws GroovyInvocationException
     */
    public Object invoke(HttpServletRequest request) throws GroovyInvocationException {
        String methodName = request.getParameter(METHOD_PARAM);
        if (methodName == null) {
            throw new GroovyInvocationException("method name missing");
        }

        int argCount;
        try {
            argCount = Integer.parseInt(request.getParameter(ARG_COUNT_PARAM));
        } catch (NumberFormatException e) {
            throw new GroovyInvocationException("argument count missing or invalid", e);
        }

        String[] args = new String[argCount];
        for (int i = 0; i < argCount; i++) {
            args[i] = request.getParameter(ARG_PARAM + i);
        }

        return invoke(methodName, args);
    }

    /**
     * Invokes a method on the instance of the underlying class with the
     * provided arguments.
     * 
     * @param methodName
     *            target method name
     * @param args
     *            target method arguments
     * @return invocation result
     * @throws GroovyInvocationException
     */
    public Object invoke(String methodName, String... args) throws GroovyInvocationException {
        Class<?> klass = target.getClass();

        if (methodName == null) {
            throw new GroovyInvocationException("method name cannot be null");
        }
        Method method;
        Class[] argTypes = new Class[args.length];
        for (int i = 0; i < argTypes.length; i++) {
            argTypes[i] = String.class;
        }
        try {
            method = klass.getMethod(methodName, argTypes);
        } catch (NoSuchMethodException e) {
            throw new GroovyInvocationException("method doesn't exist", e);
        }

        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new GroovyInvocationException("couldn't invoke method", e);
        }
    }

    /**
     * Gets the underlying reflected class.
     * 
     * @return reflected class for this invocation
     */
    public Class<?> invocationClass() {
        return klass;
    }
}
