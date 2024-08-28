package com.ericsson.cifwk.taf.findbugs.api;

import edu.umd.cs.findbugs.BugReporter;

import java.util.Set;

import static com.ericsson.cifwk.taf.api.scanner.AnnotatedApiResource.API;

/**
 * Checks use of classes annotated with @API(Deprecated)
 */
public class UseOfDeprecatedApi extends AbstractUseOfApi {

    private static final String BUG_TYPE = "DEPRECATED_API";

    public UseOfDeprecatedApi(BugReporter reporter) {
        super(reporter);
    }

    protected int getPriority() {
        return NORMAL_PRIORITY;
    }

    protected String getBugType() {
        return BUG_TYPE;
    }

    @Override
    protected Set<String> getDangerousClasses() {
        return API.getDeprecatedClasses();
    }

    @Override
    protected Set<String> getDangerousMethods() {
        return API.getDeprecatedMethods();
    }

}
