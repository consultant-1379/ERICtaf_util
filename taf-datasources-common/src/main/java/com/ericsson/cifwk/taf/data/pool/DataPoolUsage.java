package com.ericsson.cifwk.taf.data.pool;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;

/**
 * Way to map data source records to vuser executions.
 */
@API(Internal)
public enum DataPoolUsage {

    /**
     * Data source is shared between vusers. Records are given out on demand to every vuser.
     */
    SHARED,

    /**
     * Every vuser gets own copy of data source. Every user has cursor in exclusive data source.
     */
    COPIED

}
