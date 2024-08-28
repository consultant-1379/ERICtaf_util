package com.ericsson.cifwk.taf.allure.manual;

import org.junit.Test;
import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DescriptionBuilderTest {

    private DescriptionBuilder builder = new DescriptionBuilder("Header");

    @Test
    public void build() {

        // creating description
        Description description = builder
                .withHeader("Sub-header")
                .withField("Field1", "value1")
                .withSeparator()
                .withLongText("Long long text")
                .withParagraph("Title", "Paragraph")
                .withRequirementLinks(asList("CIP-123"))
                .withTestCampaignLink(456)
                .build();
        String markDown = description.getValue();

        // asserting description
        assertEquals(DescriptionType.MARKDOWN, description.getType());
        assertThat(markDown, containsString("## Header\n"));
        assertThat(markDown, containsString("### Sub-header\n"));
        assertThat(markDown, containsString("Field1: **value1**"));
        assertThat(markDown, containsString("___\n"));
        assertThat(markDown, containsString("> Long long text\n"));
        assertThat(markDown, containsString("### Title\n> Paragraph\n"));
        assertThat(markDown, containsString("[CIP-123]("));
        assertThat(markDown, containsString("CIP-123)"));
        assertThat(markDown, containsString("Test Campaign: [456]("));
        assertThat(markDown, containsString("456) \n"));
    }

    @Test
    public void buildEmpty() {
        builder = new DescriptionBuilder();
        Description description = builder
                .withField("Field1", "")
                .withLongText("")
                .withParagraph("Title1", "")
                .withField("Field2", null)
                .withLongText(null)
                .withParagraph("Title2", null)
                .withField("Field3", "null")
                .withLongText("null")
                .withParagraph("Title3", "null")
                .build();
        assertEquals("", description.getValue());
    }

    @Test
    public void getCitation() {
        String citation = builder.getCitation("line1\r\nline2\n\rline3\n");
        assertEquals("> line1\n> line2\n> line3\n> \n", citation);
    }
}