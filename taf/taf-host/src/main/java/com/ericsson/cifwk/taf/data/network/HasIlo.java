package com.ericsson.cifwk.taf.data.network;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.meta.API.Quality;

@API(Quality.Experimental)
public interface HasIlo {
    /**
     * Return ilo host
     *
     * @return ILO {@link NetworkInterface}
     */
    NetworkHost getIlo();
}
