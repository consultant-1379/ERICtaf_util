package com.ericsson.cifwk.taf.findbugs.api;

import edu.umd.cs.findbugs.BugReporter;

import java.util.Set;

import static com.ericsson.cifwk.taf.api.scanner.AnnotatedApiResource.API;

/**
 * Checks use of classes annotated with @API(Internal)
 */
public class UseOfInternalApi extends AbstractUseOfApi {

    private static final String BUG_TYPE = "INTERNAL_API";

    public UseOfInternalApi(BugReporter reporter) {
        super(reporter);
    }

    protected int getPriority() {
        return HIGH_PRIORITY;
    }

    protected String getBugType() {
        return BUG_TYPE;
    }

    @Override
    protected Set<String> getDangerousClasses() {
        return API.getInternalClasses();
    }

    @Override
    protected Set<String> getDangerousMethods() {
        return API.getInternalMethods();
    }

}
