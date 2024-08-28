package com.ericsson.cifwk.taf.testapi;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.xml.XmlSuite;

import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 09/07/2016
 */
@API(Internal)
public class TestSuite implements TestGroup {

    private final ISuite suite;

    public TestSuite(ISuite suite) {
        this.suite = suite;
    }

    @Override
    public String getId() {
        return suite.getName();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getDefinitionParameter(String parameterName) {
        XmlSuite xmlSuite = getXmlSuite();
        if (xmlSuite != null) {
            return xmlSuite.getParameter(parameterName);
        }
        return null;
    }

    @Override
    public Optional<String> getDefinitionFileName() {
        XmlSuite xmlSuite = getXmlSuite();
        if (xmlSuite != null) {
            return Optional.of(xmlSuite.getFileName());
        }
        return Optional.absent();
    }

    private XmlSuite getXmlSuite() {
        return suite.getXmlSuite();
    }

    public ISuite getSuite() {
        return suite;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String attributeName) {
        return (T) suite.getAttribute(attributeName);
    }

    @Override
    public <T> void setAttribute(String attributeName, T attributeValue) {
        suite.setAttribute(attributeName, attributeValue);
    }

    @Override
    public void removeAttribute(String attributeName) {
        suite.removeAttribute(attributeName);
    }

    @Override
    public List<TestGroupResult> getResults() {
        Map<String, ISuiteResult> resultMap = suite.getResults();
        Iterable<TestGroupResult> results = Iterables.transform(resultMap.values(), new Function<ISuiteResult, TestGroupResult>() {
            @Override
            public TestGroupResult apply(ISuiteResult iSuiteResult) {
                return new TestGroupResultImpl(iSuiteResult);
            }
        });
        return Lists.newArrayList(results);
    }

}
