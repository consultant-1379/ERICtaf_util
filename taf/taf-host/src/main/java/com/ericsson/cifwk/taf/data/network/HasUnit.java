package com.ericsson.cifwk.taf.data.network;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.meta.API.Quality;

@API(Quality.Experimental)
public interface HasUnit {

    String getUnit();
}
