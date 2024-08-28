/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario.ext.exporter.jgrapht;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;

@API(Internal)
public abstract class AttributeProvider<T> {
    private String name;

    public AttributeProvider(String name) {
        this.name = name;
    }

    public String getAttributeId() {
        return name.replace(" ", "_").toLowerCase();
    }

    public String getAttributeName() {return name;}

    public abstract String getAttributeValue(T t);
}
