package com.ericsson.cifwk.taf.huntbugs;

import com.strobel.assembler.metadata.IAnnotationsProvider;
import com.strobel.assembler.metadata.annotations.AnnotationElement;
import com.strobel.assembler.metadata.annotations.AnnotationParameter;
import com.strobel.assembler.metadata.annotations.ConstantAnnotationElement;
import com.strobel.assembler.metadata.annotations.CustomAnnotation;

import java.util.List;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 20.10.2016
 */
public class Annotations {

    public static boolean hasAnnotation(IAnnotationsProvider annotationsProvider, String annotationClass) {
        return getAnnotation(annotationsProvider, annotationClass) != null;
    }

    public static CustomAnnotation getAnnotation(IAnnotationsProvider annotationsProvider, String annotationClass) {
        for (CustomAnnotation annotation : annotationsProvider.getAnnotations()) {
            if (annotationClass.equals(annotation.getAnnotationType().getFullName())) {
                return annotation;
            }
        }
        return null;
    }

    public static <T> T getAnnotationConstantParameter(CustomAnnotation annotation, String parameterName) {
        List<AnnotationParameter> parameters = annotation.getParameters();
        for (AnnotationParameter parameter : parameters) {
            String member = parameter.getMember();
            if (parameterName.equals(member)) {
                AnnotationElement value = parameter.getValue();
                if (value instanceof ConstantAnnotationElement) {
                    return (T) ((ConstantAnnotationElement) value).getConstantValue();
                }
            }
        }
        return null;
    }

}
