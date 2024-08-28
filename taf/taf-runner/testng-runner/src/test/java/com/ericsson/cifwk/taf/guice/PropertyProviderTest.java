package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.configuration.TafProperty;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static com.ericsson.cifwk.taf.guice.PropertyProvider.isAnnotationValueSet;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.getTafPropertyAnnotation;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 07.06.2017
 */
public class PropertyProviderTest {

    @TafProperty("")
    private String notSet;

    @TafProperty(value = "value", defaultValue = "")
    private String emptyValue;

    @TafProperty(value = "value", defaultValue = "value")
    private String value;

    private Field notSetField;
    private Field emptyValueField;
    private Field valueField;

    @Before
    public void setUp() throws NoSuchFieldException {
        notSetField = PropertyProviderTest.class.getDeclaredField("notSet");
        emptyValueField = PropertyProviderTest.class.getDeclaredField("emptyValue");
        valueField = PropertyProviderTest.class.getDeclaredField("value");
    }

    @Test
    public void checkIsValueSet() {
        assertDefaultValue(notSetField, false);
        assertDefaultValue(emptyValueField, true);
        assertDefaultValue(valueField, true);
    }

    private void assertDefaultValue(Field field, boolean isSet) {
        assertThat(isAnnotationValueSet(getTafPropertyAnnotation(field).defaultValue())).isSameAs(isSet);
    }

}