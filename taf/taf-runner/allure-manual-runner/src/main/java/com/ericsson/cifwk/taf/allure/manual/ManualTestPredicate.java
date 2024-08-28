package com.ericsson.cifwk.taf.allure.manual;

import com.ericsson.cifwk.taf.tms.dto.ReferenceDataItem;
import com.ericsson.cifwk.taf.tms.dto.TestCampaignItemInfo;
import com.google.common.base.Predicate;

/**
 * Created by Mihails Volkovs <mihails.volkovs@ericsson.com>
 * 20/07/2015
 */
class ManualTestPredicate implements Predicate<TestCampaignItemInfo> {

    @Override
    public boolean apply(TestCampaignItemInfo item) {
        ReferenceDataItem executionType = item.getTestCase().getExecutionType();
        return executionType == null || "Manual".equals(executionType.getTitle());
    }

}
