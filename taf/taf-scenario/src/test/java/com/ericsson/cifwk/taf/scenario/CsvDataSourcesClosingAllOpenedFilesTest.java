package com.ericsson.cifwk.taf.scenario;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.taf.datasource.CsvMapReaderWrapper.getClosed;
import static com.ericsson.cifwk.taf.datasource.CsvMapReaderWrapper.getOpened;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class CsvDataSourcesClosingAllOpenedFilesTest {

    private static final int V_USERS = 10;

    @Test
    public void testFileClosing() {
        TestScenario scenario = TestScenarios.scenario().addFlow(
                flow("dataSourceTest")
                        .withVusers(V_USERS)
                        .withDuration(2, TimeUnit.SECONDS)
                        .withDataSources(dataSource("dataSources"))
                        .addTestStep(annotatedMethod(this, "testStep"))
        ).build();
        runner().build().start(scenario);

        assertThat(getOpened()).isEqualTo(getClosed());
    }


    @TestStep(id = "testStep")
    public synchronized void testStep(@Input(value = "recordId") String recordId) {
        /*
        * Opened and closed file counts could be different (e.g. if all users opened the file and none of them closed the file yet).
        * In this case allowable error is until predefined vUser count
        * */
        assertThat(getOpened()).isLessThanOrEqualTo(getClosed() + V_USERS);
    }

}
