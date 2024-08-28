package com.ericsson.cifwk.taf.api.scanner;

import com.ericsson.cifwk.meta.API;
import com.google.common.collect.Maps;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 12.06.2016
 */
class AnnotatedApi {

    private final Map<API.Quality, List<Class<?>>> classes = Maps.newHashMap();

    private final Map<API.Quality, List<Method>> methods = Maps.newHashMap();

    private final Map<API.Quality, List<Constructor>> constructors = Maps.newHashMap();

    public AnnotatedApi setClasses(List<Class<?>> classes, API.Quality type) {
        this.classes.put(type, classes);
        return this;
    }

    public AnnotatedApi setMethods(List<Method> methods, API.Quality type) {
        this.methods.put(type, methods);
        return this;
    }

    public AnnotatedApi setConstructors(List<Constructor> constructors, API.Quality type) {
        this.constructors.put(type, constructors);
        return this;
    }

    public List<Class<?>> getClasses(API.Quality type) {
        return this.classes.get(type);
    }

    public List<Method> getMethods(API.Quality type) {
        return this.methods.get(type);
    }

    public List<Constructor> getConstructors(API.Quality type) {
        return this.constructors.get(type);
    }

}
