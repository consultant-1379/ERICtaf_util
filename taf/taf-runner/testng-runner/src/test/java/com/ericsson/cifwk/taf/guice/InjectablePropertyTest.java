package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.configuration.TafProperty;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Properties;

import static com.ericsson.cifwk.taf.configuration.TafConfigurationProvider.provide;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class InjectablePropertyTest {

    public static final String STRING_KEY = "executionType";

    public static final String INTEGER_KEY = "timeoutMillis";

    public static final String BOOLEAN_KEY = "debugEnabled";

    public static final String CLASS_KEY = "implementationClass";

    public static final String FILE_KEY = "propertiesFile";

    public static final String URL_KEY = "propertiesLocation";

    public static final String NOT_EXISTING_KEY = "notExistingKey";

    public static final String INTEGER_VALUE = "2000";

    public static final String STRING_VALUE = "grid";

    public static final String DEFAULT_STRING_VALUE = "defaultGrid";

    public static final String MY_CUSTOM_OBJECT = "myCustomObject";

    private Injector injector;

    private InjectionTarget target;

    @Before
    public void setUp() {
        TafConfiguration tafConfiguration = provide();
        tafConfiguration.setProperty(STRING_KEY, STRING_VALUE);
        tafConfiguration.setProperty(INTEGER_KEY, INTEGER_VALUE);
        tafConfiguration.setProperty(BOOLEAN_KEY, "true");
        tafConfiguration.setProperty(CLASS_KEY, getClass().getName());
        tafConfiguration.setProperty(FILE_KEY, "logs.txt");
        tafConfiguration.setProperty(URL_KEY, "http://ericsson.com/");

        injector = Guice.createInjector(new TafGuiceModule());
        target = injector.getInstance(InjectionTarget.class);
    }

    @Test
    public void propertyInjection() {

        // injection
        assertThat(target.string).isEqualTo(STRING_VALUE);
        assertThat(target.integer).isEqualTo(2000);
        assertThat(target.longValue).isEqualTo(2000);
        assertThat(target.booleanValue).isEqualTo(true);

        // falling back to default values
        assertThat(target.defaultString).isEqualTo(DEFAULT_STRING_VALUE);
        assertThat(target.defaultInteger).isEqualTo(1);
        assertThat(target.defaultLong).isEqualTo(2);
        assertThat(target.defaultBoolean).isEqualTo(true);

        // generic objects
        assertThat(target.object).isEqualTo(STRING_VALUE);
        assertThat(target.defaultObject).isEqualTo(DEFAULT_STRING_VALUE);

        // other types
        assertThat(target.bigDecimal).isEqualTo("2000");
        assertThat(target.file).hasName("logs.txt");
        assertThat(target.url).hasHost("ericsson.com");
        assertThat(target.clazz).isEqualTo(getClass());
    }

    @Test
    public void runtimePropertyInjection() {

        // runtime configuration override
        assertThat(target.stringProvider.get()).isEqualTo(STRING_VALUE);
        TafConfiguration tafConfiguration = TafConfigurationProvider.provide();
        tafConfiguration.setProperty(STRING_KEY, INTEGER_VALUE);
        assertThat(target.stringProvider.get()).isEqualTo(INTEGER_VALUE);
        assertThat(STRING_VALUE).isNotEqualTo(INTEGER_VALUE);
        tafConfiguration.setProperty(STRING_KEY, STRING_VALUE);

        // supporting both Google and Javax providers
        assertThat(target.googleProvider.get()).isEqualTo(STRING_VALUE);
        assertThat(target.javaxProvider.get()).isEqualTo(2000);

        // checking other injector features available for providers
        assertThat(target.providerWithDefaultValue.get().intValue()).isEqualTo(123);
    }

    @Test
    public void conversionError() {
        try {
            injector.getInstance(IntegerConversionError.class);
            fail();
        } catch (ProvisionException o) {
            Throwable e = o.getCause();
            assertThat(e).hasMessageContaining("Unable to inject property in Class com.ericsson.cifwk.taf.guice.InjectablePropertyTest.IntegerConversionError (field 'integer')");
            assertThat(e.getCause()).hasMessageContaining("Unable to convert value 'grid' to type java.lang.Integer");
            assertThat(e.getCause()).hasMessageContaining("Please correct injection field type, property key or property definition.");
        }

        try {
            injector.getInstance(BooleanConversionError.class);
            fail();
        } catch (ProvisionException o) {
            Throwable e = o.getCause();
            assertThat(e).hasMessageContaining("Unable to inject property in Class com.ericsson.cifwk.taf.guice.InjectablePropertyTest.BooleanConversionError (field 'bool')");
            assertThat(e.getCause()).hasMessageContaining("Unable to convert value '2000' to type boolean");
            assertThat(e.getCause()).hasMessageContaining("Please correct injection field type, property key or property definition.");
        }
    }

    @Test
    public void conversionError_Provider() {
        try {
            target.integerConversion.get();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("Unable to inject property in Class com.ericsson.cifwk.taf.guice.InjectablePropertyTest.InjectionTarget (field 'integerConversion')");
            assertThat(e.getCause()).hasMessageContaining("Unable to convert value 'grid' to type java.lang.Integer");
            assertThat(e.getCause()).hasMessageContaining("Please correct injection field type, property key or property definition.");
        }
    }

    @Test
    public void defaultValueConversionError() {
        try {
            injector.getInstance(DefaultValueConversionError.class);
            fail();
        } catch (ProvisionException o) {
            Throwable e = o.getCause();
            assertThat(e).hasMessageContaining("Unable to inject property in Class com.ericsson.cifwk.taf.guice.InjectablePropertyTest.DefaultValueConversionError (field 'defaultInteger')");
            assertThat(e.getCause()).hasMessageContaining("Unable to convert default value 'string' to type java.lang.Integer");
            assertThat(e.getCause()).hasMessageContaining("Please correct injection field type or property default value.");
        }
    }

    @Test
    public void defaultValueConversionError_provider() {
        try {
            target.defaultIntegerConversion.get();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("Unable to inject property in Class com.ericsson.cifwk.taf.guice.InjectablePropertyTest.InjectionTarget (field 'defaultIntegerConversion')");
            assertThat(e.getCause()).hasMessageContaining("Unable to convert default value 'string' to type java.lang.Integer");
            assertThat(e.getCause()).hasMessageContaining("Please correct injection field type or property default value.");
        }
    }

    @Test
    public void defaultValuePostponedConversionError() {
        try {
            injector.getInstance(DefaultValuePostponedConversionError.class);
            fail();
        } catch (ProvisionException e) {
            Throwable t = e.getCause();
            assertThat(t).hasMessageContaining("Unable to inject property in Class com.ericsson.cifwk.taf.guice.InjectablePropertyTest.DefaultValuePostponedConversionError (field 'defaultInteger')");
            assertThat(t.getCause()).hasMessageContaining("Unable to convert default value 'string' to type java.lang.Integer");
            assertThat(t.getCause()).hasMessageContaining("Please correct injection field type or property default value.");
        }
    }

    @Test
    public void defaultValuePostponedConversionError_Provider() {
        try {
            target.eagerDefaultIntegerConversion.get();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("Unable to inject property in Class com.ericsson.cifwk.taf.guice.InjectablePropertyTest.InjectionTarget (field 'eagerDefaultIntegerConversion')");
            assertThat(e.getCause()).hasMessageContaining("Unable to convert default value 'string' to type java.lang.Integer");
            assertThat(e.getCause()).hasMessageContaining("Please correct injection field type or property default value.");
        }
    }

    @Test
    public void noValueFound() {
        try {
            injector.getInstance(NoValueFound.class);
            fail();
        } catch (ProvisionException o) {
            Throwable e = o.getCause();
            assertThat(e).hasMessageContaining("Unable to inject property in Class com.ericsson.cifwk.taf.guice.InjectablePropertyTest.NoValueFound (field 'unknown')");
            assertThat(e.getCause()).hasMessageContaining("No property found by key 'notExistingKey' (and default value was not provided)");
            assertThat(e.getCause()).hasMessageContaining("Properties available: ");
            assertThat(e.getCause()).hasMessageContaining("timeoutMillis=2000");
            assertThat(e.getCause()).hasMessageContaining("executionType=grid");
        }
    }

    @Test
    public void noValueFound_Provider() {
        try {
            target.notFound.get();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("Unable to inject property in Class com.ericsson.cifwk.taf.guice.InjectablePropertyTest.InjectionTarget (field 'notFound')");
            assertThat(e.getCause()).hasMessageContaining("No property found by key 'notExistingKey' (and default value was not provided)");
            assertThat(e.getCause()).hasMessageContaining("Properties available: ");
            assertThat(e.getCause()).hasMessageContaining("timeoutMillis=2000");
            assertThat(e.getCause()).hasMessageContaining("executionType=grid");
        }
    }

    @Test
    public void unsupportedFieldType() {
        try {
            injector.getInstance(UnsupportedFieldType.class);
            fail();
        } catch (ProvisionException o) {
            Throwable e = o.getCause();
            assertThat(e).hasMessageContaining("Unable to inject property in Class com.ericsson.cifwk.taf.guice.InjectablePropertyTest.UnsupportedFieldType (field 'integer')");
            assertThat(e.getCause()).hasMessageContaining("Field type java.util.Properties is not assignable to value of type java.lang.String");
        }
    }

    @Test
    public void unsupportedFieldType_Provider() {
        try {
            target.unsupportedTypeProvider.get();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("Unable to inject property in Class com.ericsson.cifwk.taf.guice.InjectablePropertyTest.InjectionTarget (field 'unsupportedTypeProvider')");
            assertThat(e.getCause()).hasMessageContaining("Field type java.util.Properties is not assignable to value of type java.lang.String");
        }
    }

    @Test
    public void abstractFieldType() {
        try {
            injector.getInstance(AbstractFieldType.class);
            fail();
        } catch (ProvisionException o) {
            Throwable e = o.getCause();
            assertThat(e).hasMessageContaining("Unable to inject property in Class com.ericsson.cifwk.taf.guice.InjectablePropertyTest.AbstractFieldType (field 'integer'):");
            assertThat(e.getCause()).hasMessageContaining("Field type java.lang.Number is not assignable to value of type java.lang.String");
        }
    }

    @Test
    public void abstractFieldType_Provider() {
        try {
            target.abstractTypeProvider.get();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("Unable to inject property in Class com.ericsson.cifwk.taf.guice.InjectablePropertyTest.InjectionTarget (field 'abstractTypeProvider'):");
            assertThat(e.getCause()).hasMessageContaining("Field type java.lang.Number is not assignable to value of type java.lang.String");
        }
    }

    @Test
    public void customObjectInjection() {
        provide().setProperty(MY_CUSTOM_OBJECT, this);
        CustomObjectContainer container = injector.getInstance(CustomObjectContainer.class);
        assertThat(container.test).isSameAs(this);
    }

    @Test
    public void customObjectInjection_Provider() {
        provide().setProperty(MY_CUSTOM_OBJECT, this);
        assertThat(target.test.get()).isSameAs(this);
    }

    public static class InjectionTarget {

        // defined properties
        @TafProperty(value = STRING_KEY, defaultValue = DEFAULT_STRING_VALUE)
        private String string;
        @TafProperty(INTEGER_KEY)
        private int integer;
        @TafProperty(INTEGER_KEY)
        private Long longValue;
        @TafProperty(BOOLEAN_KEY)
        private boolean booleanValue;

        // falling back to default value
        @TafProperty(value = NOT_EXISTING_KEY, defaultValue = DEFAULT_STRING_VALUE)
        private String defaultString;
        @TafProperty(value = NOT_EXISTING_KEY, defaultValue = "1")
        private int defaultInteger;
        @TafProperty(value = NOT_EXISTING_KEY, defaultValue = "2")
        private Long defaultLong;
        @TafProperty(value = NOT_EXISTING_KEY, defaultValue = "true")
        private Boolean defaultBoolean;

        // generic fields
        @TafProperty(STRING_KEY)
        private Object object;
        @TafProperty(value = NOT_EXISTING_KEY, defaultValue = DEFAULT_STRING_VALUE)
        private Object defaultObject;

        // other types
        @TafProperty(INTEGER_KEY)
        private BigDecimal bigDecimal;
        @TafProperty(CLASS_KEY)
        private Class clazz;
        @TafProperty(FILE_KEY)
        private File file;
        @TafProperty(URL_KEY)
        private URL url;

        // runtime properties
        @TafProperty(value = STRING_KEY)
        private Provider<String> stringProvider;
        @TafProperty(value = STRING_KEY)
        private com.google.inject.Provider<String> googleProvider;
        @TafProperty(value = INTEGER_KEY)
        private javax.inject.Provider<Integer> javaxProvider;
        @TafProperty(value = NOT_EXISTING_KEY, defaultValue = "123")
        private Provider<BigInteger> providerWithDefaultValue;

        // exceptional providers
        @TafProperty(value = NOT_EXISTING_KEY)
        private Provider<Boolean> notFound;
        @TafProperty(value = STRING_KEY)
        private Provider<Integer> integerConversion;
        @TafProperty(value = NOT_EXISTING_KEY, defaultValue = "string")
        private Provider<Integer> defaultIntegerConversion;
        @TafProperty(value = INTEGER_KEY, defaultValue = "string")
        private Provider<Integer> eagerDefaultIntegerConversion;
        @TafProperty(INTEGER_KEY)
        private Provider<Properties> unsupportedTypeProvider;
        @TafProperty(INTEGER_KEY)
        private Provider<Number> abstractTypeProvider;
        @TafProperty(MY_CUSTOM_OBJECT)
        private Provider<InjectablePropertyTest> test;
    }

    public static class IntegerConversionError {

        @TafProperty(STRING_KEY)
        private Integer integer;

    }

    public static class BooleanConversionError {

        @TafProperty(INTEGER_KEY)
        private boolean bool;

    }

    public static class DefaultValueConversionError {

        @TafProperty(value = NOT_EXISTING_KEY, defaultValue = "string")
        private Integer defaultInteger;

    }

    public static class DefaultValuePostponedConversionError {

        @TafProperty(value = INTEGER_KEY, defaultValue = "string")
        private Integer defaultInteger;

    }

    public static class NoValueFound {

        @TafProperty(NOT_EXISTING_KEY)
        private String unknown;

    }

    public static class UnsupportedFieldType {

        @TafProperty(INTEGER_KEY)
        private Properties integer;

    }

    public static class AbstractFieldType {

        @TafProperty(INTEGER_KEY)
        private Number integer;

    }

    private static class CustomObjectContainer {

        @TafProperty(MY_CUSTOM_OBJECT)
        private InjectablePropertyTest test;

    }
}
