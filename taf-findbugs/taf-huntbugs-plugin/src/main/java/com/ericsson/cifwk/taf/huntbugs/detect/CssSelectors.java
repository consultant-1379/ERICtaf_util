package com.ericsson.cifwk.taf.huntbugs.detect;

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.annotations.CustomAnnotation;
import one.util.huntbugs.db.FieldStats;
import one.util.huntbugs.registry.FieldContext;
import one.util.huntbugs.registry.anno.FieldVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;
import one.util.huntbugs.registry.anno.WarningDefinitions;

import static com.ericsson.cifwk.taf.huntbugs.Annotations.getAnnotationConstantParameter;
import static com.ericsson.cifwk.taf.huntbugs.UiDetectors.getUiComponentMappingAnnotation;
import static com.ericsson.cifwk.taf.huntbugs.UiDetectors.isEmpty;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 12.09.2016
 */
@WarningDefinitions({
        @WarningDefinition(category = "TAF UI", name = "LongVersionOfCssSelector", maxScore = 50),
        @WarningDefinition(category = "TAF UI", name = "EmptySelector", maxScore = 50),
        @WarningDefinition(category = "TAF UI", name = "TooManySelectorsDefined", maxScore = 50)
})
public class CssSelectors {

    @FieldVisitor
    public void visit(FieldContext fc, FieldDefinition fd, TypeDefinition td, FieldStats fs) {

        if(fd.isSynthetic() || fd.isEnumConstant()) {
            return;
        }

        // we are interested in
        CustomAnnotation mappingAnnotation = getUiComponentMappingAnnotation(fd);
        if (mappingAnnotation == null) {
            return;
        }

        String annotationValue = getAnnotationConstantParameter(mappingAnnotation, "value");
        String annotationSelector = getAnnotationConstantParameter(mappingAnnotation, "selector");
        String annotationSelectorType = getAnnotationConstantParameter(mappingAnnotation, "selectorType");

        // selector is never defined
        if (isEmpty(annotationValue) && isEmpty(annotationSelector)) {
            fc.report("EmptySelector", 10);
            return;
        }

        // selector is defined twice
        if (!isEmpty(annotationValue) && !isEmpty(annotationSelector)) {
            fc.report("TooManySelectorsDefined", 10);
            return;
        }

        // default CSS selector is used - value annotation parameter should be defined
        if (isEmpty(annotationSelectorType)) {
            if (isEmpty(annotationValue)) {
                fc.report("LongVersionOfCssSelector", 10);
                return;
            }
        }

        // CSS selector is used - value annotation parameter should be defined
        if ("CSS".equals(annotationSelectorType)) {
            fc.report("LongVersionOfCssSelector", 10);
            return;
        }

    }

}
