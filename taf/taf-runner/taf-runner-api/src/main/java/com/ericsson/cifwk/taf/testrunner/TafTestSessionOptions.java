package com.ericsson.cifwk.taf.testrunner;

import com.ericsson.cifwk.meta.API;
import com.google.common.collect.Lists;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * Test session options container.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 12/12/2016
 */
@API(Stable)
public class TafTestSessionOptions {

    private List<String> testGroupDefinitions;
    private List<Class> testClasses;
    private List<Object> testEventListeners;
    private List<String> testTags;

    /**
     * Sets the test group definitions. The values depend on the particular test runner.
     * For example, in case of TestNG this is the list of suite XML files.
     * @param testGroupDefinitions test groups to run.
     */
    public void setTestGroupDefinitions(List<String> testGroupDefinitions) {
        this.testGroupDefinitions = testGroupDefinitions;
    }

    /**
     * Sets the test classes to run. If defined, only these tests will be run, ignoring test groups defined
     * via {@link #setTestGroupDefinitions(List)}.
     * @param testClasses test classes to run
     */
    public void setTestClasses(List<Class> testClasses) {
        this.testClasses = testClasses;
    }

    /**
     * Sets the test events listeners.
     * Listeners can be TAF Test API event subscribers or a particular test engine listeners (TestNG listener, for instance).
     * @param testEventListeners
     */
    public void setTestEventListeners(List<Object> testEventListeners) {
        this.testEventListeners = testEventListeners;
    }

    /**
     * Sets the test tags.
     * Test tags limit the test session to execute only tests tagged with the defined tags.
     * In TestNG this is equivalent to setting test groups.
     * @param testTags
     */
    public void setTestTags(List<String> testTags) {
        this.testTags = testTags;
    }

    public List<String> getTestGroupDefinitions() {
        return (testGroupDefinitions != null) ? testGroupDefinitions : Lists.<String>newArrayList();
    }

    public List<Class> getTestClasses() {
        return (testClasses != null) ? testClasses : Lists.<Class>newArrayList();
    }

    public List<Object> getTestEventListeners() {
        return (testEventListeners != null) ? testEventListeners : Lists.newArrayList();
    }

    public List<String> getTestTags() {
        return (testTags != null) ? testTags : Lists.<String>newArrayList();
    }
}
