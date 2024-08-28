package com.ericsson.cifwk.taf.swt.agent.selector;

import com.ericsson.cifwk.meta.API;
import com.google.gson.Gson;

import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtSelector {

    private static final Gson gson = new Gson();

    private String type;

    private int index = 0;

    private boolean nativeWidget = false;

    private String id;

    private String customIdKey;

    private String customIdValue;

    private String text;

    private String mnemonicText;

    private String tooltip;

    private String label;

    private String wrapperType;

    private String[] initActions;

    private String container;

    private String data;

    private Map<String, Object> dataProperties;

    private Map<String, String> widgetProperties;

    public String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomIdKey() {
        return customIdKey;
    }

    public void setCustomIdKey(String customIdKey) {
        this.customIdKey = customIdKey;
    }

    public String getCustomIdValue() {
        return customIdValue;
    }

    public void setCustomIdValue(String customIdValue) {
        this.customIdValue = customIdValue;
    }

    void setText(String text) {
        this.text = text;
    }

    public String getContainer() {
        return container;
    }

    void setContainer(String container) {
        this.container = container;
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isNativeWidget() {
        return nativeWidget;
    }

    public void setNativeWidget(boolean nativeWidget) {
        this.nativeWidget = nativeWidget;
    }

    public String getMnemonicText() {
        return mnemonicText;
    }

    public void setMnemonicText(String mnemonicText) {
        this.mnemonicText = mnemonicText;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String[] getInitActions() {
        return initActions;
    }

    public void setInitActions(String[] initActions) {
        this.initActions = initActions;
    }

    public String getWrapperType() {
        return wrapperType;
    }

    public void setWrapperType(String wrapperType) {
        this.wrapperType = wrapperType;
    }

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

    public Map<String, Object> getDataProperties() {
        return dataProperties;
    }

    public void setDataProperties(Map<String, Object> dataProperties) {
        this.dataProperties = dataProperties;
    }

    public Map<String, String> getWidgetProperties() {
        return widgetProperties;
    }

    public void setWidgetProperties(Map<String, String> widgetProperties) {
        this.widgetProperties = widgetProperties;
    }
}
