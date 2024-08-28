package com.ericsson.cifwk.taf.swt.agent;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class MethodInvocation {

    private String method;

    private String[] argumentClasses;

    private String[] arguments;

    public MethodInvocation() {
        // required by GSON
    }

    public MethodInvocation(String method, String[] argumentClasses, String[] arguments) {
        this.method = method;
        this.argumentClasses = argumentClasses;
        this.arguments = arguments;
    }

    public String getMethod() {
        return method;
    }

    public String[] getArgumentClasses() {
        return argumentClasses;
    }

    public String[] getArguments() {
        return arguments;
    }

}
