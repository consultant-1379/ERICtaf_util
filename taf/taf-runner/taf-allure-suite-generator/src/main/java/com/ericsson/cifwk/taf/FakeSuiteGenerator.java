package com.ericsson.cifwk.taf;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FakeSuiteGenerator {

    private final Configuration cfg;

    public FakeSuiteGenerator() {
        cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setClassForTemplateLoading(FakeSuiteGenerator.class, "/templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public String generateForMissingSuites(Collection<String> missingSuites, Collection<String> notRunSuites) throws IOException, TemplateException {
        Template template = cfg.getTemplate("missing-suites.xml.ftl");

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("start", Calendar.getInstance().getTimeInMillis());
        templateData.put("missingSuites", missingSuites);
        templateData.put("notRunSuites", notRunSuites);

        StringWriter outputWriter = new StringWriter();
        template.process(templateData, outputWriter);

        return outputWriter.toString();
    }

    public String generateForFailedExecution(Collection<String> notRunSuites) throws IOException, TemplateException {
        Template template = cfg.getTemplate("failed-execution.xml.ftl");

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("start", Calendar.getInstance().getTimeInMillis());
        templateData.put("notRunSuites", notRunSuites);

        StringWriter outputWriter = new StringWriter();
        template.process(templateData, outputWriter);

        return outputWriter.toString();
    }

    public String generateForPartialExecution(Collection<String> finishedSuites,
                                              Collection<String> notFinishedSuites,
                                              Collection<String> notRunSuites) throws IOException, TemplateException {
        Template template = cfg.getTemplate("partial-execution.xml.ftl");

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("start", Calendar.getInstance().getTimeInMillis());
        templateData.put("finishedSuites", finishedSuites);
        templateData.put("notFinishedSuites", notFinishedSuites);
        templateData.put("notRunSuites", notRunSuites);

        StringWriter outputWriter = new StringWriter();
        template.process(templateData, outputWriter);

        return outputWriter.toString();
    }
}
