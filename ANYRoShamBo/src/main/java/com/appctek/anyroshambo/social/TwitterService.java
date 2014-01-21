package com.appctek.anyroshambo.social;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.appctek.anyroshambo.R;
import com.appctek.anyroshambo.social.auth.OAuthHeader;
import com.appctek.anyroshambo.social.auth.OAuthService;
import com.appctek.anyroshambo.social.auth.OAuthToken;
import com.appctek.anyroshambo.util.WebUtils;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class TwitterService implements SocialNetworkService {

    private static final Logger logger = LoggerFactory.getLogger(TwitterService.class);

    private OAuthService oAuthService;
    private HttpClient httpClient;
    private Context context;
    private OAuthToken consumerToken;
    private String redirectUri;

    @Inject
    public TwitterService(Context context, OAuthService oAuthService, HttpClient httpClient,
                          @Named("twConsumerToken") OAuthToken consumerToken,
                          @Named("twRedirectUri") String redirectUri) {
        this.context = context;
        this.httpClient = httpClient;
        this.oAuthService = oAuthService;
        this.consumerToken = consumerToken;
        this.redirectUri = redirectUri;
    }

    private void authorizeRequest(HttpUriRequest rq, OAuthToken token, OAuthHeader header) {
        oAuthService.authorize(rq, header, consumerToken, token);
    }

    private void authenticate() {

        new AsyncTask<Object, Object, OAuthToken>() {
            @Override
            protected OAuthToken doInBackground(Object... params) {

                final String url = "https://api.twitter.com/oauth/request_token";
                final HttpPost request = new HttpPost(url);
                authorizeRequest(request, OAuthToken.EMPTY, OAuthHeader.create().callbackUri(redirectUri));

                try {
                    final Map<String,String> responseValues = WebUtils.nameValuePairsToMap(
                            WebUtils.executeRequestParams(httpClient, request));
                    final boolean isCallbackConfirmed = Boolean.parseBoolean(responseValues.get("oauth_callback_confirmed"));
                    if (!isCallbackConfirmed) {
                        logger.error("Callback \"" + redirectUri + "\" wasn't confirmed");
                        return null;
                    }
                    final String token = responseValues.get("oauth_token");
                    final String tokenSecret = responseValues.get("oauth_token_secret");
                    return new OAuthToken(token, tokenSecret);

                } catch (IOException e) {
                    logger.error("Can't obtain request token", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(OAuthToken requestToken) {
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
