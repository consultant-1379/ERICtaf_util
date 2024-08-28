package com.ericsson.cifwk.taf.osgi.client;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Java API wrapper for the TAF OSGi agent.
 */
public class ApiClient {

	private final String endpoint;
	private final DefaultHttpClient httpClient;

	ApiClient(String endpoint) {
		this.endpoint = endpoint;
		this.httpClient = new DefaultHttpClient(
				new PoolingClientConnectionManager());
	}

	/**
	 * Pings the agent.
	 * 
	 * @return true, if agent is active at the endpoint
	 */
	public boolean isAlive() {
		HttpGet request = new HttpGet(endpoint);
		return executeRequest(request).isSuccess();
	}

	/**
	 * Registers a new Groovy class on the agent from source.
	 * 
	 * @param source
	 *            source of the Groovy class
	 * @return response with registered class name
	 */
	public ApiResponse register(String source) {
		HttpPost request = new HttpPost(endpoint + "/classes");
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("source", source));

		try {
			request.setEntity(new UrlEncodedFormEntity(params));
		} catch (UnsupportedEncodingException e) {
			return failResponse(e.getMessage());
		}
		return executeRequest(request);
	}

	/**
	 * Remotely invokes a method in one of classes registered on the agent.
	 * Invocations of unregistered classes would be treated as bad requests.
	 * 
	 * @param className
	 *            target class name
	 * @param method
	 *            target method name
	 * @param args
	 *            arguments for target method
	 * @return response with method result as string
	 */
	public ApiResponse invoke(String className, String method, String... args) {
		HttpPost request = new HttpPost(endpoint + "/classes/" + className);
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("method", method));

		int argCount = args.length;
		params.add(new BasicNameValuePair("argCount", Integer
				.toString(argCount)));
		for (int i = 0; i < argCount; i++) {
			params.add(new BasicNameValuePair("arg" + i, args[i]));
		}

		try {
			request.setEntity(new UrlEncodedFormEntity(params));
		} catch (UnsupportedEncodingException e) {
			return failResponse(e.getMessage());
		}
		return executeRequest(request);
	}

	private ApiResponse executeRequest(HttpUriRequest request) {
		try {
			HttpResponse response = httpClient.execute(request);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			String result = EntityUtils.toString(response.getEntity());
			return new ApiResponse(statusCode >= 200 && statusCode < 300,
					result);
		} catch (IOException e) {
			return failResponse(e.getMessage());
		}
	}

	private ApiResponse failResponse(String message) {
		return new ApiResponse(false, message);
	}

}
