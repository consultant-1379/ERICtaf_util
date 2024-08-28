package com.ericsson.cifwk.taf.osgi.client;

public final class JavaApi {

    private JavaApi() {
    }

    /**
     * Creates a Java API wrapper for an agent at the given HTTP endpoint.
     *
     * @param endpoint agent endpoint, i.e. agent servlet root path
     * @return TAF OSGi agent client
     */
    public static ApiClient createApiClient(String endpoint) {
        return new ApiClient(endpoint);
    }

}
