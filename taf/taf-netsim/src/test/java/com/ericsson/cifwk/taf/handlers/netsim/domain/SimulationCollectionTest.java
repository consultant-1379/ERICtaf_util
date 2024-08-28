package com.ericsson.cifwk.taf.handlers.netsim.domain;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimSession;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class SimulationCollectionTest {

    @Mock Simulation simulation1;
    @Mock Simulation simulation2;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) NetSimContext context1;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) NetSimContext context2;

    @Mock NetSimSession session1;
    @Mock NetSimSession session2;

    @Mock NetworkElement ne11;
    @Mock NetworkElement ne21;
    @Mock NetworkElement ne22;

    @Mock NetworkMap networkMap;

    @Before
    public void setUp() throws Exception {
        when(simulation1.getName()).thenReturn("sim1");
        when(simulation2.getName()).thenReturn("sim2");

        when(simulation1.getAllNEs(session1)).thenReturn(Lists.newArrayList(ne11));
        when(simulation1.getContext()).thenReturn(context1);
        when(context1.openSession()).thenReturn(session1);

        when(simulation2.getAllNEs(session2)).thenReturn(Lists.newArrayList(ne21, ne22));
        when(simulation2.getContext()).thenReturn(context2);
        when(context2.openSession()).thenReturn(session2);

        when(context1.getNetworkMap().getNetworkElements()).thenReturn(Lists.newArrayList(ne11));
        when(context2.getNetworkMap().getNetworkElements()).thenReturn(Lists.newArrayList(ne21, ne22));
    }

    @Test
    public void getAllNEs() {
        SimulationGroup unit = new SimulationGroup(new Simulation[]{simulation1, simulation2});
        List<NetworkElement> allNEs = unit.getAllNEs();
        Assert.assertEquals(3, allNEs.size());
        Assert.assertTrue(allNEs.contains(ne11) && allNEs.contains(ne21) && allNEs.contains(ne22));
    }

    @Test
    public void exec() {
        NetSimResult sim1Result = new NetSimResult("aaaaa");
        when(simulation1.exec(any(List.class))).thenReturn(sim1Result);

        NetSimResult sim2Result = new NetSimResult("bbbbb");
        when(simulation2.exec(any(List.class))).thenReturn(sim2Result);

        SimulationGroup unit = new SimulationGroup(new Simulation[]{simulation1, simulation2});

        Map<Simulation, NetSimResult> results =
                unit.exec(NetSimCommands.open("sim1"), NetSimCommands.open("sim2"));
        Assert.assertEquals(2, results.size());
        Assert.assertEquals(sim1Result, results.get(simulation1));
        Assert.assertEquals(sim2Result, results.get(simulation2));
    }

}
