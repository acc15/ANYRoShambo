package com.appctek.anyroshambo.social.auth;

import com.appctek.anyroshambo.services.DateTimeService;
import com.appctek.anyroshambo.util.WebUtils;
import com.google.inject.Inject;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-22-01
 */
public class OAuthService {

    private DateTimeService dateTimeService;
    private Random random;

    @Inject
    public OAuthService(DateTimeService dateTimeService, Random random) {
        this.dateTimeService = dateTimeService;
        this.random = random;
    }

    public void authorize(HttpUriRequest request,
                          OAuthHeaderParams params) {

        final String httpMethod = request.getMethod();
        final URI uri = request.getURI();

        final List<NameValuePair> postParams = WebUtils.parseRequestParams(request);
        final List<NameValuePair> urlParams = URLEncodedUtils.parse(uri, WebUtils.DEFAULT_CHARSET);

        final String headerValue = params.
                httpMethod(httpMethod).
                baseUrl(WebUtils.getUriWithoutParams(uri.toString())).
                version(OAuthHeaderParams.DEFAULT_VERSION).
                signatureMethod(OAuthHeaderParams.DEFAULT_SIGNATURE_METHOD).
                nonce(OAuthUtils.generateNonce(random)).
                timestamp(TimeUnit.MILLISECONDS.toSeconds(dateTimeService.getTimeInMillis())).
                urlParams(urlParams).
                postParams(postParams).
                sign().
                toString();

        request.setHeader("Authorization", headerValue);
    }


}
