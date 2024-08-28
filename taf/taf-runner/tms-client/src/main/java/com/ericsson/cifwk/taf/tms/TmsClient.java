/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.tms;

import com.ericsson.cifwk.taf.tms.dto.TestCampaignInfo;
import com.ericsson.cifwk.taf.tms.dto.TestCaseInfo;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

@ThreadSafe
public class TmsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TmsClient.class);

    private static final int DEFAULT_TIMEOUT_SECONDS = 6;

    private final TafConfig tafConfig;

    private final CloseableHttpClient httpClient;

    public TmsClient() {
        httpClient = HttpClientFactory.create();
        tafConfig = TafConfig.newInstance();
        closeOnShutDown();
    }

    public TestCaseInfo getTestCase(String testCaseId) {
        if (!tafConfig.isEnabled()) {
            return null;
        }

        try {
            return getEntity(tafConfig.getApiUrlForTestCase(testCaseId), TestCaseInfo.class);
        } catch (IOException e) {
            LOGGER.info("Retrieval of TestCase from TMS is unsuccessful. Continuing with the execution of testcase");
            return null;
        }
    }

    public TestCampaignInfo getTestCampaign(long testCampaignId) {
        if (!tafConfig.isEnabled()) {
            return null;
        }

        try {
            return getEntity(tafConfig.getApiUrlForTestCampaign(testCampaignId), TestCampaignInfo.class);
        } catch (IOException e) {
            LOGGER.info("Retrieval of TestCampaign from TMS is unsuccessful. Continuing with the execution of testcase");
            return null;
        }
    }

    public void close() {
        try {
            httpClient.close();
        } catch (IOException ignored) { // NOSONAR
            // do nothing
        }
    }

    private void closeOnShutDown() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
    }


    protected <T> T getEntity(String uri, Class<T> entityClass) throws IOException {
        HttpGet request = new HttpGet(uri);
        setRequestTimeout(request);

        CloseableHttpResponse httpResponse = null;
        Reader response = null;
        try {
            httpResponse = httpClient.execute(request);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                LOGGER.info("Test Case/Plan ID was not found in TMS");
                return null;
            }

            InputStream content = httpResponse.getEntity().getContent();
            response = new InputStreamReader(content); // NOSONAR - Sonar complains we don't close reader, but we do
            return new Gson().fromJson(response, entityClass);
        } finally {
            request.releaseConnection();
            try {
                Closeables.close(httpResponse, true);
            } catch (IOException e) { // NOSONAR
                // ignored
            }
            try {
                Closeables.close(response, true);
            } catch (IOException e) { // NOSONAR
                // ignored
            }
        }
    }

    private void setRequestTimeout(HttpRequestBase request) {
        RequestConfig oldConfig = request.getConfig() == null ? RequestConfig.DEFAULT : request.getConfig();
        RequestConfig newConfig = RequestConfig.copy(oldConfig)
                .setConnectTimeout(DEFAULT_TIMEOUT_SECONDS * 1000)
                .setSocketTimeout(DEFAULT_TIMEOUT_SECONDS * 1000)
                .build();
        request.setConfig(newConfig);
    }

}
