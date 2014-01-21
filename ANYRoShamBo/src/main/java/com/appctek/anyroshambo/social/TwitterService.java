package com.appctek.anyroshambo.social;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.appctek.anyroshambo.R;
import com.appctek.anyroshambo.social.auth.OAuthHeaderParams;
import com.appctek.anyroshambo.social.auth.OAuthService;
import com.appctek.anyroshambo.social.auth.OAuthToken;
import com.appctek.anyroshambo.util.WebUtils;
import com.google.inject.Inject;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class TwitterService implements SocialNetworkService {

    private static final String CONSUMER_KEY = "eNAz8lhCSpXog4F0UbScQA";
    private static final String CONSUMER_SECRET = "KbVJTv5BlHMJlETCARFWwBHlTq53wim7XJocuG5N5So";
    private static final String CALLBACK_URI = "http://appctek.com/anyroshambo";

    private static final Logger logger = LoggerFactory.getLogger(TwitterService.class);

    private OAuthService oAuthService;
    private HttpClient httpClient;
    private Context context;

    @Inject
    public TwitterService(Context context, OAuthService oAuthService, HttpClient httpClient) {
        this.context = context;
        this.httpClient = httpClient;
        this.oAuthService = oAuthService;
    }

    private OAuthHeaderParams createParams() {
        return OAuthHeaderParams.create().
                consumerKey(CONSUMER_KEY).
                consumerSecret(CONSUMER_SECRET);
    }

    private void authenticate() {

        new AsyncTask<Object, Object, OAuthToken>() {
            @Override
            protected OAuthToken doInBackground(Object... params) {

                final String url = "https://api.twitter.com/oauth/request_token";
                final HttpPost request = new HttpPost(url);
                oAuthService.authorize(request, createParams().callbackUri(CALLBACK_URI));

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
