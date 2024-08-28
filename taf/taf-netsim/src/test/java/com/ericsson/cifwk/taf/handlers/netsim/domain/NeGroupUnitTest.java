package com.ericsson.cifwk.taf.handlers.netsim.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommand;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandExecutor;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimSession;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import org.junit.Assert;
import org.junit.Test;

public class NeGroupUnitTest {

    @Test
    public void testBasicCollectionOperations() {
        NetworkElement ne1 = mock(NetworkElement.class);
        NetworkElement ne2 = mock(NetworkElement.class);
        NetworkElement ne3 = mock(NetworkElement.class);
        NetworkElement ne4 = mock(NetworkElement.class);

        NeGroup neGroup1 = new NeGroup(ne1);
        NeGroup neGroup2 = new NeGroup(ne3, ne4);
        Assert.assertFalse(neGroup1.isEmpty());
        Assert.assertFalse(neGroup2.isEmpty());

        neGroup1.add(ne2);
        Assert.assertTrue(neGroup1.contains(ne1) && neGroup1.contains(ne2));
        Assert.assertEquals(2, neGroup1.size());

        neGroup1.addAll(neGroup2);
        Assert.assertTrue(neGroup1.containsAll(neGroup2));
        Assert.assertTrue(neGroup1.contains(ne3) && neGroup1.contains(ne4));
        Assert.assertEquals(4, neGroup1.size());

        neGroup1.removeAll(neGroup2);
        Assert.assertTrue(neGroup1.contains(ne1) && neGroup1.contains(ne2));
        Assert.assertEquals(2, neGroup1.size());

        neGroup1.remove(ne1);
        Assert.assertTrue(neGroup1.contains(ne2) && !neGroup1.contains(ne1));
        Assert.assertEquals(1, neGroup1.size());

        neGroup1.retainAll(neGroup2);
        Assert.assertTrue(neGroup1.isEmpty());
    }

    @Test
    public void testConversionAndIteration() {
        NetworkElement ne1 = mock(NetworkElement.class);
        NetworkElement ne2 = mock(NetworkElement.class);

        NeGroup neGroup1 = new NeGroup(ne1, ne2);
        Iterator<NetworkElement> iterator = neGroup1.iterator();
        Assert.assertEquals(ne1, iterator.next());
        Assert.assertEquals(ne2, iterator.next());
        Assert.assertFalse(iterator.hasNext());

        Object[] array = neGroup1.toArray();
        Assert.assertEquals(ne1, array[0]);
        Assert.assertEquals(ne2, array[1]);

        array = neGroup1.toArray(new NetworkElement[0]);
        Assert.assertEquals(ne1, array[0]);
        Assert.assertEquals(ne2, array[1]);

        neGroup1.clear();
        Assert.assertTrue(neGroup1.isEmpty());
    }

    @Test
    public void shouldCloseSession_happyPath() {
        NetSimSession session = mock(NetSimSession.class);
        NeGroup unit = getStubbedUnit(session);
        doReturn(mock(NetSimResult.class)).when(unit).
                runWithAdditionalCommands(
                        any(NetSimCommand[].class),
                        anyListOf(NetSimCommand.class),
                        any(NetSimCommandExecutor.class));
        unit.exec(NetSimCommands.ldap());

        verify(session).close();
    }

    @Test(expected = RuntimeException.class)
    public void shouldCloseSession_onException() {
        NetSimSession session = mock(NetSimSession.class);
        NeGroup unit = getStubbedUnit(session);
        doThrow(RuntimeException.class).when(unit).
                runWithAdditionalCommands(
                        any(NetSimCommand[].class),
                        anyListOf(NetSimCommand.class),
                        any(NetSimCommandExecutor.class));
        unit.exec(NetSimCommands.ldap());

        verify(session).close();
    }

    private NeGroup getStubbedUnit(NetSimSession session) {
        NetworkElement ne1 = mock(NetworkElement.class);
        Simulation simulation1 = mock(Simulation.class);
        NetSimContext context = mock(NetSimContext.class);

        when(ne1.getSimulation()).thenReturn(simulation1);
        when(simulation1.getContext()).thenReturn(context);
        when(context.openSession()).thenReturn(session);

        List<NetworkElement> nes = Arrays.asList(ne1);
        NeGroup unit = new NeGroup(nes);

        return spy(unit);
    }
}
