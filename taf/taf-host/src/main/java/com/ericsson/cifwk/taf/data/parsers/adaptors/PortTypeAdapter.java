package com.ericsson.cifwk.taf.data.parsers.adaptors;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Ports;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class PortTypeAdapter implements JsonDeserializer<Ports> {

    private static Logger logger = LoggerFactory.getLogger(PortTypeAdapter.class);

    @Override
    public Ports deserialize(JsonElement json, Type type, JsonDeserializationContext jsonContext)
        throws JsonParseException {
        String element = json.getAsJsonPrimitive().getAsString();
        if (element != null && !element.isEmpty()) {
            try {
                return Ports.valueOf(element.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("PortType with name '" + element + "' doesn't exist.");
            }
        }
        return Ports.UNKNOWN;
    }
}
