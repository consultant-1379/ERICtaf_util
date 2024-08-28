package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import com.ericsson.cifwk.meta.API;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 05/05/2016
 */
@API(Stable)
public class DataSourceNotFoundException extends Exception {

    public DataSourceNotFoundException(String message) {
        super(message);
    }

}
