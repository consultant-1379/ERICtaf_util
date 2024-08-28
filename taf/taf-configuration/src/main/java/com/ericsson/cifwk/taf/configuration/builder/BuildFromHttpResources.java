package com.ericsson.cifwk.taf.configuration.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.configuration.processor.ConfigurationProcessor;

public class BuildFromHttpResources {

    static final Logger logger = LoggerFactory.getLogger(BuildFromHttpResources.class);
    public static final String MSG_ERROR_CANT_CREATE_URI = "Can't create URI for url:%s, parameters:?%s,  throws %s ";

    String url;
    List<NameValuePair> parameters = new ArrayList<>();
    ConfigurationProcessor<HttpGet> function;

    public BuildFromHttpResources applyEach(final ConfigurationProcessor<HttpGet> function) {
        this.function = function;
        return this;
    }

    public BuildFromHttpResources forUrl(String url) {
        this.url = url;
        return this;
    }

    public BuildFromHttpResources withParameter(String param, String value) {
        parameters = Arrays.asList(new NameValuePair[]{new BasicNameValuePair(param, value)});
        return this;
    }

    public Configuration build(final CompositeConfiguration configuration) {
        try {
            HttpGet request = new HttpGet(
                    new URIBuilder(url).addParameters(parameters).build()
            );
            Configuration c = function.apply(request);
            if (c != null) configuration.addConfiguration(c);
        } catch (Exception e) {
            String msg = String.format(MSG_ERROR_CANT_CREATE_URI, url, Arrays.asList(parameters), e.getMessage());
            logger.warn(msg);
            logger.trace(e.getMessage(), e);
        }
        return configuration;
    }
}
