package com.ericsson.cifwk.taf.tools.cli.handlers.impl;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
/**
 * Created by ejohlyn on 2/18/16.
 */
public class RemoteObjectHandlerTest {

    @Test
    public void testCheckPathEnding() throws Exception {
        assertThat(RemoteObjectHandler.ensurePathEnding("somePath")).endsWith("/");
        assertThat(RemoteObjectHandler.ensurePathEnding("somePath/")).endsWith("/");
    }

    @Test
    public void getDefaultUser() {

        // provided user is taken
        User defaultUser = new User("temp", "temp", UserType.OPER);
        User actualUser = RemoteObjectHandler.getDefaultUser(null, defaultUser);
        Assertions.assertThat(actualUser).isSameAs(defaultUser);

        // empty user is taken
        actualUser = RemoteObjectHandler.getDefaultUser(new Host(), null);
        Assertions.assertThat(actualUser).isEqualTo(new User());

        // first user from host is taken
        Host host = Host.builder().withUser(defaultUser).build();
        actualUser = RemoteObjectHandler.getDefaultUser(host, null);
        Assertions.assertThat(actualUser).isSameAs(defaultUser);
    }
}