package com.ericsson.cifwk.taf.data.network;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.meta.API.Quality;

@API(Quality.Experimental)
public interface HasIp {

    /**
     * Get host address (ip v4)
     */
    String getIpv4();

    /**
     * Get host address (ip v6)
     */
    String getIpv6();

}
