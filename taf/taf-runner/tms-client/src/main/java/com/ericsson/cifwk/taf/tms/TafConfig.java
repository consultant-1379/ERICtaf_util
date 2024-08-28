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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import ru.yandex.qatools.properties.PropertyLoader;
import ru.yandex.qatools.properties.annotations.Property;
import ru.yandex.qatools.properties.annotations.Resource;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static java.lang.String.format;

@SuppressWarnings("unused")
@Resource.Classpath("taf.properties")
public class TafConfig {
    public static final String NULL = "null";

    @Property("taf.tms.api.test-case")
    private String testCaseApiUrlPattern = "https://taftm.seli.wh.rnd.internal.ericsson.com/tm-server/api/test-cases/%s?view=detailed";

    @Property("taf.tms.api.test-campaign")
    private String testCampaignApiUrlPattern;

    @Property("taf.tms.web.test-campaign")
    private String testCampaignWebUrlPattern;

    @Property("taf.tms.api.test-plan")
    private String testPlanApiUrlPattern = "https://taftm.seli.wh.rnd.internal.ericsson.com/tm-server/api/test-campaigns/%s?view=detailedItems";

    @Property("taf.tms.web.test-plan")
    private String testPlanWebUrlPattern = "https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTestCampaign/%s";

    public TafConfig() {
        PropertyLoader.populate(this);
    }

    public static TafConfig newInstance() {
        return new TafConfig();
    }

    public Boolean isEnabled() {
        return !(isCaseApiDisabled() &&
                (isTestCampaignApiDisabled() || isTestPlanApiDisabled()));
    }

    private Boolean isCaseApiDisabled() {
        return NULL.equals(testCaseApiUrlPattern);
    }

    private Boolean isTestCampaignApiDisabled() {
        return NULL.equals(testCampaignApiUrlPattern) && NULL.equals(testCampaignWebUrlPattern);
    }

    private Boolean isTestPlanApiDisabled() {
        return NULL.equals(testPlanApiUrlPattern) && NULL.equals(testPlanWebUrlPattern);
    }

    public String getApiUrlForTestCase(String testCaseId) {
        Preconditions.checkNotNull(testCaseId, "test case id is mandatory");
        return format(testCaseApiUrlPattern, encodeQueryParam(testCaseId));
    }

    public String getApiUrlForTestCampaign(long testCampaignId) {
        return chooseApiUrl(testCampaignApiUrlPattern, testPlanApiUrlPattern, testCampaignId);
    }

    public String getWebUrlForTestCampaign(long testCampaignId) {
        return chooseApiUrl(testCampaignWebUrlPattern, testPlanWebUrlPattern, testCampaignId);
    }

    private String chooseApiUrl(String newApiUrl, String oldApiUrl, long testCampaignId) {
        if (!Strings.isNullOrEmpty(newApiUrl)) {
            return format(newApiUrl, testCampaignId);
        } else {
            return format(oldApiUrl, testCampaignId);
        }
    }

    private String encodeQueryParam(String testCaseId) {
        Preconditions.checkNotNull(testCaseId, "test case id is mandatory");
        try {
            String testCaseIdEncoded = URLEncoder.encode(testCaseId, "utf-8");
            // Encoding of space symbol differs depending on location (path or URL parameters)
            // please see http://stackoverflow.com/a/13684170/1307586 for details
            return testCaseIdEncoded.replace("+", "%20");
        } catch (UnsupportedEncodingException canNeverHappen) {
            throw new RuntimeException(canNeverHappen); // NOSONAR
        }
    }

}
