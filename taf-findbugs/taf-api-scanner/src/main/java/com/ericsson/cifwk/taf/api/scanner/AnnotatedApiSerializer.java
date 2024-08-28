package com.ericsson.cifwk.taf.api.scanner;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;
import static com.ericsson.cifwk.meta.API.Quality.Experimental;
import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 12.06.2016
 */
public class AnnotatedApiSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotatedApiSerializer.class);

    public void serialize(AnnotatedApi api, File targetFolder) throws IOException {

        // target folder creation on demand
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        // serializing
        write(classes(api.getClasses(Internal)), to(targetFolder, "internal-classes"));
        write(classes(api.getClasses(Deprecated)), to(targetFolder, "deprecated-classes"));
        write(classes(api.getClasses(Experimental)), to(targetFolder, "experimental-classes"));
        write(methods(api.getMethods(Internal)), to(targetFolder, "internal-methods"));
        write(methods(api.getMethods(Deprecated)), to(targetFolder, "deprecated-methods"));
        write(methods(api.getMethods(Experimental)), to(targetFolder, "experimental-methods"));
        write(constructors(api.getConstructors(Internal)), to(targetFolder, "internal-constructors"));
        write(constructors(api.getConstructors(Deprecated)), to(targetFolder, "deprecated-constructors"));
        write(constructors(api.getConstructors(Experimental)), to(targetFolder, "experimental-constructors"));
    }

    private void write(List<String> items, File resource) throws IOException {
        FileWriter fileWriter = new FileWriter(resource);
        for (String item : items) {
            fileWriter.write(item);
            fileWriter.write("\n");
        }
        fileWriter.close();
    }

    private List<String> classes(List<Class<?>> classes) {
        return Lists.transform(classes, new Function<Class<?>, String>(){
            @Override
            public String apply(Class<?> clazz) {
                // e.g. "java.lang.String"
                return clazz.getName();
            }
        });
    }

    private List<String> methods(List<Method> methods) {
        return Lists.transform(methods, new Function<Method, String>(){
            @Override
            public String apply(Method method) {
                // e.g. "public boolean java.lang.Object.equals(java.lang.Object)"
                return method.toString();
            }
        });
    }

    private List<String> constructors(List<Constructor> constructors) {
        return Lists.transform(constructors, new Function<Constructor, String>(){
            @Override
            public String apply(Constructor constructor) {
                // e.g. "public java.util.Hashtable(int,float)"
                return constructor.toString();
            }
        });
    }

    private File to(File targetFolder, String resourceName) throws IOException {
        File internalClassesFile = new File(targetFolder, resourceName);
        if (!internalClassesFile.exists()) {
            internalClassesFile.createNewFile();
        }
        return internalClassesFile;
    }

}
