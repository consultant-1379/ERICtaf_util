/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.tms;

import com.ericsson.cifwk.taf.tms.dto.ReferenceDataItem;
import ru.yandex.qatools.allure.model.SeverityLevel;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class PriorityConverter {

    private static Map<String, SeverityLevel> severities = newHashMap();

    static {

        // old TMS priorities: to be removed
        severities.put("HIGH", SeverityLevel.BLOCKER);
        severities.put("MEDIUM", SeverityLevel.NORMAL);
        severities.put("LOW", SeverityLevel.MINOR);

        // deprecated TMS priority: to be removed
        severities.put("UNKNOWN", SeverityLevel.MINOR);

        // TMS priorities aligned to Allure report severities
        severities.put("BLOCKER", SeverityLevel.BLOCKER);
        severities.put("CRITICAL", SeverityLevel.CRITICAL);
        severities.put("NORMAL", SeverityLevel.NORMAL);
        severities.put("MINOR", SeverityLevel.MINOR);
        severities.put("TRIVIAL", SeverityLevel.TRIVIAL);
    }

    private PriorityConverter() {
        // hiding constructor
    }

    public static SeverityLevel toSeverity(ReferenceDataItem priority) {
        return toSeverity(priority == null ? null : priority.getTitle());
    }

    public static SeverityLevel toSeverity(String priority) {
        SeverityLevel severityLevel = severities.get(priority == null ? null : priority.toUpperCase());
        return severityLevel == null ? SeverityLevel.MINOR : severityLevel;
    }

}
