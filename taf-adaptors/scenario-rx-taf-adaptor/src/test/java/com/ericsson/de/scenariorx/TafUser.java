package com.ericsson.de.scenariorx;

/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.datasource.DataRecord;

public interface TafUser extends DataRecord {
    String ID = "id";
    String USERNAME = "username";
    String PASSWORD = "password";
    String ENABLED = "enabled";
    String ROLES = "roles";

    Integer getId();

    String getUsername();

    String getPassword();

    Boolean getEnabled();

    String getRoles();

}
