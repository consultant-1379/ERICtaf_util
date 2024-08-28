package com.ericsson.cifwk.taf.api.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 12.06.2016
 */
public class AnnotatedApiResource {

    public static final AnnotatedApiResource INSTANCE = new AnnotatedApiResource();

    public static final AnnotatedApiResource API = INSTANCE;

    private final Set<String> deprecatedClasses;

    private final Set<String> internalClasses;

    private final Set<String> experimentalClasses;

    private final Set<String> deprecatedMethods;

    private final Set<String> internalMethods;

    private final Set<String> experimentalMethods;

    private AnnotatedApiResource() {
        this.deprecatedClasses = readClasses("deprecated-classes");
        this.internalClasses = readClasses("internal-classes");
        this.experimentalClasses = readClasses("experimental-classes");
        this.deprecatedMethods = readMethods("deprecated-methods");
        this.internalMethods = readMethods("internal-methods");
        this.experimentalMethods = readMethods("experimental-methods");
    }

    private static Set<String> readClasses(String resourceName) {
        Set<String> result = new HashSet<>();
        for (String line : readLines(resourceName)) {
            result.add(line);
        }
        return result;
    }

    private static Set<String> readMethods(String resourceName) {
        Set<String> result = new HashSet<>();
        for (String line : readLines(resourceName)) {
            result.add(processMethod(line));
        }
        return result;
    }

    static String processMethod(String line) {
        String [] array =  line.split(" ");
        //replace "java.lang." is workaround currently if method contains String, its processed as
        //String, if contains String[] its processed as java.lang.String[] need to remove the java.lang.
        //as SignatureConverter.convertMethodSignature() returns just String[]
        return array[array.length-1].replace("java.lang.","");
    }

    private static Set<String> readLines(String resourceName) {
        InputStream resource = AnnotatedApiResource.class.getClassLoader().getResourceAsStream(resourceName);
        if (resource == null) {
            System.out.println(String.format("Couldn't find resource %s in classpath", resourceName));
        } else {
            HashSet<String> result = new HashSet<>();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(resource));
                String line;
                while ((line = br.readLine()) != null) {
                    result.add(line);
                }
                return result;
            } catch (IOException e) {
                System.out.println(String.format("Couldn't read from resource %s: %s", resourceName, e));
            } finally {
                try {
                    resource.close();
                } catch (IOException ignored) {
                    // OK
                }
            }
        }
        return new HashSet<>();
    }

    public Set<String> getDeprecatedClasses() {
        return deprecatedClasses;
    }

    public Set<String> getInternalClasses() {
        return internalClasses;
    }

    public Set<String> getDeprecatedMethods() {
        return deprecatedMethods;
    }

    public Set<String> getInternalMethods() {
        return internalMethods;
    }

    public Set<String> getExperimentalClasses() {
        return experimentalClasses;
    }

    public Set<String> getExperimentalMethods() {
        return experimentalMethods;
    }

}
