package com.appctek.anyroshambo.social;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;
import com.appctek.anyroshambo.R;
import com.appctek.anyroshambo.social.auth.*;
import com.appctek.anyroshambo.social.task.Task;
import com.appctek.anyroshambo.social.task.TaskManager;
import com.appctek.anyroshambo.social.token.TokenManager;
import com.appctek.anyroshambo.util.WebUtils;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class TwitterService implements SocialNetworkService {

    private static final Logger logger = LoggerFactory.getLogger(TwitterService.class);

    private static final String TW_TOKEN = "tw";
    private static final String TW_TOKEN_SECRET = "tw.secret";

    private OAuthService oAuthService;
    private HttpClient httpClient;
    private Context context;
    private OAuthToken consumerToken;
    private String redirectUri;
    private TokenManager tokenManager;
    private TaskManager taskManager;

    @Inject
    public TwitterService(Context context, TokenManager tokenManager, TaskManager taskManager,
                          OAuthService oAuthService, HttpClient httpClient,
                          @Named("twConsumerToken") OAuthToken consumerToken,
                          @Named("twRedirectUri") String redirectUri) {
        this.context = context;
        this.tokenManager = tokenManager;
        this.taskManager = taskManager;
        this.httpClient = httpClient;
        this.oAuthService = oAuthService;
        this.consumerToken = consumerToken;
        this.redirectUri = redirectUri;
    }

    private void doWithToken(final boolean revoke, final Task<OAuthToken, ErrorInfo> task) {
        if (revoke) {
            tokenManager.revokeToken(TW_TOKEN);
            tokenManager.revokeToken(TW_TOKEN_SECRET);
        } else {
            final String token = tokenManager.getTokenAsString(TW_TOKEN);
            final String tokenSecret = tokenManager.getTokenAsString(TW_TOKEN_SECRET);
            if (token != null && tokenSecret != null) {
                taskManager.executeAsync(task, new OAuthToken(token, tokenSecret));
                return;
            }
        }

        new AsyncTask<Object, Object, OAuthToken>() {
            @Override
            protected OAuthToken doInBackground(Object... params) {

                final String url = "https://api.twitter.com/oauth/request_token";
                final HttpPost request = new HttpPost(url);
                oAuthService.authorize(request, consumerToken, OAuthToken.EMPTY,
                        new OAuthHeader().callbackUri(redirectUri));
                try {
                    final Map<String,String> responseValues = WebUtils.nameValuePairsToMap(
                            WebUtils.executeRequestParams(httpClient, request));
                    final boolean isCallbackConfirmed = Boolean.parseBoolean(
                            responseValues.get("oauth_callback_confirmed"));
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
            protected void onPostExecute(final OAuthToken requestToken) {
                if (requestToken == null) {
                    Toast.makeText(context, R.string.share_error, Toast.LENGTH_LONG).show();
                    return;
                }
                showAuthDialog(revoke, requestToken, task);
            }
        }.execute();

    }

    private void showAuthDialog(final boolean revoke, final OAuthToken requestToken,
                                final Task<OAuthToken, ErrorInfo> task) {

        final Uri.Builder uriBuilder = Uri.parse("https://api.twitter.com/oauth/authenticate").buildUpon().
                appendQueryParameter("oauth_token", requestToken.getKey());
        if (revoke) {
            uriBuilder.appendQueryParameter("force_login", "true");
        }

        final String requestUrl = uriBuilder.build().toString();
        WebUtils.showWebViewDialog(context, requestUrl, new WebUtils.UrlHandler() {
            public boolean handleUrl(DialogInterface dialog, String url) {
                if (!url.startsWith(redirectUri)) {
                    return false;
                }

                final Uri uri = Uri.parse(url);
                final String returnedToken = uri.getQueryParameter("oauth_token");
                if (!requestToken.getKey().equals(returnedToken)) {
                    logger.error("Returned oauth_token mismatch. Either User cancelled authentication. " +
                            "Expected: " + requestToken.getKey() +
                            "; But received: " + returnedToken);
                    Toast.makeText(context, R.string.share_error, Toast.LENGTH_LONG).show();
                    dialog.cancel();
                    return true;
                }

                final String verifier = uri.getQueryParameter("oauth_verifier");
                logger.debug("Received verifier: " + verifier);
                obtainAccessToken(requestToken, verifier, task);
                return true;
            }
        });

    }

    private void obtainAccessToken(final OAuthToken requestToken, final String verifier,
                                   final Task<OAuthToken, ErrorInfo> task) {
        new AsyncTask<Object,Object,OAuthToken>() {
            @Override
            protected OAuthToken doInBackground(Object... params) {

                final HttpPost httpPost = new HttpPost("https://api.twitter.com/oauth/access_token");

                final List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
                requestParams.add(new BasicNameValuePair("oauth_verifier", verifier));
                httpPost.setEntity(WebUtils.createUrlEncodedFormEntity(requestParams));

                oAuthService.authorize(httpPost, consumerToken, requestToken, new OAuthHeader());
                try {
                    final Map<String,String> responseParams = WebUtils.nameValuePairsToMap(
                            WebUtils.executeRequestParams(httpClient, httpPost));

                    final String accessToken = responseParams.get("oauth_token");
                    final String accessTokenSecret = responseParams.get("oauth_token_secret");
                    if (accessToken == null || accessTokenSecret == null) {
                        logger.error("Access token or access token secret wasn't returned");
                        return null;
                    }
                    return new OAuthToken(accessToken, accessTokenSecret);

                } catch (IOException e) {
                    logger.error("Can't obtain access token", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(OAuthToken token) {
                if (token == null) {
                    return;
                }
                logger.info("Access token obtained: " + token);
                task.execute(token);
            }
        }.execute();
    }

    public void shareText(boolean revoke, String text) {
        doWithToken(revoke, new Task<OAuthToken, ErrorInfo>() {
            public ErrorInfo execute(final OAuthToken token) {
                // TODO implement...
                return ErrorInfo.success();
            }

            public void onFinish(ErrorInfo error) {
                Toast.makeText(context, R.string.share_error, Toast.LENGTH_LONG).show();

            }
        });
    }
}
