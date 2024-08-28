package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.tms.TmsClient;
import com.ericsson.cifwk.taf.tms.dto.ReferenceDataItem;
import com.ericsson.cifwk.taf.tms.dto.RequirementInfo;
import com.ericsson.cifwk.taf.tms.dto.TestCaseInfo;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import ru.yandex.qatools.allure.events.TestCaseStartedEvent;
import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;
import ru.yandex.qatools.allure.model.Label;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;

import static com.ericsson.cifwk.taf.TafAllureModelUtils.createAutomatedExecutionTypeLabel;
import static com.ericsson.cifwk.taf.TafAllureModelUtils.createGroupLabel;
import static com.ericsson.cifwk.taf.TestExecutionHelper.getCurrentSuiteName;
import static com.ericsson.cifwk.taf.TestExecutionHelper.getCurrentVUsers;
import static com.ericsson.cifwk.taf.tms.PriorityConverter.toSeverity;
import static java.lang.String.format;
import static ru.yandex.qatools.allure.config.AllureModelUtils.createFeatureLabel;
import static ru.yandex.qatools.allure.config.AllureModelUtils.createHostLabel;
import static ru.yandex.qatools.allure.config.AllureModelUtils.createSeverityLabel;
import static ru.yandex.qatools.allure.config.AllureModelUtils.createStoryLabel;
import static ru.yandex.qatools.allure.config.AllureModelUtils.createTestLabel;
import static ru.yandex.qatools.allure.config.AllureModelUtils.createThreadLabel;

public class TestCaseEnricher {

    protected static final String SPRINT_LABEL = "sprint";

    private final TmsClient tmsClient;

    public TestCaseEnricher(TmsClient tmsClient) {
        this.tmsClient = tmsClient;
    }

    public void enrich(TestCaseBean bean, TestCaseStartedEvent event) {
        withExecutorInfo(event);

        String testId = bean.getTestId();

        // fetching test details from TMS
        TestCaseInfo testCase = tmsClient.getTestCase(testId);

        // could not fetch from TMS - falling back to default values
        if (testCase == null) {
            String title = bean.getTitle();
            event.setTitle(StringUtils.isEmpty(title) ? testId : title);
            event.setName(testId);
            return;
        }

        // populating event
        String title = testCase.getTitle();
        title = StringUtils.isEmpty(title) ? testId : title;
        title = ParametersManager.getParametrizedName(title, bean.getParameters().values().toArray());
        event.setTitle(title);
        if (testCase.getDescription() != null) {
            event.setDescription(new Description().withValue(testCase.getDescription()).withType(DescriptionType.TEXT));
        }
        event.setName(testId);

        // collecting user stories and unique sprints
        List<RequirementInfo> stories = testCase.getRequirements();
        Set<String> sprints = Sets.newHashSet();
        for (RequirementInfo story : stories) {
            sprints.add(story.getDeliveredIn());
            event.withLabels(createStoryLabel(format("%s: %s", story.getExternalId(), story.getSummary())));
        }

        // tagging test with every sprint
        for (String sprint : sprints) {
            event.withLabels(new Label().withName(SPRINT_LABEL).withValue(sprint));
        }

        List<ReferenceDataItem> groups = testCase.getGroups();
        for (ReferenceDataItem group : groups) {
            event.withLabels(createGroupLabel(group.getTitle()));
        }

        List<ReferenceDataItem> technicalComponents = testCase.getTechnicalComponents();
        for (ReferenceDataItem technicalComponent : technicalComponents) {
            event.withLabels(createFeatureLabel(technicalComponent.getTitle()));
        }

        // standard labels
        Label severityLabel = createSeverityLabel(toSeverity(testCase.getPriority().getTitle()));
        Label executionTypeLabel = createAutomatedExecutionTypeLabel();
        event.withLabels(severityLabel, executionTypeLabel);

        // optional labels
        Label testLabel = createTestLabel(testId);
        event.withLabels(testLabel);
    }

    /**
     * Add information about host and thread to specified test case started event
     *
     * @param event given event to enrich
     * @return updated event
     */
    public void withExecutorInfo(TestCaseStartedEvent event) {

        // host label
        Label hostLabel = createHostLabel("default");
        try {
            hostLabel.setValue(InetAddress.getLocalHost().getHostName());
        } catch (Exception ignored) { //NOSONAR
        }

        // thread label
        Label threadLabel = createThreadLabel(format("%s.%s", getCurrentSuiteName(), getCurrentVUsers()));

        event.withLabels(hostLabel, threadLabel);
    }

}
