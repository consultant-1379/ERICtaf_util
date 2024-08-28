package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.data.Host;
import com.google.inject.spi.TypeEncounter;

import java.lang.reflect.Field;

import static com.ericsson.cifwk.taf.guice.ParameterizedFieldUtils.checkCollectionGenericType;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.isHostAnnotationPresent;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.isHostTarget;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.isList;

class TafHostTypeListener extends TafAbstractTypeListener {

    @Override
    protected boolean memberShouldBeRegistered(Field field) {
        return isHostTarget(field) && isHostAnnotationPresent(field);
    }

    @Override
    protected <T> void registerMember(TypeEncounter<T> typeEncounter, Field field) {
        if (isList(field)) {
            checkCollectionGenericType(field, Host.class);
        }
        typeEncounter.register(new TafHostInjector<T>(field));
    }
}
