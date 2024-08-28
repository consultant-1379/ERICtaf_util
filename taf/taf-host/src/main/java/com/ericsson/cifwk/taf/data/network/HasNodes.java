package com.ericsson.cifwk.taf.data.network;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.meta.API.Quality;

import java.util.List;

@API(Quality.Experimental)
public interface HasNodes {

    List<NetworkHost> getNodes();

}
