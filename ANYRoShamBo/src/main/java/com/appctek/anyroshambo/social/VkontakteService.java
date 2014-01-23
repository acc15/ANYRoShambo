package com.appctek.anyroshambo.social;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.widget.Toast;
import com.appctek.anyroshambo.R;
import com.appctek.anyroshambo.social.auth.*;
import com.appctek.anyroshambo.social.task.Task;
import com.appctek.anyroshambo.social.task.TaskManager;
import com.appctek.anyroshambo.social.token.Token;
import com.appctek.anyroshambo.social.token.TokenManager;
import com.appctek.anyroshambo.util.JSONUtils;
import com.appctek.anyroshambo.util.WebUtils;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-29-12
 */
public class VkontakteService implements SocialNetworkService {

    public static final String EMPTY_RESPONSE = "empty.response";
    public static final String RESPONSE_DETAIL = "response";
    public static final String USER_CANCELLED = "user.cancelled";

    private static final Logger logger = LoggerFactory.getLogger(VkontakteService.class);
    private static final String VK_TOKEN = "vk";
    private static final String API_VERSION = "5.5";

    private final Context context;
    private final TokenManager tokenManager;
    private final TaskManager taskManager;
    private final HttpClient httpClient;

    private final String appId;
    private final String redirectUri;


    @Inject
    public VkontakteService(Context context,
                            TokenManager tokenManager,
                            TaskManager taskManager,
                            HttpClient httpClient,
                            @Named("vkAppId") String appId,
                            @Named("vkRedirectUri") String redirectUri) {
        this.context = context;
        this.tokenManager = tokenManager;
        this.httpClient = httpClient;
        this.taskManager = taskManager;
        this.appId = appId;
        this.redirectUri = redirectUri;
    }

    public void shareText(boolean revoke, final String text) {
        doWithToken(revoke, new Task<Token, ErrorInfo>() {
            public ErrorInfo execute(Token param) {
                return postOnWall(param, text);
            }

            public void onFinish(ErrorInfo error) {
                if (error.is(USER_CANCELLED)) {
                    return;
                }
                Toast.makeText(context,
                    error.isError() ? R.string.share_success : R.string.share_error,
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private ErrorInfo postOnWall(Token token, String message) {
        final String url = Uri.parse("https://api.vk.com/method/wall.post").buildUpon().
                appendQueryParameter("message", message).
                appendQueryParameter("access_token", token.getToken()).
                build().toString();
        try {
            final JSONObject jsonObject = JSONUtils.parseJSON(WebUtils.executeRequestString(httpClient, new HttpPost(url)));
            if (!jsonObject.has("response")) {
                return ErrorInfo.create(EMPTY_RESPONSE).withDetail(RESPONSE_DETAIL, jsonObject);
            }
            final String postId = jsonObject.getJSONObject("response").getString("post_id");
            logger.info("Post has been created with id " + postId);
            return ErrorInfo.success();
        } catch (JSONException e) {
            logger.error("Error occurred while executing JSON POST request", e);
            return ErrorInfo.create().fromThrowable(e);
        } catch (IOException e) {
            logger.error("I/O error occurred", e);
            return ErrorInfo.create().fromThrowable(e);
        }
    }

    private void doWithToken(final boolean revoke, final Task<Token, ErrorInfo> task) {
        if (!revoke) {
            final Token token = tokenManager.getToken(VK_TOKEN);
            if (token != null) {
                taskManager.executeAsync(task, token);
                return;
            }
        } else {
            tokenManager.revokeToken(VK_TOKEN);
        }

        final Uri.Builder uriBuilder = Uri.parse("https://oauth.vk.com/authorize").buildUpon().
                appendQueryParameter("client_id", appId).
                appendQueryParameter("scope", "wall").
                appendQueryParameter("redirect_uri", redirectUri).
                appendQueryParameter("display", "mobile").
                appendQueryParameter("v", API_VERSION).
                appendQueryParameter("response_type", "token");
        if (revoke) {
            uriBuilder.appendQueryParameter("revoke", "1");
        }

        //https://oauth.vk.com/blank.html#error=access_denied&error_reason=user_denied&error_description=User denied your request
        //https://oauth.vk.com/blank.html#
        // access_token=09858b7944d137f44497b49206bbfb3b48543577b7c50ea9094ad868a07fc21a169622998801f85e9b23a&
        // expires_in=86400 // in seconds
        // user_id=1114703
        WebUtils.showWebViewDialog(context, uriBuilder.build().toString(), new WebUtils.UrlHandler() {
            public boolean handleUrl(DialogInterface dialog, String url) {
                if (!url.startsWith(redirectUri)) {
                    return false;
                }

                final Uri uri = Uri.parse(url);

                final String error = uri.getQueryParameter("error");
                if (error != null) {
                    final String errorDescription = uri.getQueryParameter("error_description");
                    logger.info("Vkontakte authentication cancelled (returned error: " + error +
                            ") with description: " + errorDescription);
                    dialog.cancel();
                    task.onFinish(ErrorInfo.create(USER_CANCELLED).withDetail("error_description", errorDescription));
                    return true;
                }

                final Map<String, String> hashParams = WebUtils.parseUriFragmentParameters(uri.getFragment());
                final String accessToken = hashParams.get("access_token");
                final String expiresInString = hashParams.get("expires_in");
                final long expiresIn = Long.parseLong(expiresInString);

                final Token token = tokenManager.createToken(accessToken, expiresIn, TimeUnit.SECONDS);
                tokenManager.storeToken(VK_TOKEN, token);
                dialog.dismiss();

                taskManager.executeAsync(task, token);
                return true;
            }
        });
    }

}
