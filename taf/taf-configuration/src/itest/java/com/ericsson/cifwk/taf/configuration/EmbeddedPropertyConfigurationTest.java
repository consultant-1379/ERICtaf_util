package com.ericsson.cifwk.taf.configuration;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by ekonsla on 01/08/2016.
 */
public class EmbeddedPropertyConfigurationTest {

    @Test
    public void testInterpolated() throws Exception {
        TafConfiguration configuration = TafConfigurationProvider.provide();

        assertThat(configuration.getString("one.property"), is("1"));
        assertThat(configuration.getString("fifteen.property"), is("15"));
        assertThat(configuration.getString("twoonefive.property"), is("215"));

        assertThat(configuration.getInt("one.property"), is(1));
        assertThat(configuration.getInt("fifteen.property"), is(15));
        assertThat(configuration.getInt("twoonefive.property"), is(215));

        assertThat(configuration.getDouble("one.property"), is(1d));
        assertThat(configuration.getDouble("oneandahalf.property"), is(1.5d));
        assertThat(configuration.getDouble("fifteen.property"), is(15d));
        assertThat(configuration.getDouble("twoonefive.property"), is(215d));

        assertThat(configuration.getBoolean("seven.property"), is(true));
        assertThat(configuration.getBoolean("eight.property"), is(false));

        assertThat(configuration.getString("folderone.foldertwo.property"), is("folderone/foldertwo"));
    }
}
