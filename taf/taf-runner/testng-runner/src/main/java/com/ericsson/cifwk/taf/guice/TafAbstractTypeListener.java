package com.ericsson.cifwk.taf.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.Field;

abstract class TafAbstractTypeListener implements TypeListener {

    public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
        Class<?> clazz = typeLiteral.getRawType();
        while (clazz != null) {
            registerMembers(typeEncounter, clazz);
            clazz = clazz.getSuperclass();
        }
    }

    private <T> void registerMembers(TypeEncounter<T> typeEncounter, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (memberShouldBeRegistered(field)) {
                registerMember(typeEncounter, field);
            }
        }
    }

    protected abstract boolean memberShouldBeRegistered(Field field);

    protected abstract <T> void registerMember(TypeEncounter<T> typeEncounter, Field field);
}
