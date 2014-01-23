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
                          OAuthToken consumerToken, OAuthToken accessToken, OAuthHeader header) {

        final String httpMethod = request.getMethod();
        final URI uri = request.getURI();
        final String baseUri = WebUtils.getUriWithoutParams(uri.toString());

        final List<NameValuePair> postParams = WebUtils.parseRequestParams(request);
        final List<NameValuePair> urlParams = URLEncodedUtils.parse(uri, WebUtils.UTF_8);
        final List<NameValuePair> oauthParams = header.
                version(OAuthHeader.DEFAULT_VERSION).
                signatureMethod(OAuthHeader.DEFAULT_SIGNATURE_METHOD).
                nonce(OAuthUtils.generateNonce(random)).
                timestamp(TimeUnit.MILLISECONDS.toSeconds(dateTimeService.getTimeInMillis())).
                consumerKey(consumerToken.getKey()).
                token(accessToken.getKey()).
                asNameValuePairs();

        final String signature = OAuthUtils.buildSignature(
                httpMethod,
                baseUri,
                consumerToken.getSecret(),
                accessToken.getSecret(),
                urlParams,
                postParams,
                oauthParams);

        final String headerValue = header.signature(signature).toString();
        request.setHeader("Authorization", headerValue);
    }


}
