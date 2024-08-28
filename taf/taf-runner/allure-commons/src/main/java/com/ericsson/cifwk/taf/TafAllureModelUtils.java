package com.ericsson.cifwk.taf;

import ru.yandex.qatools.allure.model.Label;

/**
 * Helper class for easy label creation
 *
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 04/08/2015
 *         <p/>
 */
public final class TafAllureModelUtils {

    private TafAllureModelUtils() {
        // hiding constructor
    }

    public static Label createManualExecutionTypeLabel() {
        return createLabel(TafLabelName.EXECUTION_TYPE, "manual");
    }

    public static Label createAutomatedExecutionTypeLabel() {
        return createLabel(TafLabelName.EXECUTION_TYPE, "automated");
    }

    public static Label createCommentLabel(String comment) {
        if (comment == null) {
            comment = "";
        }
        return createLabel(TafLabelName.COMMENT, comment);
    }

    public static Label createGroupLabel(String group) {
        return createLabel(TafLabelName.GROUP, group);
    }

    public static Label createLabel(TafLabelName name, String value) {
        return new Label().withName(name.value()).withValue(value);
    }


}
