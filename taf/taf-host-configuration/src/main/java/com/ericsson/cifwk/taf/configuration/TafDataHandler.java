package com.ericsson.cifwk.taf.configuration;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;
import static com.ericsson.cifwk.meta.API.Quality.Experimental;
import static com.ericsson.cifwk.taf.configuration.GenericHostBuilder.toEnmHost;
import static com.ericsson.cifwk.taf.configuration.GenericHostBuilder.toHost;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getOnlyElement;

import java.util.List;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.meta.API.Since;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.network.EnmNetworkHost;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

@API(Experimental)
public class TafDataHandler {
    private static TafConfiguration configuration = TafConfigurationProvider.provide();

    /**
     * NOTE: All hosts that have `tunnel` property will initialize tunelling on this call
     *
     * @see #getAllEnmHosts()
     * @return all {@link Host}
     */
    public static List<Host> getAllHosts() {
        return findHost().getAll();
    }

    /**
     * @return all {@link EnmNetworkHost}
     */
    public static List<EnmNetworkHost> getAllEnmHosts() {
        return findEnmHost().getAll();
    }

    /**
     * @return fluent Builder to search {@link Host}.
     * For example {@code
     * TafDataHandler
     * .findHost()
     * .withGroup("group")
     * .withHostname("hostname")
     * .withType("type")
     * .getAll();
     * }
     */
    public static HostFinder<Host> findHost() {
        return new HostFinder<>(toHost());
    }

    /**
     * @return fluent Builder to search {@link EnmNetworkHost}.
     * For example {@code
     * TafDataHandler
     * .findEnmHost()
     * .withGroup("group")
     * .withHostname("hostname")
     * .withType("type")
     * .getAll();
     * }
     */
    public static HostFinder<EnmNetworkHost> findEnmHost() {
        return new HostFinder<>(toEnmHost());
    }

    /**
     * Gets a property from the configuration. This is the most basic get
     * method for retrieving values of properties.
     *
     * @param name property to retrieve
     * @param defaultValue default value
     * @return a object representation of the configuration property requested, converted to the class type of default
     * value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAttribute(String name, T defaultValue) {
        Class<T> clazz = (Class<T>) defaultValue.getClass();
        return configuration.getProperty(name, defaultValue, clazz);
    }

    /**
     * Finding the requested configuration property.
     *
     * @param name requested property
     * @return a object representation of the configuration property requested, or null if property not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAttribute(String name) {
        Object property = configuration.getProperty(name);
        return (T) property;
    }

    /**
     * Set a property in runtime configuration, this will replace any previously set values.
     *
     * @param name  The key of the property to change
     * @param value The new value
     */
    public static void setAttribute(String name, Object value) {
        configuration.setProperty(name, value);
    }

    /**
     * Get configuration for functionality that are not directly available using {@link TafDataHandler}
     * For example {@code TafDataHandler.getConfiguration().clear()}
     */
    public static TafConfiguration getConfiguration() {
        return configuration;
    }

    public static class HostFinder<T> {
        FluentIterable<GenericHostBuilder> finder;
        private boolean nullable = false;
        private Function<GenericHostBuilder, T> builder;

        HostFinder(Function<GenericHostBuilder, T> builder) {
            this.builder = builder;
            this.finder = FluentIterable.from(findHosts());
        }

        /**
         * Filters host by group
         */
        public HostFinder<T> withGroup(final String group) {
            checkArgument(group != null);
            finder = finder.filter(new Predicate<GenericHostBuilder>() {
                @Override
                public boolean apply(GenericHostBuilder host) {
                    return group.equals(host.getGroup());
                }
            });

            return this;
        }

        /**
         * Filters host by hostname
         */
        public HostFinder<T> withHostname(final String hostname) {
            checkArgument(hostname != null);
            finder = finder.filter(new Predicate<GenericHostBuilder>() {
                @Override
                public boolean apply(GenericHostBuilder host) {
                    return hostname.equals(host.getHostName());
                }
            });

            return this;
        }

        /**
         * Filters {@link Host} by {@link HostType}
         */
        public HostFinder<T> withType(final HostType type) {
            checkArgument(type != null);
            finder = finder.filter(new Predicate<GenericHostBuilder>() {
                @Override
                public boolean apply(GenericHostBuilder host) {
                    return type.getName().equalsIgnoreCase(host.getType());
                }
            });

            return this;
        }

        /**
         * Filters host by type
         */
        public HostFinder<T> withType(final String type) {
            checkArgument(type != null);
            finder = finder.filter(new Predicate<GenericHostBuilder>() {
                @Override
                public boolean apply(GenericHostBuilder host) {
                    return type.equalsIgnoreCase(host.getType());
                }
            });

            return this;
        }

        /**
         * Filters {@link Host} by parent
         */
        public HostFinder<T> withParent(final Host parent) {
            checkArgument(parent != null);
            finder = finder.filter(new Predicate<GenericHostBuilder>() {
                @Override
                public boolean apply(GenericHostBuilder host) {
                    return host.parent != null &&
                            host.parent.isEqualTo(parent);
                }
            });

            return this;
        }

