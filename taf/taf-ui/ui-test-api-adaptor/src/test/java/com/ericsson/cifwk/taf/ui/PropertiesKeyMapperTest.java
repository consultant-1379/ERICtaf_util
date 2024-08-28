package com.ericsson.cifwk.taf.ui;

import com.ericsson.cifwk.taf.ui.spi.TafPropertiesKeyMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

import static com.ericsson.cifwk.taf.ui.DefaultSettings.RETRY_SCHEMA;
import static java.lang.System.setProperty;
import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesKeyMapperTest {

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void retrySchemaPropertyMapped() {
        assertThat(DefaultSettings.getImplicitWaitRetrySchema()).isEqualTo(RETRY_SCHEMA);

        // search in alternative properties
        setProperty(TafPropertiesKeyMapper.TAF_UI_RETRY_SCHEMA_PROPERTY, "1,2,3");
        assertThat(DefaultSettings.getImplicitWaitRetrySchema()).isEqualTo(new Long[]{1L, 2L, 3L});

        // canonical property has priority
        setProperty(DefaultSettings.UI_RETRY_SCHEMA_PROPERTY, "3,2,1");
        assertThat(DefaultSettings.getImplicitWaitRetrySchema()).isEqualTo(new Long[]{3L, 2L, 1L});
    }

}
