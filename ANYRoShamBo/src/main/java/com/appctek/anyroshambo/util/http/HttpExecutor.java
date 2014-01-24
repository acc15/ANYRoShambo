package com.appctek.anyroshambo.util.http;

import com.appctek.anyroshambo.util.converters.Converter;
import com.appctek.anyroshambo.util.GenericException;
import com.google.inject.Inject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public class HttpExecutor {

    private HttpClient httpClient;

    @Inject
    public HttpExecutor(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpResponse execute(HttpUriRequest request) {
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            throw new GenericException("Can't execute HTTP " + request.getMethod() + " request", e);
        }
    }

    public <T> T execute(HttpUriRequest request, Converter<HttpEntity, T> converter) {
        final HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new GenericException("Can't execute HTTP " + request.getMethod() + " request", e);
        }
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            throw new GenericException("Server responded with error status: " + statusCode);
        }
        final HttpEntity entity = response.getEntity();
        return converter.convert(entity);
    }

}
