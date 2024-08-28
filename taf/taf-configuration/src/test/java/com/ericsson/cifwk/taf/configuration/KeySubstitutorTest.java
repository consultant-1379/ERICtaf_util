package com.ericsson.cifwk.taf.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class KeySubstitutorTest {

    @Test
    public void shouldVerifyExistenceOfKey(){
        boolean result = KeySubstitutor.stringValueContainsKeyIdentifier("Hello there ${name}");
        assertThat(result).isTrue();
    }

    @Test
    public void shouldVerifyExistenceOfKeyComplete(){
        boolean result = KeySubstitutor.stringValueContainsKeyIdentifier("${name}");
        assertThat(result).isTrue();
    }

    @Test
    public void shouldOnlyCheckStringForKey(){
        boolean result = KeySubstitutor.stringValueContainsKeyIdentifier(new Object());
        assertThat(result).isFalse();
    }

    @Test
    public void shouldReturnFalseForEmptyString(){
        boolean result = KeySubstitutor.stringValueContainsKeyIdentifier("");
        assertThat(result).isFalse();
    }

    @Test
    public void shouldReturnFalseForEmptyKey(){
        boolean result = KeySubstitutor.stringValueContainsKeyIdentifier("${}");
        assertThat(result).isFalse();
    }

    @Test
    public void shouldVerifyAbsenceOfKey(){
        boolean result = KeySubstitutor.stringValueContainsKeyIdentifier("Hello there fellow");
        assertThat(result).isFalse();
    }

    @Test
    public void shouldReplaceKeyWithValue(){
        System.setProperty("myKey", "myValue");
        String replacedValue = KeySubstitutor.replaceKeyWithValue("${myKey}");
        assertThat(replacedValue).matches("myValue");
    }

    @Test
    public void shouldNotReplaceNonExistentKey(){
        String replacedValue = KeySubstitutor.replaceKeyWithValue("${myOtherKey}");
        assertThat(replacedValue).matches("\\$\\{myOtherKey\\}");
    }
}
