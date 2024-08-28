package com.ericsson.cifwk.taf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         10/09/2015
 */

@RunWith(MockitoJUnitRunner.class)
public class TestSuiteStorageTest {

    private TestSuiteStorage testSuiteStorage;

    @Mock
    private TestSuiteResult parentSuite;

    @Mock
    private TestSuiteResult childSuite;

    @Before
    public void setUp() throws Exception {
        when(parentSuite.getName()).thenReturn("parentTest");
        when(childSuite.getName()).thenReturn("childTest");

        testSuiteStorage = new TestSuiteStorage();
    }

    @Test
    public void testGetCurrentSuite() throws Exception {
        testSuiteStorage.setCurrentSuite(parentSuite);
        assertThat(testSuiteStorage.getCurrentSuite().getName(), is("parentTest"));

        testSuiteStorage.setCurrentSuite(childSuite);
        assertThat(testSuiteStorage.getCurrentSuite().getName(), is("childTest"));
    }

    @Test
    public void testGetCurrentSuiteHolder() throws Exception {
        testSuiteStorage.setCurrentSuite(parentSuite);
        assertThat(testSuiteStorage.getCurrentSuiteHolder().getSuite().getName(), is("parentTest"));
    }

    @Test
    public void testRemoveCurrentSuite() throws Exception {
        testSuiteStorage.setCurrentSuite(parentSuite);
        testSuiteStorage.setCurrentSuite(childSuite);
        assertThat(testSuiteStorage.getCurrentSuite().getName(), is("childTest"));

        testSuiteStorage.removeCurrentSuite();
        assertThat(testSuiteStorage.getCurrentSuite().getName(), is("parentTest"));

        testSuiteStorage.removeCurrentSuite();
        assertThat(testSuiteStorage.getCurrentSuite(), nullValue());
    }
}