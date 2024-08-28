package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.api.MyService;
import com.ericsson.cifwk.taf.api.MySingleService;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 10/05/2016
 */
public class ServiceRegistryTafServiceInitializerTest {

    private ServiceRegistry.TafServiceInitializer unit;

    @Before
    public void setUp() {
        unit = new ServiceRegistry.TafServiceInitializer();
        unit = spy(unit);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionIfNoServiceFound() {
        unit.findAllServices(MyLonelyInterface.class);
    }

    @Test
    public void shouldFindAllServices() {
        List<MyService> allServices = unit.findAllServices(MyService.class);
        assertThat(allServices, hasSize(2));
    }

    @Test
    public void shouldRetrieveAllServiceInstancesOnlyOnce() {
        List<MyService> allServices = unit.getAllServiceInstances(MyService.class);
        assertThat(allServices, hasSize(2));
        verify(unit, times(1)).findAllServices(MyService.class);

        allServices = unit.getAllServiceInstances(MyService.class);
        assertThat(allServices, hasSize(2));
        verify(unit, times(1)).findAllServices(MyService.class);
    }

    @Test
    public void shouldGetSingleServiceInstanceOnlyOnce() {
        MySingleService singleServiceInstance = unit.getUniqueServiceInstance(MySingleService.class);
        assertNotNull(singleServiceInstance);
        verify(unit, times(1)).findAllServices(MySingleService.class);

        unit.getUniqueServiceInstance(MySingleService.class);
        verify(unit, times(1)).findAllServices(MySingleService.class);
        verify(unit, never()).handleMultipleBindings(anySet(), any(Class.class));
    }

    @Test(expected = MultipleServiceBindingsException.class)
    public void shouldThrowExceptionOnMultipleImplementationsOfUniqueService() {
        unit.getUniqueServiceInstance(MyService.class);
    }

    private static interface MyLonelyInterface {
    }

}

