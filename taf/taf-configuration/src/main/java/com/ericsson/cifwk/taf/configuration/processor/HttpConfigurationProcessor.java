package com.ericsson.cifwk.taf.configuration.processor;

import com.ericsson.cifwk.taf.configuration.parser.ConfigurationParser;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static com.ericsson.cifwk.taf.configuration.processor.HttpClientFactory.HTTP_CLIENT_FACTORY;

class HttpConfigurationProcessor extends ConfigurationProcessor<HttpGet> {

    static private final Logger logger = LoggerFactory.getLogger(HttpConfigurationProcessor.class);

    public HttpConfigurationProcessor(ConfigurationParser parser) {
        super(parser);
    }

    @Override
    public Configuration apply(HttpGet request) {
        final PropertiesConfiguration config = new PropertiesConfiguration();
        config.setFileName(request.toString());
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HTTP_CLIENT_FACTORY.create();
            response = httpClient.execute(request);
            InputStream is = response.getEntity().getContent();
            if (parser != null) {
                Configuration c = parser.parse(is);
                if (c != null) config.append(c);
            }
        } catch (Exception e) {
            logger.warn("Can't load configuration from:" + request.getURI() + ", throws " + e.getMessage());
            logger.trace(e.getMessage(), e);
        } finally {
            // closes entity stream as well
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(httpClient);
        }
        return config;
    }

}
