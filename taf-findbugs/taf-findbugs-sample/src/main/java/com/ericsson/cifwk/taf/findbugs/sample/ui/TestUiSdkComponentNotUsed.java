package com.ericsson.cifwk.taf.findbugs.sample.ui;

import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.Link;
import com.ericsson.cifwk.taf.ui.sdk.RadioButton;
import com.ericsson.cifwk.taf.ui.sdk.TextBox;
import one.util.huntbugs.registry.anno.AssertWarning;

import java.util.List;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 06.09.2016
 */
public class TestUiSdkComponentNotUsed {

    public static final String UI_SDK_COMPONENT_NOT_USED = "UiSdkComponentNotUsed";

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping(".ebAccordion-button i")
    private UiComponent accordionPart;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping(selector = ".ebBreadcrumbs-item")
    private List<UiComponent> breadcrumbItems;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping(".ebCombobox > button.ebCombobox-helper")
    private Button comboboxOpener;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping("button.ebComboMultiSelect-helper")
    private Button comboMultiSelectOpener;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping(".ebDatePicker-body")
    private UiComponent monthDaysTable;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping(selector = ".ebDropdown-button")
    private Button dropDownOpener;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping(".elWidgets-EditableField-Input-Textbox.ebInput")
    private TextBox input;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping(selector = ".ebDialogBox .ebDialogBox-contentBlock")
    private UiComponent dialogContent;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping("button.ebSelect-header")
    private Button selectOpener;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping(".ebPagination-pages")
    private Link pages;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping(".elWidgets-PopupDatePicker-inputWrapper .ebInput.elWidgets-PopupDatePicker-input")
    private TextBox dateInput;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping(".ebRadioBtn")
    private List<RadioButton> radioButtons;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping(selector = ".ebSpinner-inputHolder input")
    private TextBox integerInput;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping(selector = ".ebSwitcher")
    private UiComponent switcher;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping("table thead .ebTableRow")
    private UiComponent headRow;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping(".ebTabs-top .ebTabs-tabArea .ebTabs-tabItem.ebTabs-tabItem_selected_true")
    private UiComponent selectedTab;

    @AssertWarning(UI_SDK_COMPONENT_NOT_USED)
    @UiComponentMapping("ul.ebTree > li.ebTreeItem")
    private List<Button> treeItems;

}
