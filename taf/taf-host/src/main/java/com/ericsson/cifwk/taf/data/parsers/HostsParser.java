package com.ericsson.cifwk.taf.data.parsers;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.data.parsers.adaptors.HostTypeAdapter;
import com.ericsson.cifwk.taf.data.parsers.adaptors.PortTypeAdapter;
import com.ericsson.cifwk.taf.data.parsers.adaptors.UserTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class HostsParser {

    public static List<Host> parse(InputStream inputStream) throws UnsupportedEncodingException {
        return parse(new InputStreamReader(inputStream, "UTF-8"));
    }

    public static List<Host> parse(Reader reader) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(HostType.class, new HostTypeAdapter());
        gsonBuilder.registerTypeAdapter(Ports.class, new PortTypeAdapter());
        gsonBuilder.registerTypeAdapter(UserType.class, new UserTypeAdapter());

        Gson gson = gsonBuilder.create();
        Host[] hosts = gson.fromJson(reader, Host[].class);
        return Arrays.asList(hosts);
    }

}
