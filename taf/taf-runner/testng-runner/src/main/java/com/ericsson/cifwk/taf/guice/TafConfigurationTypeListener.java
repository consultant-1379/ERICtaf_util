package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.configuration.TafProperty;
import com.google.inject.spi.TypeEncounter;

import java.lang.reflect.Field;

class TafConfigurationTypeListener extends TafAbstractTypeListener {

    @Override
    protected boolean memberShouldBeRegistered(Field field) {
        return field.isAnnotationPresent(TafProperty.class);
    }

    @Override
    protected <T> void registerMember(TypeEncounter<T> typeEncounter, Field field) {
        typeEncounter.register(new TafConfigurationInjector<T>(field));
    }
}
