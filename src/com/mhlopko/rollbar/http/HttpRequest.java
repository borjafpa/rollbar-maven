package com.mhlopko.rollbar.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

public class HttpRequest {

    private static final int SOCKET_TIMEOUT = 5000;
    private static final int CONNECTION_TIMEOUT = 5000;

    private URL url;
    private String body;
    private int attemptNumber;

    public HttpRequest(URL url, String body) {
        this.url = url;
        this.body = body;

        attemptNumber = 0;
    }

    public boolean execute() {

        attemptNumber++;

        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .build();
        HttpClient httpClient = buildHttpClientWithConfig(config);
        HttpPost postRequest = new HttpPost(url.toString());

        StringEntity params;

        try {
            params = new StringEntity(body);
            postRequest.setEntity(params);
        } catch (UnsupportedEncodingException e1) {
            return false;
        }

        postRequest.setHeader("accept", "application/json");
        postRequest.setHeader("content-type", "application/json");


        try {
            HttpResponse response = httpClient.execute(postRequest);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) return false;

        } catch (ClientProtocolException e) {
            return false;
        } catch (IOException e) {
            return false;
        }   finally {
            HttpClientUtils.closeQuietly(httpClient);
        }

        return true;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    private static HttpClient buildHttpClientWithConfig(RequestConfig config) {
        return HttpClients.custom().setDefaultRequestConfig(config).build();
    }

}
