package com.appctek.anyroshambo.social;

import android.os.AsyncTask;
import com.appctek.anyroshambo.services.DateTimeService;
import com.appctek.anyroshambo.social.auth.OAuthHeaderParams;
import com.appctek.anyroshambo.social.auth.OAuthUtils;
import com.appctek.anyroshambo.util.WebUtils;
import com.google.inject.Inject;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class TwitterService implements SocialNetworkService {

    private static final String CONSUMER_KEY = "eNAz8lhCSpXog4F0UbScQA";
    private static final String CONSUMER_SECRET = "KbVJTv5BlHMJlETCARFWwBHlTq53wim7XJocuG5N5So";
    public static final String CALLBACK_URI = "http://appctek.com/anyroshambo";

    private Random random;
    private DateTimeService dateTimeService;
    private HttpClient httpClient;

    @Inject
    public TwitterService(Random random, DateTimeService dateTimeService, HttpClient httpClient) {
        this.random = random;
        this.dateTimeService = dateTimeService;
        this.httpClient = httpClient;
    }

    private static class OAuthTokens {
        private String token;
        private String tokenSecret;

        private OAuthTokens(String token, String tokenSecret) {
            this.token = token;
            this.tokenSecret = tokenSecret;
        }

        public String getToken() {
            return token;
        }

        public String getTokenSecret() {
            return tokenSecret;
        }
    }

    private HttpUriRequest authorizeRequest(HttpUriRequest request, OAuthHeaderParams params) {

        final String httpMethod = request.getMethod();
        final URI uri = request.getURI();

        final List<NameValuePair> postParams = "POST".equals(httpMethod)
                ? WebUtils.parseParams(((HttpPost) request).getEntity())
                : Collections.<NameValuePair>emptyList();
        final List<NameValuePair> urlParams = URLEncodedUtils.parse(uri, WebUtils.DEFAULT_CHARSET.name());
        params.httpMethod(httpMethod).
                baseUrl(WebUtils.getUriWithoutParams(uri.toString())).
                version(OAuthHeaderParams.DEFAULT_VERSION).
                signatureMethod(OAuthHeaderParams.DEFAULT_SIGNATURE_METHOD).
                consumerKey(CONSUMER_KEY).
                nonce(OAuthUtils.generateNonce(random)).
                timestamp(dateTimeService.getTimeInMillis()).
                urlParams(urlParams).
                postParams(postParams);

        final String headerValue = params.toString();
        request.addHeader("Authorization", headerValue);
        return request;
    }

    private void authenticate() {

        new AsyncTask<Object, Object, OAuthTokens>() {
            @Override
            protected OAuthTokens doInBackground(Object... params) {

                final String url = "https://api.twitter.com/oauth/request_token";
                final HttpPost post = new HttpPost(url);
                authorizeRequest(post, new OAuthHeaderParams().callbackUri(CALLBACK_URI));

                try {
                    httpClient.execute(post);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // TODO implement..
                return null;
            }

            @Override
            protected void onPostExecute(OAuthTokens requestToken) {
                System.out.println(requestToken);
            }
        };

    }

    public void shareText(boolean revoke, String text) {
        authenticate();
    }
}
