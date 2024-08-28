package com.ericsson.cifwk.taf.data.parser;

import com.ericsson.cifwk.meta.API;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import groovy.util.ConfigObject;

import java.util.Arrays;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class JsonParser {

    public static List<ConfigObject> parse(String jsonString) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        Gson gson = gsonBuilder.create();
        ConfigObject[] hosts = gson.fromJson(jsonString, ConfigObject[].class);
        return Arrays.asList(hosts);
    }
}
