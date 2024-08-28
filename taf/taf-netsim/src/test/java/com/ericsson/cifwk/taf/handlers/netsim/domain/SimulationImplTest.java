package com.ericsson.cifwk.taf.handlers.netsim.domain;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.notNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.cifwk.taf.handlers.netsim.CommandOutput;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommand;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;

@RunWith(MockitoJUnitRunner.class)
public class SimulationImplTest {

    CommandOutput simnesOutput = new CommandOutput("simnesOutput");
    CommandOutput correctStartedOutput = new CommandOutput("MyNE  first detail  second detail");
    CommandOutput failingStartedOutput = new CommandOutput("MyNE first detail second detail");
    CommandOutput[] correctCommandOutputs = {simnesOutput, correctStartedOutput};
    CommandOutput[] failingCommandOutputs = {simnesOutput, failingStartedOutput};

    @Mock
    SimulationImpl simulation;
    @Mock
    NetSimResultMapper netSimResultMapper;
    @Mock
    NetSimResult netSimResult;
    @Mock
    NetworkElement networkElement;

    @Before
    public void setUp() throws Exception {
        simulation.mapper = netSimResultMapper;
        when(simulation.exec(any(NetSimCommand.class), any(NetSimCommand.class))).thenReturn(netSimResult);
        when(simulation.mapper.parse(any(CommandOutput.class))).thenReturn(new ArrayList<>(Arrays.asList(networkElement)));
        when(networkElement.getName()).thenReturn("MyNE");
        when(networkElement.getSimulationName()).thenThrow(new RuntimeException("This should get caught"));

        when(simulation.getStartedNEs()).thenCallRealMethod();
        when(netSimResultMapper.mapToNetworkElements(netSimResult)).thenCallRealMethod();
        when(netSimResultMapper.mapToNetworkElements(any(NetSimResult.class))).thenCallRealMethod();
        when(netSimResultMapper.populateListOfStartedNEs(any(CommandOutput.class), any(List.class))).thenCallRealMethod();
    }

    @Test
    public void shouldCatchAndLogException() {
        when(netSimResult.getOutput()).thenReturn(correctCommandOutputs);
        simulation.getStartedNEs();

        verify(simulation.mapper, times(1)).logFailedNEData(any(List.class), notNull(Exception.class));
        verify(simulation.mapper, times(0)).logFailedNEData(any(List.class), isNull(Exception.class));
    }

    @Test
    public void shouldLogFailedDataWithNoException() {
        when(netSimResult.getOutput()).thenReturn(failingCommandOutputs);
        simulation.getStartedNEs();

        verify(simulation.mapper, times(0)).logFailedNEData(any(List.class), notNull(Exception.class));
        verify(simulation.mapper, times(1)).logFailedNEData(any(List.class), isNull(Exception.class));
    }

}
