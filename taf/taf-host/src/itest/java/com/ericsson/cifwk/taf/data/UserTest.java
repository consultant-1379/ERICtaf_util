package com.ericsson.cifwk.taf.data;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 19.10.2016
 */
public class UserTest {

    private static final String PASSWORD = "password1";

    private User user;

    @Before
    public void setUp() {
        user = new User("user1", PASSWORD, UserType.OPER);
    }

    @Test
    public void passwordIsProtected() {
        String userAsString = user.toString();
        Assertions.assertThat(userAsString).doesNotContain(PASSWORD);
    }

}