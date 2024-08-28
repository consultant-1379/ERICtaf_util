package com.ericsson.cifwk.taf.allure.manual;

import com.ericsson.cifwk.taf.tms.TafConfig;
import com.ericsson.cifwk.taf.tms.dto.ReferenceDataItem;
import com.google.common.annotations.VisibleForTesting;
import ru.yandex.qatools.allure.config.AllureConfig;
import ru.yandex.qatools.allure.model.Description;

import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static org.jvnet.jaxb2_commons.lang.StringUtils.isEmpty;
import static ru.yandex.qatools.allure.model.DescriptionType.MARKDOWN;

class DescriptionBuilder {

    private final StringBuilder sb = new StringBuilder();

    private final TafConfig tafConfig = TafConfig.newInstance();

    private final AllureConfig allureConfig = AllureConfig.newInstance();

    public DescriptionBuilder() {
        // do nothing
    }

    public DescriptionBuilder(String header) {
        sb.append("## ").append(header).append("\n");
    }

    public DescriptionBuilder withHeader(String header) {
        sb.append("### ").append(header).append("\n");
        return this;
    }

    public DescriptionBuilder withSeparator() {
        sb.append("___\n");
        return this;
    }

    public DescriptionBuilder withRequirementLinks(Collection<String> requirementIds) {
        if (!requirementIds.isEmpty()) {
            String linkPattern = allureConfig.getIssueTrackerPattern();
            sb.append("Requirements: ");
            for (String requirementId : requirementIds) {
                String link = format(linkPattern, requirementId);
                withLink(requirementId, link);
            }
            sb.append("\n");
        }
        return this;
    }

    public DescriptionBuilder withTestCampaignLink(long testCampaignId) {
        String link = tafConfig.getWebUrlForTestCampaign(testCampaignId);
        sb.append("Test Campaign: ");
        withLink(Long.toString(testCampaignId), link);
        sb.append("\n");
        return this;
    }

    private DescriptionBuilder withLink(String label, String link) {
        sb.append("[").append(label).append("](").append(link).append(") ");
        return this;
    }

    public DescriptionBuilder withLongText(String text) {
        if (notEmpty(text)) {
            sb.append(getCitation(text)).append("\n");
        }
        return this;
    }

    @VisibleForTesting
    protected String getCitation(String text) {
        return "> " + text.replaceAll("\r\n", "\n").replaceAll("\n\r", "\n").replaceAll("\n", "\n> ") + "\n";
    }

    public DescriptionBuilder withField(String fieldName, String fieldValue) {
        if (notEmpty(fieldValue)) {
            withValue(fieldName, "**" + fieldValue + "**");
        }
        return this;
    }

    public DescriptionBuilder withFields(String fieldName, List<ReferenceDataItem> fieldValues) {
        for (ReferenceDataItem fieldValue : fieldValues) {
            withValue(fieldName, "**" + fieldValue.getTitle() + "**");
        }
        return this;
    }

    public DescriptionBuilder withParagraph(String title, String value) {
        if (notEmpty(value)) {
            withHeader(title);
            sb.append(getCitation(value)).append("\n");
        }
        return this;
    }

    private DescriptionBuilder withValue(String fieldName, String fieldValue) {
        sb.append(format("%s: %s%n", fieldName, fieldValue));
        return this;
    }

    public Description build() {
        return new Description().withType(MARKDOWN).withValue(sb.toString());
    }

    private boolean notEmpty(String value) {
        return !isEmpty(value) && !"null" .equals(value);
    }

}
