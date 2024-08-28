/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.swt.agent.introspection;

import com.ericsson.cifwk.meta.API;

import java.util.ArrayList;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

@API(Stable)
public class WindowDto {

    private List<WidgetDto> arrowButtons = new ArrayList<WidgetDto>();
    private List<WidgetDto> buttons = new ArrayList<WidgetDto>();
    private List<WidgetDto> browsers = new ArrayList<WidgetDto>();
    private List<WidgetDto> checkBoxes = new ArrayList<WidgetDto>();
    private List<WidgetDto> comboBoxes = new ArrayList<WidgetDto>();
    private List<WidgetDto> dateTimes = new ArrayList<WidgetDto>();
    private List<WidgetDto> expandBars = new ArrayList<WidgetDto>();
    private List<WidgetDto> labels = new ArrayList<WidgetDto>();
    private List<WidgetDto> links = new ArrayList<WidgetDto>();
    private List<WidgetDto> lists = new ArrayList<WidgetDto>();
    private List<WidgetDto> radios = new ArrayList<WidgetDto>();
    private List<WidgetDto> scales = new ArrayList<WidgetDto>();
    private List<WidgetDto> sliders = new ArrayList<WidgetDto>();
    private List<WidgetDto> spinners = new ArrayList<WidgetDto>();
    private List<WidgetDto> styledTexts = new ArrayList<WidgetDto>();
    private List<WidgetDto> tabItems = new ArrayList<WidgetDto>();
    private List<WidgetDto> tables = new ArrayList<WidgetDto>();
    private List<WidgetDto> textBoxes = new ArrayList<WidgetDto>();
    private List<WidgetDto> toggleButtons = new ArrayList<WidgetDto>();
    private List<WidgetDto> trayItems = new ArrayList<WidgetDto>();
    private List<WidgetDto> trees = new ArrayList<WidgetDto>();

    private List<WidgetDto> ccomboBoxes = new ArrayList<WidgetDto>();
    private List<WidgetDto> clabels = new ArrayList<WidgetDto>();
    private List<WidgetDto> ctabItems = new ArrayList<WidgetDto>();

    private List<WidgetDto> toolbarDropDownButtons = new ArrayList<WidgetDto>();
    private List<WidgetDto> toolbarRadioButtons = new ArrayList<WidgetDto>();
    private List<WidgetDto> toolbarToggleButtons = new ArrayList<WidgetDto>();

    public List<WidgetDto> getButtons() {
        return buttons;
    }

    public List<WidgetDto> getTextBoxes() {
        return textBoxes;
    }

    public List<WidgetDto> getArrowButtons() {
        return arrowButtons;
    }

    public List<WidgetDto> getBrowsers() {
        return browsers;
    }

    public List<WidgetDto> getCheckBoxes() {
        return checkBoxes;
    }

    public List<WidgetDto> getComboBoxes() {
        return comboBoxes;
    }

    public List<WidgetDto> getDateTimes() {
        return dateTimes;
    }

    public List<WidgetDto> getExpandBars() {
        return expandBars;
    }

    public List<WidgetDto> getLabels() {
        return labels;
    }

    public List<WidgetDto> getLinks() {
        return links;
    }

    public List<WidgetDto> getLists() {
        return lists;
    }

    public List<WidgetDto> getRadios() {
        return radios;
    }

    public List<WidgetDto> getScales() {
        return scales;
    }

    public List<WidgetDto> getSliders() {
        return sliders;
    }

    public List<WidgetDto> getSpinners() {
        return spinners;
    }

    public List<WidgetDto> getStyledTexts() {
        return styledTexts;
    }

    public List<WidgetDto> getTabItems() {
        return tabItems;
    }

    public List<WidgetDto> getTables() {
        return tables;
    }

    public List<WidgetDto> getToggleButtons() {
        return toggleButtons;
    }

    public List<WidgetDto> getTrayItems() {
        return trayItems;
    }

    public List<WidgetDto> getTrees() {
        return trees;
    }

    public List<WidgetDto> getCcomboBoxes() {
        return ccomboBoxes;
    }

    public List<WidgetDto> getClabels() {
        return clabels;
    }

    public List<WidgetDto> getCtabItems() {
        return ctabItems;
    }

    public List<WidgetDto> getToolbarDropDownButtons() {
        return toolbarDropDownButtons;
    }

    public List<WidgetDto> getToolbarRadioButtons() {
        return toolbarRadioButtons;
    }

    public List<WidgetDto> getToolbarToggleButtons() {
        return toolbarToggleButtons;
    }
}
