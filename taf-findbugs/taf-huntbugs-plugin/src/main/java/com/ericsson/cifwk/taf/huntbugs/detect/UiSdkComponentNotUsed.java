package com.ericsson.cifwk.taf.huntbugs.detect;

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.annotations.CustomAnnotation;
import one.util.huntbugs.registry.FieldContext;
import one.util.huntbugs.registry.anno.FieldVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;
import one.util.huntbugs.warning.Role.StringRole;

import java.util.HashMap;
import java.util.Map;

import static com.ericsson.cifwk.taf.huntbugs.Annotations.getAnnotationConstantParameter;
import static com.ericsson.cifwk.taf.huntbugs.UiDetectors.getUiComponentMappingAnnotation;
import static com.ericsson.cifwk.taf.huntbugs.UiDetectors.isEmpty;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 06.09.2016
 */
@WarningDefinition(category = "TAF UI", name = "UiSdkComponentNotUsed", maxScore = 50)
public class UiSdkComponentNotUsed {

    private static final StringRole UI_COMPONENT = StringRole.forName("UI_COMPONENT");

    private static final Map<String, String> knownMappings = new HashMap<>();

    static {
        knownMappings.put("ebAccordion", "Accordion");
        knownMappings.put("ebBreadcrumbs", "Breadcrumb");
        knownMappings.put("ebCombobox", "UiSdkComboBox");
        knownMappings.put("ebComboMultiSelect", "UiSdkComboMultiSelectBox");
        knownMappings.put("ebDatePicker", "DatePicker");
        knownMappings.put("ebDropdown", "UiSdkDropDownMenu");
        knownMappings.put("elWidgets-EditableField", "EditableField");
        knownMappings.put("ebDialogBox", "ModalDialog");
        knownMappings.put("ebSelect", "UiSdkSelectBox or UiSdkMultiSelectBox");
        knownMappings.put("ebPagination-pages", "Pagination");
        knownMappings.put("elWidgets-PopupDatePicker", "PopupDatePicker");
        knownMappings.put("ebProgressBar", "ProgressBar");
        knownMappings.put("ebRadioBtn", "RadioButtonGroup");
        knownMappings.put("ebSpinner", "Spinner");
        knownMappings.put("ebSwitcher", "Switcher");
        knownMappings.put("ebTableRow", "Table");
        knownMappings.put("ebTabs", "Tabs");
        knownMappings.put("ebTree", "Tree");
    }

    @FieldVisitor
    public void visit(FieldContext fc, FieldDefinition fd) {

        if(fd.isSynthetic() || fd.isEnumConstant()) {
            return;
        }

        CustomAnnotation mappingAnnotation = getUiComponentMappingAnnotation(fd);
        if (mappingAnnotation == null) {
            return;
        }

        String mapping;
        String annotationValue = getAnnotationConstantParameter(mappingAnnotation, "value");
        String annotationSelector = getAnnotationConstantParameter(mappingAnnotation, "selector");
        if (!isEmpty(annotationValue)) {
            mapping = annotationValue;
        } else if (!isEmpty(annotationSelector)) {
            mapping = annotationSelector;
        } else {
            return;
        }

        for (String knownMapping : knownMappings.keySet()) {
            if (mapping.contains(knownMapping)) {
                String component = knownMappings.get(knownMapping);
                fc.report("UiSdkComponentNotUsed", 10, UI_COMPONENT.create(component));
            }
        }
    }

}