        /**
         * Filters {@link EnmNetworkHost} by parent
         */
        public HostFinder<T> withParent(final EnmNetworkHost parent) {
            checkArgument(parent != null);
            finder = finder.filter(new Predicate<GenericHostBuilder>() {
                @Override
                public boolean apply(GenericHostBuilder host) {
                    return host.parent != null &&
                            host.parent.isEqualTo(parent);
                }
            });

            return this;
        }

        /**
         * Filters host by parent hostname
         */
        public HostFinder<T> withParentHostName(final String parentHostName) {
            checkArgument(parentHostName != null);
            finder = finder.filter(new Predicate<GenericHostBuilder>() {
                @Override
                public boolean apply(GenericHostBuilder host) {
                    return parentHostName.equalsIgnoreCase(host.getParentName());
                }
            });

            return this;
        }

        /**
         * Filters host by predicate
         *
         * @see HostFilter
         */
        public HostFinder<T> withPredicate(Predicate<HostFilter> customPredicate) {
            checkArgument(customPredicate != null);
            finder = finder.filter(customPredicate);

            return this;
        }

        /**
         * Return null if no host found. If {@link #nullable()} not set, will throw exception if no host found.
         */
        public HostFinder<T> nullable() {
            this.nullable = true;
            return this;
        }

        /**
         * Gets all hosts that matches given criteria
         *
         * @see #get()
         * @see #getFirst()
         */
        public List<T> getAll() {
            return finder.transform(builder).toList();
        }

        /**
         * Gets first host that matches given criteria, even if than one host found
         * If {@link #nullable()} not set, will throw exception if no host found.
         *
         * @see #get()
         * @see #getAll()
         */
        public T getFirst() {
            if (finder.isEmpty()) {
                return nullOrException();
            }
            return Iterables.get(finder.transform(builder), 0);
        }

        /**
         * Gets one host that matches given criteria
         * Will throw exception if more than one host found, use {@link #getFirst()} for different requirements
         * If {@link #nullable()} not set, will throw exception if no host found.
         *
         *
         * @see #getFirst()
         * @see #getAll()
         */
        public T get() {
            if (finder.isEmpty()) {
                return nullOrException();
            }
            return getOnlyElement(finder.transform(builder));
        }

        private T nullOrException() {
            if (nullable) {
                return null;
            } else {
                throw new IllegalArgumentException("Unable to find Host with defined criteria");
            }
        }
    }

    private static List<GenericHostBuilder> findHosts() {
        return HostPropertyParser.propsToHosts(getConfiguration().getProperties());
    }

    // Deprecated section ↓

    /**
     * @deprecated use {@code findHost()
     * .nullable()
     * .withType(HostType)
     * .getFirst();}
     * instead.
     */
    @Deprecated
    @API(Deprecated)
    @Since(2.34)
    public static Host getHostByType(HostType jboss) {
        return findHost()
                .withType(jboss.toString())
                .nullable()
                .getFirst();
    }

    /**
     * @deprecated use {@code findHost()
     * .nullable()
     * .withHostname(String)
     * .getFirst();}
     * instead.
     */
    @Deprecated
    @API(Deprecated)
    @Since(2.34)
    public static Host getHostByName(String name) {
        return findHost()
                .withHostname(name)
                .nullable()
                .getFirst();
    }

    /**
     * @deprecated use {@code findHost()
     * .nullable()
     * .withGroup(String)
     * .getAll();}
     * instead.
     */
    @Deprecated
    @API(Deprecated)
    @Since(2.34)
    public static List<Host> getAllHostsByGroup(String name) {
        return findHost().withGroup(name).getAll();
    }

    /**
     * @deprecated use {@code TafDataHandler.getConfiguration().clearProperty(key)}
     */
    @Deprecated
    @API(Deprecated)
    @Since(2.34)
    public static void unsetAttribute(String key) throws NullPointerException {
        configuration.clearProperty(key);
    }

    /**
     * @deprecated use {@code TafDataHandler.getConfiguration().clear()}
     */
    @Deprecated
    @API(Deprecated)
    @Since(2.34)
    public static void unsetAttributes() {
        configuration.clear();
    }

    /**
     * @deprecated use {@code findHost()
     * .nullable()
     * .withType(HostType)
     * .getFirst();}
     * instead.
     */
    @Deprecated
    @API(Deprecated)
    @Since(2.34)
    public static List<Host> getAllHostsByType(HostType netsim) {
        return findHost().withType(netsim.toString()).getAll();
    }

    /**
     * @deprecated use {@link #getAllHosts()} instead.
     */
    @Deprecated
    @API(Deprecated)
    @Since(2.34)
    public static List<Host> getHosts() {
        return getAllHosts();
    }

    /**
     * @deprecated use {@code findHost()
     * .withParent(String)
     * .nullable()
     * .withType(HostType)
     * .getFirst();}
     * instead.
     */
    @Deprecated
    @API(Deprecated)
    @Since(2.34)
    public static Host getSpecificNode(Host host, HostType type) {
        return findHost()
                .withParent(host)
                .nullable()
                .withType(type.toString())
                .getFirst();
    }


    // Deprecated section ↑
}
