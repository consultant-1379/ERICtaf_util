<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:test-suite start="${start?long?c}" stop="${start?long?c}" version="1.4.16" xmlns:ns2="urn:model.allure.qatools.yandex.ru">
    <name>Failed to complete execution</name>
    <title>Failed to complete execution</title>
    <test-cases>
    <#list finishedSuites as finishedSuite>
        <test-case start="${start?long?c}" stop="${start?long?c}" status="broken">
            <name>${finishedSuite}</name>
            <title>${finishedSuite}</title>
            <labels>
                <label name="severity" value="blocker"/>
            </labels>
            <failure>
                <message>Suite "${finishedSuite}" finished but result is unknown.</message>
            </failure>
        </test-case>
    </#list>
    <#list notFinishedSuites as notFinishedSuite>
        <test-case start="${start?long?c}" stop="${start?long?c}" status="broken">
            <name>${notFinishedSuite}</name>
            <title>${notFinishedSuite}</title>
            <labels>
                <label name="severity" value="blocker"/>
            </labels>
            <failure>
                <message>Suite "${notFinishedSuite}" didn't finish due to failed execution.</message>
            </failure>
        </test-case>
    </#list>
    <#list notRunSuites as notRunSuite>
        <test-case start="${start?long?c}" stop="${start?long?c}" status="broken">
            <name>${notRunSuite}</name>
            <title>${notRunSuite}</title>
            <labels>
                <label name="severity" value="blocker"/>
            </labels>
            <failure>
                <message>Suite "${notRunSuite}" is skipped due to failed execution.</message>
            </failure>
        </test-case>
    </#list>
    </test-cases>
</ns2:test-suite>
