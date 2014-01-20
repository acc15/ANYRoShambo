package com.appctek.anyroshambo.social;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.appctek.anyroshambo.R;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class TwitterService implements SocialNetworkService {

    private static final String CONSUMER_KEY = "eNAz8lhCSpXog4F0UbScQA";
    private static final String CONSUMER_SECRET = "KbVJTv5BlHMJlETCARFWwBHlTq53wim7XJocuG5N5So";
    private static final String CALLBACK_URI = "http://appctek.com/anyroshambo";

    private static final Logger logger = LoggerFactory.getLogger(TwitterService.class);

    private Random random;
    private DateTimeService dateTimeService;
    private HttpClient httpClient;
    private Context context;

    @Inject
    public TwitterService(Context context, Random random, DateTimeService dateTimeService, HttpClient httpClient) {
        this.context = context;
        this.random = random;
        this.dateTimeService = dateTimeService;
        this.httpClient = httpClient;
    }

    private static class TokenPair {
        private String token;
        private String tokenSecret;

        private TokenPair(String token, String tokenSecret) {
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

        final List<NameValuePair> postParams = WebUtils.parseRequestParams(request);
        final List<NameValuePair> urlParams = URLEncodedUtils.parse(uri, WebUtils.DEFAULT_CHARSET);

        final String headerValue = params.
                httpMethod(httpMethod).
                baseUrl(WebUtils.getUriWithoutParams(uri.toString())).
                version(OAuthHeaderParams.DEFAULT_VERSION).
                signatureMethod(OAuthHeaderParams.DEFAULT_SIGNATURE_METHOD).
                consumerKey(CONSUMER_KEY).
                consumerSecret(CONSUMER_SECRET).
                nonce(OAuthUtils.generateNonce(random)).
                timestamp(TimeUnit.MILLISECONDS.toSeconds(dateTimeService.getTimeInMillis())).
                urlParams(urlParams).
                postParams(postParams).
                sign().
                toString();

        request.setHeader("Authorization", headerValue);
        return request;
    }

    private void authenticate() {

        new AsyncTask<Object, Object, TokenPair>() {
            @Override
            protected TokenPair doInBackground(Object... params) {

                final String url = "https://api.twitter.com/oauth/request_token";
                final HttpPost request = new HttpPost(url);
                authorizeRequest(request, new OAuthHeaderParams().callbackUri(CALLBACK_URI));

                try {
                    final Map<String,String> responseValues = WebUtils.nameValuePairsToMap(
                            WebUtils.executeRequestParams(httpClient, request));
                    final boolean isCallbackConfirmed = Boolean.parseBoolean(responseValues.get("oauth_callback_confirmed"));
                    if (!isCallbackConfirmed) {
                        logger.error("Callback \"" + CALLBACK_URI + "\" wasn't confirmed");
                        return null;
                    }
                    final String token = responseValues.get("oauth_token");
                    final String tokenSecret = responseValues.get("oauth_token_secret");
                    return new TokenPair(token, tokenSecret);

                } catch (IOException e) {
                    logger.error("Can't obtain request token", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(TokenPair requestToken) {
                if (requestToken == null) {
                    Toast.makeText(context, R.string.share_error, Toast.LENGTH_LONG).show();
                    return;
                }

                System.out.println(requestToken);
            }
        }.execute();

    }

    public void shareText(boolean revoke, String text) {
        authenticate();
    }
}
