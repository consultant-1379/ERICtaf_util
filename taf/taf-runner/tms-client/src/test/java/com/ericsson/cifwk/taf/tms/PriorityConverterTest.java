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
import org.junit.Test;
import ru.yandex.qatools.allure.model.SeverityLevel;

import static org.junit.Assert.assertEquals;

public class PriorityConverterTest {

    @Test
    public void toSeverity() {

        // invalid values
        assertEquals(SeverityLevel.MINOR, PriorityConverter.toSeverity((ReferenceDataItem) null));
        assertEquals(SeverityLevel.MINOR, PriorityConverter.toSeverity((String) null));
        assertEquals(SeverityLevel.MINOR, PriorityConverter.toSeverity("Unexpected"));
        assertEquals(SeverityLevel.MINOR, PriorityConverter.toSeverity("Unknown"));

        // old TMS priorities to be removed
        assertEquals(SeverityLevel.BLOCKER, PriorityConverter.toSeverity("High"));
        assertEquals(SeverityLevel.NORMAL, PriorityConverter.toSeverity("Medium"));
        assertEquals(SeverityLevel.MINOR, PriorityConverter.toSeverity("Low"));

        // TMS priorities aligned to Allure severities
        assertEquals(SeverityLevel.BLOCKER, PriorityConverter.toSeverity("BLOCKER"));
        assertEquals(SeverityLevel.NORMAL, PriorityConverter.toSeverity("Normal"));
        assertEquals(SeverityLevel.MINOR, PriorityConverter.toSeverity("minor"));
    }

}
