package com.appctek.anyroshambo.social;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import com.appctek.anyroshambo.social.auth.ErrorInfo;
import com.appctek.anyroshambo.social.auth.OAuthHeader;
import com.appctek.anyroshambo.social.auth.OAuthService;
import com.appctek.anyroshambo.social.auth.OAuthToken;
import com.appctek.anyroshambo.social.token.Token;
import com.appctek.anyroshambo.social.token.TokenManager;
import com.appctek.anyroshambo.util.GenericException;
import com.appctek.anyroshambo.util.Pair;
import com.appctek.anyroshambo.util.WebUtils;
import com.appctek.anyroshambo.util.http.HttpExecutor;
import com.appctek.anyroshambo.util.http.HttpFormat;
import com.appctek.anyroshambo.util.task.Task;
import com.appctek.anyroshambo.util.task.TaskManager;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class TwitterService implements SocialNetworkService {

    private static final Logger logger = LoggerFactory.getLogger(TwitterService.class);

    public static enum Error {
        URI_NOT_CONFIRMED,
        REQUEST_TOKEN_MISMATCH,
        ACCESS_TOKEN_MISSING,
        UPDATE_STATUS_ERROR,
        DUPLICATE_STATUS_ERROR
    }

    private static final String TW_TOKEN = "tw";
    private static final String TW_TOKEN_SECRET = "tw.secret";

    private OAuthService oAuthService;
    private HttpExecutor httpExecutor;
    private Context context;
    private OAuthToken consumerToken;
    private String redirectUri;
    private TokenManager tokenManager;
    private TaskManager taskManager;

    @Inject
    public TwitterService(Context context, TokenManager tokenManager, TaskManager taskManager,
                          OAuthService oAuthService, HttpExecutor httpExecutor,
                          @Named("twConsumerToken") OAuthToken consumerToken,
                          @Named("twRedirectUri") String redirectUri) {
        this.context = context;
        this.tokenManager = tokenManager;
        this.taskManager = taskManager;
        this.httpExecutor = httpExecutor;
        this.oAuthService = oAuthService;
        this.consumerToken = consumerToken;
        this.redirectUri = redirectUri;
    }

    private void doWithToken(final boolean revoke, final Task<OAuthToken, ErrorInfo> task) {
        if (!revoke) {
            final String token = tokenManager.getTokenAsString(TW_TOKEN);
            final String tokenSecret = tokenManager.getTokenAsString(TW_TOKEN_SECRET);
            if (token != null && tokenSecret != null) {
                taskManager.executeAsync(task, new OAuthToken(token, tokenSecret));
                return;
            }
        }

        taskManager.executeAsync(new Task<Object, Pair<ErrorInfo, OAuthToken>>() {
            public Pair<ErrorInfo, OAuthToken> execute(Object param) {
                final String url = "https://api.twitter.com/oauth/request_token";
                final HttpPost request = new HttpPost(url);
                oAuthService.authorize(request, consumerToken, OAuthToken.EMPTY,
                        new OAuthHeader().callbackUri(redirectUri));
                try {
                    final Map<String,String> responseValues = httpExecutor.execute(request, HttpFormat.form());
                    final boolean isCallbackConfirmed = Boolean.parseBoolean(
                            responseValues.get("oauth_callback_confirmed"));
                    if (!isCallbackConfirmed) {
                        logger.error("Callback \"" + redirectUri + "\" wasn't confirmed");
                        return Pair.keyOnly(
                                ErrorInfo.create(Error.URI_NOT_CONFIRMED).withDetail("redirect.uri", redirectUri));
                    }
                    final String token = responseValues.get("oauth_token");
                    final String tokenSecret = responseValues.get("oauth_token_secret");
                    return Pair.makePair(ErrorInfo.success(), new OAuthToken(token, tokenSecret));

                } catch (GenericException e) {
                    logger.error("Can't obtain request token", e);
                    return Pair.keyOnly(ErrorInfo.create(CommonError.AUTH_ERROR).withThrowable(e));
                }
            }

            public void onFinish(Pair<ErrorInfo, OAuthToken> result) {
                if (result.key.isError()) {
                    task.onFinish(result.key);
                }
                showAuthDialog(revoke, result.value, task);
            }
        }, null);
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

                dialog.dismiss();

                final Uri uri = Uri.parse(url);
                final String returnedToken = uri.getQueryParameter("oauth_token");
                if (!requestToken.getKey().equals(returnedToken)) {
                    logger.error("Returned oauth_token mismatch. Either User cancelled authentication. " +
                            "Expected: " + requestToken.getKey() +
                            "; But received: " + returnedToken);
                    task.onFinish(ErrorInfo.create(returnedToken == null
                            ? CommonError.USER_CANCELLED
                            : Error.REQUEST_TOKEN_MISMATCH));
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
        taskManager.executeAsync(new Task<OAuthToken, ErrorInfo>() {
            public ErrorInfo execute(final OAuthToken requestToken) {

                final HttpPost httpPost = new HttpPost("https://api.twitter.com/oauth/access_token");

                final List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
                requestParams.add(new BasicNameValuePair("oauth_verifier", verifier));
                httpPost.setEntity(WebUtils.createUrlEncodedFormEntity(requestParams));

                oAuthService.authorize(httpPost, consumerToken, requestToken, new OAuthHeader());
                try {
                    final Map<String,String> responseParams = httpExecutor.execute(httpPost, HttpFormat.form());
                    final String accessToken = responseParams.get("oauth_token");
                    final String accessTokenSecret = responseParams.get("oauth_token_secret");
                    if (accessToken == null || accessTokenSecret == null) {
                        logger.error("Access token or access token secret wasn't returned");
                        return ErrorInfo.create(Error.ACCESS_TOKEN_MISSING);
                    }
                    tokenManager.storeToken(TW_TOKEN, new Token(accessToken, Token.NEVER_EXPIRES));
                    tokenManager.storeToken(TW_TOKEN_SECRET, new Token(accessTokenSecret, Token.NEVER_EXPIRES));
                    return task.execute(new OAuthToken(accessToken, accessTokenSecret));

                } catch (GenericException e) {
                    logger.error("Can't obtain access token", e);
                    return ErrorInfo.create(CommonError.AUTH_ERROR).withThrowable(e);
                }
            }

            public void onFinish(ErrorInfo result) {
                task.onFinish(result);
            }
        }, requestToken);
    }

    public void share(final ShareParams shareParams) {
        doWithToken(shareParams.doRevoke(), new Task<OAuthToken, ErrorInfo>() {
            public ErrorInfo execute(final OAuthToken token) {
                final HttpPost httpPost = new HttpPost("https://api.twitter.com/1.1/statuses/update.json");
                final List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
                requestParams.add(new BasicNameValuePair("status", shareParams.getText()));
                httpPost.setEntity(WebUtils.createUrlEncodedFormEntity(requestParams));
                oAuthService.authorize(httpPost, consumerToken, token, new OAuthHeader());
                try {
                    final HttpResponse response = httpExecutor.execute(httpPost);
                    final int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != HttpStatus.SC_OK) {
                        return ErrorInfo.create(statusCode == HttpStatus.SC_FORBIDDEN
                                ? Error.DUPLICATE_STATUS_ERROR
                                : Error.UPDATE_STATUS_ERROR).withDetail("status", statusCode);
                    }

                    final JSONObject jsonResponse = HttpFormat.<JSONObject>json().convert(response.getEntity());
                    logger.debug("JSON response received: " + jsonResponse.toString());

                } catch (GenericException e) {
                    logger.error("Execute POST request failed", e);
                    return ErrorInfo.create(Error.UPDATE_STATUS_ERROR).withThrowable(e);
                }
                return ErrorInfo.success();
            }

            public void onFinish(ErrorInfo error) {
                shareParams.invokeFinishAction(error);
            }
        });
    }
}
