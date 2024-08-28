package com.ericsson.cifwk.taf.data.network;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.meta.API.Quality;
import com.ericsson.cifwk.taf.data.User;

import java.util.List;

@API(Quality.Experimental)
public interface HasUsers {

    List<User> getUsers();

}
