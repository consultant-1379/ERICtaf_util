package com.ericsson.cifwk.taf.configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TafConfigurationTest {

    String savedUserHome;

    @Before
    public void setUp() throws Exception {
        savedUserHome = System.getProperty("user.home");
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty("user.home", savedUserHome);
    }

    @Test
    public void testConfiguration() throws Exception {
        System.setProperty("user.home", "src/itest/resources/user-home");
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();
        build.reload();
        //
        assertThat(build.getString("user-home.properties"), is("exist"));
        assertThat(build.getString("first.properties"), is("exist"));
        assertThat(build.getString("second.properties"), is("exist"));
        assertThat(build.getString("host.first.ip"), is("exist"));
        assertThat(build.getString("host.second.ip"), is("exist"));
        //
        assertThat(build.getString("folder.1.properties"), is("exist"));
        assertThat(build.getString("host.folder.1.host.ip"), is("exist"));
        assertThat(build.getString("folder.2.properties"), is("exist"));
        assertThat(build.getString("host.folder.2.host.ip"), is("exist"));
    }

    @Test
    public void shouldProcessCorrectly_WrongHomePath() throws Exception {
        System.setProperty("user.home", "./" + System.currentTimeMillis());
        new TafConfigurationProvider().getConfiguration();
    }

    @Test
    public void shouldProcessCorrectly_incorrectUserHomePath() throws Exception {
        System.setProperty("user.home", "::::");
        new TafConfigurationProvider().getConfiguration();

        System.setProperty("user.home", "http://localhost/a");
        new TafConfigurationProvider().getConfiguration();
    }

    private void save(Configuration configuration) throws ConfigurationException {
        PropertiesConfiguration p = new PropertiesConfiguration();
        p.append(configuration);
        p.save("./target/save.properties");
    }

    @Test
    public void testProfile_A_B_C_IS_CONFIGURED() throws Exception {
        System.setProperty("taf.profiles", "a,b,c");
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();
        String[] stringArray = build.getStringArray("taf.profiles");
        assertThat(stringArray, arrayContaining("a", "b", "c"));
    }

    @Test
    public void testProfile_A_B_C() throws Exception {
        System.setProperty("taf.profiles", "a,b,c");
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();

        assertThat(build.getString("profile.a.properties"), is("exist"));
        assertThat(build.getString("host.profile.a.ip"), is("exist"));
        //
        assertThat(build.getString("profile.b.properties"), is("exist"));
        assertThat(build.getString("host.profile.b.ip"), is("exist"));
        //
        assertThat(build.getString("profile.c.properties"), is("exist"));
        assertThat(build.getString("host.profile.c.ip"), is("exist"));
    }

    @Test
    public void testProfile_only_B() throws Exception {
        System.setProperty("taf.profiles", "b");
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();

        assertThat(build.getString("profile.a.properties"), not(is("exist")));
        assertThat(build.getString("host.profile.a.ip"), not(is("exist")));
        //
        assertThat(build.getString("profile.b.properties"), is("exist"));
        assertThat(build.getString("host.profile.b.ip"), is("exist"));
        //
        assertThat(build.getString("profile.c.properties"), not(is("exist")));
        assertThat(build.getString("host.profile.c.ip"), not(is("exist")));
    }

    @Test
    public void testProfile_for_non_existing_profile() throws Exception {
        System.setProperty("taf.profiles", "for_non_existing_profile");
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();
        System.out.println();
    }

    @Test
    public void testProfile_for_taf_properties_location() throws Exception {
        System.setProperty("taf.properties.location", "host-properties-json");
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();
        //
        assertThat(build.getString("host.host-1.offset"), is("0"));
        assertThat(build.getString("host.host-1.tunnel"), is("1"));
        assertThat(build.getString("host.host-1.node.node-1.offset"), is("2"));
        assertThat(build.getString("host.host-1.node.node-1.tunnel"), is("3"));

        assertThat(build.getString("host.host-3.offset"), is("4"));
        assertThat(build.getString("host.host-3.tunnel"), is(isNull()));
        assertThat(build.getString("host.host-4.offset"), is(isNull()));
        assertThat(build.getString("host.host-4.tunnel"), is("5"));
        System.clearProperty("taf.properties.location");
    }

    @Test
    public void should_convert_system_property() {
        System.setProperty("shouldBeLong", "100");
        TafConfiguration configuration = new TafConfigurationProvider().getConfiguration();
        assertThat(configuration.getProperty("shouldBeLong", Long.class), is(100L));
    }

    @Test
    public void should_convert_to_list() {
        System.setProperty("list", "this, is, a, list");
        TafConfiguration configuration = new TafConfigurationProvider().getConfiguration();
        assertThat(configuration.getProperty("list", List.class).toString(), is("[this, is, a, list]"));
    }

    @Test
    public void should_ignore_commas() {
        System.setProperty("list", "this\\, is\\, a\\, list");
        TafConfiguration configuration = new TafConfigurationProvider().getConfiguration();
        assertThat(configuration.getProperty("list").toString(), is("this, is, a, list"));
    }

    @Test(timeout = 10_000L)
    public void should_be_thread_safe() throws InterruptedException {
        System.setProperty("user.home", "src/itest/resources/user-home");
        final TafConfiguration configuration = new TafConfigurationProvider().getConfiguration();
        final AtomicInteger executionCount = new AtomicInteger(0);
        Runnable setThread = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 750; i++) {
                    configuration.setProperty("aprop", "10");
                }
                executionCount.incrementAndGet();
            }
        };
        Runnable readThread = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 750; i++) {
                    configuration.getProperties();
                }
                executionCount.incrementAndGet();
            }
        };
        Runnable reloadThread = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    configuration.reload();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                executionCount.incrementAndGet();
            }
        };

        new Thread(setThread).start();
        new Thread(reloadThread).start();
        new Thread(readThread).start();
        while (executionCount.get() < 3) {
            Thread.sleep(100);
        }
    }

    @Test
    public void testSpecialCharactersInPropertyValues(){
        System.setProperty("hosts", "pmserv_1,fmx_1");
        final TafConfiguration configuration = new TafConfigurationProvider().getConfiguration();
        assertThat(configuration.getProperty("hosts", new String[]{}, String[].class).length,is(2));
    }

}
