package com.ericsson.cifwk.taf.configuration;

import com.google.common.collect.Sets;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;

import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 08/06/2017
 */
public class TafConfigurationUtilsTest {

    @Test
    public void shouldGetPropertiesFromApacheConfig() throws Exception {
        Configuration configuration = mock(Configuration.class);
        Set<String> keys = Sets.newHashSet("a", "b", "c");
        when(configuration.getKeys()).thenReturn(keys.iterator());
        when(configuration.getProperty("a")).thenReturn("a_value");
        when(configuration.getProperty("b")).thenReturn("b_value");
        when(configuration.getProperty("c")).thenReturn(null);

        Properties properties = TafConfigurationUtils.getProperties(configuration);
        assertThat(properties).hasSize(2);
        assertThat(properties.getProperty("a")).isEqualTo("a_value");
        assertThat(properties.getProperty("b")).isEqualTo("b_value");
    }

}