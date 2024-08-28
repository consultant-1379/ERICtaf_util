package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.configuration.TafProperty;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 01.06.2017
 */
public class TafConfigurationTypeListenerTest {

    private TafConfigurationTypeListener listener;

    @TafProperty("")
    private String string;
    @TafProperty("")
    private long longValue;
    @TafProperty("")
    private Integer integerValue;
    @TafProperty("")
    private Object object;
    @TafProperty("")
    private Date date;

    private String stringWithoutAnnotation;
    private int intWithoutAnnotation;

    private Field stringField;
    private Field longValueField;
    private Field integerValueField;
    private Field objectField;
    private Field dateField;
    private Field stringWithoutAnnotationField;
    private Field intWithoutAnnotationField;

    @Before
    public void setUp() throws NoSuchFieldException {
        listener = new TafConfigurationTypeListener();

        stringField = TafConfigurationTypeListenerTest.class.getDeclaredField("string");
        longValueField = TafConfigurationTypeListenerTest.class.getDeclaredField("longValue");
        integerValueField = TafConfigurationTypeListenerTest.class.getDeclaredField("integerValue");
        objectField = TafConfigurationTypeListenerTest.class.getDeclaredField("object");
        dateField = TafConfigurationTypeListenerTest.class.getDeclaredField("date");
        stringWithoutAnnotationField = TafConfigurationTypeListenerTest.class.getDeclaredField("stringWithoutAnnotation");
        intWithoutAnnotationField = TafConfigurationTypeListenerTest.class.getDeclaredField("intWithoutAnnotation");
    }

    @Test
    public void memberShouldBeRegistered() {

        // good candidates
        assertThat(listener.memberShouldBeRegistered(stringField)).isTrue();
        assertThat(listener.memberShouldBeRegistered(longValueField)).isTrue();
        assertThat(listener.memberShouldBeRegistered(integerValueField)).isTrue();
        assertThat(listener.memberShouldBeRegistered(objectField)).isTrue();
        assertThat(listener.memberShouldBeRegistered(dateField)).isTrue();

        // bad candidates
        assertThat(listener.memberShouldBeRegistered(stringWithoutAnnotationField)).isFalse();
        assertThat(listener.memberShouldBeRegistered(intWithoutAnnotationField)).isFalse();
    }

}