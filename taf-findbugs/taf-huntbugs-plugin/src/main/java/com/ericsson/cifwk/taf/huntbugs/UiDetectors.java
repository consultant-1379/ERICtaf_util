package com.ericsson.cifwk.taf.huntbugs;

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.assembler.metadata.annotations.CustomAnnotation;
import one.util.huntbugs.util.AccessLevel;
import one.util.huntbugs.util.Types;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 05.09.2016
 */
public class UiDetectors {

    public static boolean isUiComponent(TypeReference type) {
        return Types.isInstance(type, "com/ericsson/cifwk/taf/ui/core/UiComponent");
    }

    public static boolean isViewModel(TypeReference type) {
        return Types.isInstance(type, "com/ericsson/cifwk/taf/ui/sdk/ViewModel");
    }

    public static boolean isPublic(MethodDefinition md) {
        return AccessLevel.of(md).equals(AccessLevel.PUBLIC);
    }

    public static boolean isWaitUntil(MethodReference mr) {
        Set<String> waitMethods = new HashSet<>();
        waitMethods.add("waitUntil");
        waitMethods.add("waitUntilComponentIsDisplayed");
        return mr != null && waitMethods.contains(mr.getName()) &&
                (isUiComponent(mr.getDeclaringType()) || Types.isInstance(mr.getDeclaringType(), "com/ericsson/cifwk/taf/ui/ConditionWait"));
    }

    public static boolean isUiComponentMethod(MethodReference mr) {
        return isUiComponent(mr.getDeclaringType());
    }

    public static CustomAnnotation getUiComponentMappingAnnotation(FieldDefinition fd) {
        return Annotations.getAnnotation(fd, "com.ericsson.cifwk.taf.ui.core.UiComponentMapping");
    }

    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

}
