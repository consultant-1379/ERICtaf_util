/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.data;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import com.ericsson.cifwk.meta.API;
import com.google.gson.annotations.SerializedName;

/**
 * Enum for different user types allowed
 *
 * @author erobemu
 */
@API(Stable)
public enum UserType {
    @SerializedName("admin")
    ADMIN("ADMIN"),
    @SerializedName("oper")
    OPER("OPER"),
    @SerializedName("web")
    WEB("WEB"),

    // Default OSS-RC user categories - 1543-APR 901 0003 Uen H7
    @SerializedName("sys_adm")
    SYS_ADM("SYS_ADM"),
    @SerializedName("appl_adm")
    APPL_ADM("APPL_ADM"),
    @SerializedName("nw_ope")
    NW_OPE("NW_OPE"),
    @SerializedName("ope")
    OPE("OPE"),
    @SerializedName("ass_ope")
    ASS_OPE("ASS_OPE"),
    @SerializedName("custom")
    CUSTOM("CUSTOM");

    private String type;

    UserType(String type) {
        this.type = type;
    }

    public String toString() {
        return type;
    }

    public static UserType getByName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CUSTOM;
        }
    }
}
