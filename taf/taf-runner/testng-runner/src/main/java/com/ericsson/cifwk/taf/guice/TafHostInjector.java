package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.configuration.TafDataHandler;
import com.ericsson.cifwk.taf.configuration.TafHost;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.network.EnmNetworkHost;
import com.ericsson.cifwk.taf.data.network.NetworkInterface;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.inject.MembersInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.getLocation;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.getTafHostAnnotation;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.isCollection;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.isList;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.isSetCollection;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.logMessageAndThrowException;
import static java.lang.String.format;

class TafHostInjector<T> implements MembersInjector<T> {

    private static final Logger LOG = LoggerFactory.getLogger(TafHostInjector.class);

    private static final String UNABLE_TO_INJECT = "Unable to inject Host in %s:%n";

    private static final String NO_HOSTS_FOUND = UNABLE_TO_INJECT +
            "No hosts found by given search criteria %s%n" +
            "Hosts available: %s";

    private static final String MULTIPLE_HOSTS_FOUND = UNABLE_TO_INJECT +
            "More than one hosts found by given search criteria %s:%n" +
            "%s%n" +
            "Please use List, Set or Collection as field type to get all hosts by given search criteria %s.";

    private static final String NOT_ASSIGNABLE = UNABLE_TO_INJECT +
            "Field type %s is not assignable to value of type %s";

    public static final String HOST_REPRESENTATION = "(hostname: '%s', group: '%s', type: '%s')";

    private final Field field;

    TafHostInjector(Field field) {
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    public void injectMembers(T instance) {
        try {
            TafHost annotation = getTafHostAnnotation(field);
            if (annotation != null) {
                inject(instance, annotation);
            }
        } catch (IllegalAccessException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void inject(T instance, TafHost annotation) throws IllegalAccessException {
        List<Host> found = TafHostFilter.getInstance().filter(annotation);
        Object value;
        if (isSetCollection(field)) {
            value = new HashSet<>(found);
        } else if (isList(field) || isCollection(field)) {
            value = found;
        } else {
            checkHost(annotation, found);
            value = found.iterator().next();
        }

        checkAssignment(field, value);
        field.set(instance, value);
    }

    private void checkHost(TafHost annotation, Collection<Host> filteredHosts) {
        if (filteredHosts.isEmpty()) {
            failInjectionWithEmptyValue(annotation);
        } else if (filteredHosts.size() > 1) {
            failInjectionWithMoreThanOneValue(annotation, filteredHosts);
        }
    }

    private void failInjectionWithMoreThanOneValue(TafHost annotation, Collection<Host> found) {
        String location = getLocation(field);
        String criteria = asString(annotation);
        String foundHosts = asString(found);
        throw logMessageAndThrowException(MULTIPLE_HOSTS_FOUND, location, criteria, foundHosts, criteria);
    }

    private void failInjectionWithEmptyValue(TafHost annotation) {
        String location = getLocation(field);
        String criteria = asString(annotation);
        String availableHosts = asString(TafDataHandler.getAllEnmHosts());
        throw logMessageAndThrowException(NO_HOSTS_FOUND, location, criteria, availableHosts);
    }

    private void checkAssignment(Field field, Object value) {
        Class<?> fieldType = field.getType();
        Class<?> valueType = value.getClass();
        if (!fieldType.isAssignableFrom(valueType)) {
            String location = getLocation(field);
            String fieldTypeName = fieldType.getCanonicalName();
            String valueTypeName = valueType.getCanonicalName();
            throw logMessageAndThrowException(NOT_ASSIGNABLE, location, fieldTypeName, valueTypeName);
        }
    }

    @VisibleForTesting
    static String asString(TafHost annotation) {
        return format(HOST_REPRESENTATION, annotation.hostname(), annotation.group(), annotation.type());
    }

    @VisibleForTesting
    static String asString(Host host) {
        return format(HOST_REPRESENTATION, host.getHostname(), host.getGroup(), host.getType().getName().toLowerCase());
    }

    private String asString(List<EnmNetworkHost> hosts) {
        return Collections2.transform(hosts, new Function<EnmNetworkHost, String>() {
            @Override
            public String apply(EnmNetworkHost host) {
                String hostNames = Collections2.transform(host.getNetworkInterfaces(), new Function<NetworkInterface, String>() {
                    @Override
                    public String apply(NetworkInterface input) {
                        return input.getHostname();
                    }
                }).toString();

                return format(HOST_REPRESENTATION, hostNames, host.getGroup(), host.getType());
            }
        }).toString();
    }

    private static String asString(Collection<Host> hosts) {
        return Collections2.transform(hosts, new Function<Host, String>() {
            @Override
            public String apply(Host host) {
                return asString(host);
            }
        }).toString();
    }

}
