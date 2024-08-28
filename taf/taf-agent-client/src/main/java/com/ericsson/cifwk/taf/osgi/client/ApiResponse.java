package com.ericsson.cifwk.taf.osgi.client;

/**
 * Basic response from TAF OSGi agent client.
 *
 * @see com.ericsson.cifwk.taf.osgi.client.ApiClient
 */
public class ApiResponse {

    private final boolean success;
    private final String value;

    ApiResponse(boolean success, String value) {
        this.success = success;
        this.value = value;
    }

    /**
     * Returns the success status of response.
     *
     * @return true, if response is successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the body of response.
     *
     * @return response value as string
     */
    public String getValue() {
        return value;
    }

}
