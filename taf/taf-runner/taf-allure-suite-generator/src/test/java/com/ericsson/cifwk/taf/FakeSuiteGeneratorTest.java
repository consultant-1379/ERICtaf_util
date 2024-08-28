package com.ericsson.cifwk.taf;

import com.google.common.io.Resources;
import freemarker.template.TemplateException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FakeSuiteGeneratorTest {

    private FakeSuiteGenerator fakeSuiteGenerator;
    private List<String> missingSuites;
    private List<String> skippedSuites;
    private String resultFailedExecution;
    private String resultMissingSuites;
    private String resultPartialExecutuon;

    @Before
    public void setUp() throws IOException {
        URL resource = this.getClass().getResource("/result-failed-execution.xml");
        resultFailedExecution = Resources.toString(resource, Charset.defaultCharset());

        resource = this.getClass().getResource("/result-missing-suites.xml");
        resultMissingSuites = Resources.toString(resource, Charset.defaultCharset());


        resource = this.getClass().getResource("/result-partial-execution.xml");
        resultPartialExecutuon = Resources.toString(resource, Charset.defaultCharset());

        fakeSuiteGenerator = new FakeSuiteGenerator();

        missingSuites = Arrays.asList("missing-suite1.xml", "missing-suite2.xml");
        skippedSuites = Arrays.asList("skipped-suite1.xml", "skipped-suite2.xml");
    }

    @Test
    public void testGenerateForMissingSuites() throws IOException, TemplateException {
        String generate = fakeSuiteGenerator.generateForMissingSuites(missingSuites, skippedSuites)
                .replaceAll("start=\"\\d+\"", "start=\"1449652085852\"")
                .replaceAll("stop=\"\\d+\"", "stop=\"1449652085852\"");

        assertThat(generate, equalTo(resultMissingSuites));
    }

    @Test
    public void testGenerateForFailedExecution() throws IOException, TemplateException {
        String generate = fakeSuiteGenerator.generateForFailedExecution(skippedSuites)
                .replaceAll("start=\"\\d+\"", "start=\"1449652085852\"")
                .replaceAll("stop=\"\\d+\"", "stop=\"1449652085852\"");

        assertThat(generate, is(resultFailedExecution));
    }

    @Test
    public void testGenerateForPartialExecution() throws IOException, TemplateException {
        String generate = fakeSuiteGenerator.generateForPartialExecution(
                Arrays.asList("finished-suite-123.xml"),
                Arrays.asList("failed-suite-123.xml"),
                Arrays.asList("skipped-suite-123.xml"))
                .replaceAll("start=\"\\d+\"", "start=\"1449652085852\"")
                .replaceAll("stop=\"\\d+\"", "stop=\"1449652085852\"");

        assertThat(generate, is(resultPartialExecutuon));
    }
}
