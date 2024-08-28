package com.test.ericsson.cifwk.taf.guice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.cifwk.taf.configuration.TafHost;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.guice.TafGuiceModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

public class InjectableHostTest {

    public static final String HOST_1_NAME = "host-1";

    public static final String HOST_2_NAME = "host-2";

    public static final String HOST_1_TYPE = "netsim";

    public static final String HOST_1_GROUP = "group-1";

    private Injector injector;

    private InjectionTarget target;

    @Before
    public void setUp() {
        injector = Guice.createInjector(new TafGuiceModule());
        target = injector.getInstance(InjectionTarget.class);
    }

    @Test
    public void getHostByName() {
        Host host = target.getHost1ByName();
        assertThat(host).isNotNull();
        assertThat(host.getHostname()).isEqualTo(HOST_1_NAME);

        host = target.getHost2ByName();
        assertThat(host).isNotNull();
        assertThat(host.getHostname()).isEqualTo(HOST_2_NAME);
    }

    @Test
    public void getHostByType() {
        Host host = target.getHostByType();
        assertThat(host).isNotNull();
        assertThat(host.getType()).isEqualTo(HostType.NETSIM);
    }

    @Test
    public void getHostByGroup() {
        Host host = target.getHostByGroup();
        assertThat(host).isNotNull();
        assertThat(host.getGroup()).isEqualTo(HOST_1_GROUP);
    }

    @Test
    public void getHostsByNonExistingGroup() {
        assertThat(target.getNoneHosts()).isEmpty();
    }

    @Test
    public void getHostByNonExistingGroup() {
        try {
            injector.getInstance(NonExistingGroup.class);
            fail();
        } catch (ProvisionException e) {
            assertThat(e).hasMessageContaining("Unable to inject Host in Class com.test.ericsson.cifwk.taf.guice"
                    + ".InjectableHostTest.NonExistingGroup (field 'host'):");
            assertThat(e).hasMessageContaining("No hosts found by given search criteria (hostname: '', group: 'nonExistingGroup', type: '')");
            // all hosts are displayed
            assertThat(e).hasMessageContaining("host-1");
            assertThat(e).hasMessageContaining("group-1");
            assertThat(e).hasMessageContaining("netsim");
        }
    }

    @Test
    public void moreThanOneValueIsPresented() {
        try {
            injector.getInstance(MoreThanOneValueIsPresent.class);
            fail();
        } catch (ProvisionException e) {
            assertThat(e).hasMessageContaining("Unable to inject Host in Class com.test.ericsson.cifwk.taf.guice"
                    + ".InjectableHostTest.MoreThanOneValueIsPresent (field 'host'):");
            assertThat(e).hasMessageContaining("More than one hosts found by given search criteria (hostname: '', group: 'my-hosts', type: ''):");
            // only found hosts are displayed
            assertThat(e).hasMessageContaining("host-3");
            assertThat(e).hasMessageContaining("host-4");
            assertThat(e).hasMessageContaining("Please use List, Set or Collection as field type to get all hosts by given search criteria (hostname: '', group: 'my-hosts', type: '')");
        }
    }

    @Test
    public void hostByAllCriterias() {
        Host host = target.getHostByAllAvailableCriterias();
        assertThat(host.getHostname()).isEqualTo("host-1");
        assertThat(host.getGroup()).isEqualTo("group-1");
        assertThat(host.getType().getName().toLowerCase()).isEqualTo("netsim");
    }

    @Test
    public void getAllHostsByGroup() {
        Collection<Host> hosts = target.getHostsByGroup();
        assertThat(hosts).isNotNull();
        assertThat(hosts).hasSize(1);
    }

    @Test
    public void getCombinedSearchArguments() {
        Collection<Host> hosts = target.getCombinedSearchArguments();
        assertThat(hosts).isNotNull();
        assertThat(hosts).hasSize(1);
    }

    @Test
    public void getAllHosts() {
        Collection<Host> hosts = target.getAllHosts();
        assertThat(hosts).isNotNull();
        assertThat(hosts).hasSize(8);
    }

    @Test
    public void onlyHostShouldBeAllowedAsGenericType() {
        try {
            injector.getInstance(WrongCollectionGenericType.class);
            fail();
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("Class com.test.ericsson.cifwk.taf.guice.InjectableHostTest"
                    + ".WrongCollectionGenericType (field 'hosts'): collection expected parameter is com.ericsson.cifwk.taf.data.Host type but was java.lang.String");
        }
    }

    @Test
    public void onlyCollectionInterfacesShouldBeAllowed() {
        try {
            injector.getInstance(WrongCollectionType.class);
            fail();
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("Unable to inject Host in Class com.test.ericsson.cifwk.taf.guice"
                    + ".InjectableHostTest.WrongCollectionType (field 'unexpectedListType'):");
            assertThat(e).hasMessageContaining("Field type java.util.LinkedList is not assignable to value of type ");
        }
    }

    public static class InjectionTarget {

        @TafHost(hostname = HOST_1_NAME)
        private Host host1ByName;

        @TafHost(hostname = HOST_2_NAME)
        private Host host2ByName;

        @TafHost(type = HOST_1_TYPE)
        private Host hostByType;

        @TafHost(group = HOST_1_GROUP)
        private Host hostByGroup;

        @TafHost(hostname = "host-1", group = "group-1", type = "netsim")
        private Host hostByAllAvailableCriterias;

        @TafHost(group = "group-1")
        private List<Host> hostsByGroup;

        @TafHost(group = "group-1", type = "netsim")
        private Set<Host> combinedSearchArguments;

        @TafHost
        private Collection<Host> allHosts;

        @TafHost(group = "non-existing-group")
        private List<Host> noneHosts;

        public Host getHost1ByName() {
            return host1ByName;
        }

        public Host getHost2ByName() {
            return host2ByName;
        }

        public Host getHostByType() {
            return hostByType;
        }

        public Host getHostByGroup() {
            return hostByGroup;
        }

        public Host getHostByAllAvailableCriterias() {
            return hostByAllAvailableCriterias;
        }

        public List<Host> getHostsByGroup() {
            return hostsByGroup;
        }

        public Set<Host> getCombinedSearchArguments() {
            return combinedSearchArguments;
        }

        public Collection<Host> getAllHosts() {
            return allHosts;
        }

        public List<Host> getNoneHosts() {
            return noneHosts;
        }
    }

    public static class NonExistingGroup {

        @TafHost(group = "nonExistingGroup")
        private Host host;

    }

    public static class MoreThanOneValueIsPresent {

        @TafHost(group = "my-hosts")
        private Host host;

    }

    public static class WrongCollectionGenericType {

        @TafHost
        private List<String> hosts;

    }

    public static class WrongCollectionType {

        @TafHost
        private LinkedList<Host> unexpectedListType;

    }

}
