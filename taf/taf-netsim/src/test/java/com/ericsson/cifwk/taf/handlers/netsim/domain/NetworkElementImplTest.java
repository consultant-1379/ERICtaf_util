package com.ericsson.cifwk.taf.handlers.netsim.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.ericsson.cifwk.taf.handlers.netsim.commands.CreatemoCommand;

public class NetworkElementImplTest {

    @Test
    public void hasNotTimedOut(){
        assertThat(NetworkElementImpl.hasNotTimedOut(System.currentTimeMillis()-2000,5000)).isTrue();
    }

    @Test
    public void hasTimedOut(){
        assertThat(NetworkElementImpl.hasNotTimedOut(System.currentTimeMillis()-5000,2000)).isFalse();
    }

    @Test
    public void shouldBuildCreateMoCommandWithAttributes(){
        NetworkElementImpl ne = new NetworkElementImpl(null, null);
        CreatemoCommand createmoCommand = ne.buildCreatemoCommand("fdn", "type", "name", "myAttributes", 1);
        assertThat(createmoCommand.getAttributes()).matches("myAttributes");
    }
}
