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

import com.ericsson.cifwk.meta.API;

import java.util.Objects;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * Class representing user objects
 *
 * @author erobemu
 */
@API(Stable)
public class User {

    private String username;
    private String password;
    private UserType type;

    public User() {
        username = "";
        password = "";
        type = UserType.OPER;
    }

    public User(String username, String password, UserType type) {
        this.username = username;
        this.password = password;
        this.type = type;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the type
     */
    public UserType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(UserType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (that == null || getClass() != that.getClass()) {
            return false;
        }

        User user = (User) that;

        return Objects.equals(this.username, user.username)
                && Objects.equals(this.password, user.password)
                && Objects.equals(this.type, user.type);
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    /**
     * String representation of a user object
     */
    public String toString() {
        return "User username=" + username + " type=" + type;
    }

}
