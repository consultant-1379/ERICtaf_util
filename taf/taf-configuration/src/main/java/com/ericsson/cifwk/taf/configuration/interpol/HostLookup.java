package com.ericsson.cifwk.taf.configuration.interpol;

import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.configuration.exception.HostInterpolationException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import org.apache.commons.configuration.PropertyConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ekonsla on 16/08/2016.
 * <p>
 * Custom apache common configuration interpolator
 * used for embedding host to property in a format <porttype>://<ip>:<port>
 * referring to host as ${host:<hostname> port:<porttype> useschema:<true | false>}
 *
 * e.g.
 * svc-1.login.url=${host:svc-1 port:https useschema:true}/login
 * will be resolved as https://10.43.251.3:8443/login
 *
 * ${host:httpd} -> 192.168.0.1
 * ${host:httpd port:http} -> 192.168.0.1:8080
 * ${host:httpd port:http useschema:true} -> http://192.168.0.1:8080
 *
 * Default ports for http/https 80/443 are ommited
 *
 * ${host:httpd port:ssh useschema:true} -> 192.168.0.1:22 schema is omitted for ports other than http/https
 *
 * <p/>
 */
public class HostLookup extends StrLookup {

    private static final Logger LOGGER = LoggerFactory.getLogger(HostLookup.class);

    public static final String PREFIX = "host";
    private static final String PORT_PREFIX = "port";
    private static final String SCHEMA_PREFIX = "useSchema";

    private static final String HTTP_DEFAULT_PORT = "80";
    private static final String HTTPS_DEFAULT_PORT = "443";

    private static final String HTTP_SCHEMA = "http";
    private static final String HTTPS_SCHEMA = "https";

    private static final char PARAMS_DELIMITER = ' ';
    private static final char KEY_VALUE_DELIMITER = ':';

    private static final Map<String, String> DEFAULTS_PORTS;

    static {
        DEFAULTS_PORTS = new HashMap<>();
        DEFAULTS_PORTS.put(HTTP_SCHEMA, HTTP_DEFAULT_PORT);
        DEFAULTS_PORTS.put(HTTPS_SCHEMA, HTTPS_DEFAULT_PORT);
    }

    @Override
    public String lookup(String key) {
        List<String> config = PropertyConverter.split(key, PARAMS_DELIMITER, true);
        if (config.get(0).isEmpty()) {
            String msg = "Host definition should come in a format: ${host:<hostname>} or  ${host:<hostname> port:<porttype>}";
            LOGGER.error(msg);
            throw new HostInterpolationException(msg);
        }
        String hostname = config.get(0);

        TafConfiguration configuration = TafConfigurationProvider.provide();
        String ip = configuration.getString(getHostIpKey(hostname));

        if (ip == null) {
            String msg = String.format("Host [%s] is  not found", hostname);
            LOGGER.error(msg);
            throw new HostInterpolationException(msg);
        }

        StringBuilder urlBuilder = new StringBuilder();

        urlBuilder
            .append(getSchema(config))
            .append(ip)
            .append(getPort(hostname, config, configuration));

        return urlBuilder.toString();
    }

    @VisibleForTesting
    protected Optional<String> findParam(String key, List<String> config) {
        for (String keyValue : config) {
            if (keyValue.trim().startsWith(key)) {
                List<String> keyValList = PropertyConverter.split(keyValue.trim(), KEY_VALUE_DELIMITER, true);
                if (keyValList.size() < 2 || StringUtils.isBlank(keyValList.get(1))) {
                    String msg = String.format("Parameter [%s] is empty", key);
                    LOGGER.error(msg);
                    throw new HostInterpolationException(msg);
                }
                return Optional.of(keyValList.get(1));
            }
        }
        return Optional.absent();
    }

    @VisibleForTesting
    protected String getPort(String hostname, List<String> config, TafConfiguration configuration) {
        Optional<String> portType = findParam(PORT_PREFIX, config);

        if (!portType.isPresent()) {
            return "";
        }

        String type = portType.get();
        String port = configuration.getString(getHostPortKey(hostname, portType.get()));

        if (StringUtils.isEmpty(port)) {
            String msg = String.format("Port of type [%s] is not defined for host [%s]", portType.get(), hostname);
            LOGGER.error(msg);
            throw new HostInterpolationException(msg);
        }

        if (isDefaultPort(type, port)) {
            return "";
        }

        return String.format(":%s", port);
    }

    @VisibleForTesting
    protected boolean isDefaultPort(String type, String port) {
        String defaultPort = DEFAULTS_PORTS.get(type);
        return !StringUtils.isEmpty(defaultPort) && defaultPort.equals(port);
    }

    @VisibleForTesting
    protected boolean isUseSchema(List<String> config) {
        Optional<String> useSchema = findParam(SCHEMA_PREFIX, config);
        return useSchema.isPresent() && Boolean.valueOf(useSchema.get());
    }

    @VisibleForTesting
    protected String getSchema(List<String> config) {
        Optional<String> portType = findParam(PORT_PREFIX, config);
        boolean useSchema = isUseSchema(config);
        if (!useSchema) {
            return "";
        }
        if (!portType.isPresent()){
            throw new HostInterpolationException(String.format("%s is not defined for %s", PORT_PREFIX, config));
        }
        if (!(portType.get().equals(HTTP_SCHEMA) || portType.get().equals(HTTPS_SCHEMA))) {
            return "";
        }
        return String.format("%s://", portType.get());
    }

    private String getHostIpKey(String hostname) {
        return Joiner.on(".").join("host", hostname, "ip");
    }

    private String getHostPortKey(String hostname, String portType) {
        return Joiner.on(".").join("host", hostname, "port", portType);
    }
}
