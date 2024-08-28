package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.data.Host;
import com.google.inject.Provider;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import static com.ericsson.cifwk.taf.guice.ParameterizedFieldUtils.checkCollectionGenericType;
import static com.ericsson.cifwk.taf.guice.ParameterizedFieldUtils.getProviderParameter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class TafHostValidationUtilsTest {

    private List<Host> hosts;
    private List<Object> objects;
    private List<Date> dates;
    private List<?> something;
    private List list;
    private Provider<String> stringProvider;
    private Provider<?> someProvider;
    private Provider provider;

    private Field hostsField;
    private Field objectsField;
    private Field datesField;
    private Field somethingField;
    private Field listField;
    private Field stringProviderField;
    private Field someProviderField;
    private Field providerField;

    @Before
    public void setUp() throws NoSuchFieldException {
        hostsField = TafHostValidationUtilsTest.class.getDeclaredField("hosts");
        objectsField = TafHostValidationUtilsTest.class.getDeclaredField("objects");
        datesField = TafHostValidationUtilsTest.class.getDeclaredField("dates");
        somethingField = TafHostValidationUtilsTest.class.getDeclaredField("something");
        listField = TafHostValidationUtilsTest.class.getDeclaredField("list");
        stringProviderField = TafHostValidationUtilsTest.class.getDeclaredField("stringProvider");
        someProviderField = TafHostValidationUtilsTest.class.getDeclaredField("someProvider");
        providerField = TafHostValidationUtilsTest.class.getDeclaredField("provider");
    }

    @Test
    public void checkType_happyPath() {
        checkCollectionGenericType(hostsField, Host.class);
    }

    @Test
    public void checkType_genericParameter() {
        checkCollectionGenericType(objectsField, Host.class);
    }

    @Test
    public void checkType_wrongParameter() {
        try {
            checkCollectionGenericType(datesField, Host.class);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Class com.ericsson.cifwk.taf.guice.TafHostValidationUtilsTest (field 'dates'): collection expected parameter is com.ericsson.cifwk.taf.data.Host type but was java.util.Date");
        }
    }

    @Test
    public void checkType_unknownParameter() {
        try {
            checkCollectionGenericType(somethingField, Host.class);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Class com.ericsson.cifwk.taf.guice.TafHostValidationUtilsTest (field 'something'): collection is not parametrized");
        }
    }

    @Test
    public void checkType_noParameter() {
        try {
            checkCollectionGenericType(listField, Host.class);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Class com.ericsson.cifwk.taf.guice.TafHostValidationUtilsTest (field 'list'): collection is not parametrized");
        }
    }

    @Test
    public void checkType_happyPath_provider() {
        assertThat(getProviderParameter(stringProviderField)).isEqualTo(String.class);
    }


    @Test
    public void checkType_unknownParameter_provider() {
        try {
            getProviderParameter(someProviderField);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Class com.ericsson.cifwk.taf.guice.TafHostValidationUtilsTest (field 'someProvider'): provider is not parametrized");
        }
    }

    @Test
    public void checkType_noParameter_provider() {
        try {
            getProviderParameter(providerField);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Class com.ericsson.cifwk.taf.guice.TafHostValidationUtilsTest (field 'provider'): provider is not parametrized");
        }
    }



}