<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:test-suite start="${start?long?c}" stop="${start?long?c}" version="1.4.16" xmlns:ns2="urn:model.allure.qatools.yandex.ru">
    <name>Missing suites</name>
    <title>Missing suites</title>
    <test-cases>
    <#list missingSuites as missingSuite>
        <test-case start="${start?long?c}" stop="${start?long?c}" status="broken">
            <name>${missingSuite}</name>
            <title>${missingSuite}</title>
            <labels>
                <label name="severity" value="blocker"/>
            </labels>
            <failure>
                <message>Suite "${missingSuite}" is not found</message>
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
                <message>Suite "${notRunSuite}" is skipped due to missing suites error</message>
            </failure>
        </test-case>
    </#list>
    </test-cases>
</ns2:test-suite>
