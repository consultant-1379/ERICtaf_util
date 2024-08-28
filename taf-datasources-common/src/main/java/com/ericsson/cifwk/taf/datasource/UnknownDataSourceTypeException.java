package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import com.ericsson.cifwk.meta.API;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 05/05/2016
 */
@API(Stable)
public class UnknownDataSourceTypeException extends Exception {

    private final String type;

    public UnknownDataSourceTypeException(String type) {
        this.type = type;
    }

    @Override
    public String getMessage() {
        return String.format("Unknown data source type: '%s'", type);
    }
}
