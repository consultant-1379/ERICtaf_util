package com.ericsson.cifwk.taf.performance.threshold.graphite;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.google.common.base.Throwables;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public class GraphiteClient {

    private final Host host;
    private final CloseableHttpClient httpClient;

    public GraphiteClient(Host host) {
        this.host = host;
        httpClient = HttpClients.createDefault();
    }

    public String get(String format, String target, String from, String until) throws IOException {
        URI uri = getURI(format, target, from, until);
        HttpGet request = new HttpGet(uri);
        CloseableHttpResponse response = httpClient.execute(request);
        try {
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } finally {
        	if (response != null) response.close();
        }
    }

    private URI getURI(String format, String target, String from, String until) {
        try {
            String ip = host.getIp();
            String port = host.getPort().get(Ports.HTTP);

            return new URIBuilder()
                    .setScheme("http")
                    .setHost(ip)
                    .setPort(Integer.parseInt(port))
                    .setPath("/render")
                    .addParameter("format", format)
                    .addParameter("target", target)
                    .addParameter("from", from)
                    .addParameter("until", until)
                    .build();
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

}
