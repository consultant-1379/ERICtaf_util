package com.ericsson.cifwk.taf.findbugs.api;

import edu.umd.cs.findbugs.BugReporter;

import java.util.Set;

import static com.ericsson.cifwk.taf.api.scanner.AnnotatedApiResource.API;

/**
 * Checks use of classes annotated with @API(Experimental)
 */
public class UseOfExperimentalApi extends AbstractUseOfApi {

    private static final String BUG_TYPE = "EXPERIMENTAL_API";

    public UseOfExperimentalApi(BugReporter reporter) {
        super(reporter);
    }

    protected int getPriority() {
        return LOW_PRIORITY;
    }

    protected String getBugType() {
        return BUG_TYPE;
    }

    @Override
    protected Set<String> getDangerousClasses() {
        return API.getExperimentalClasses();
    }

    @Override
    protected Set<String> getDangerousMethods() {
        return API.getExperimentalMethods();
    }

}
