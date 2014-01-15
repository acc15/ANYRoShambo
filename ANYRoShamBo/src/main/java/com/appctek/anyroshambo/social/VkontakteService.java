package com.appctek.anyroshambo.social;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;
import com.appctek.anyroshambo.R;
import com.appctek.anyroshambo.social.auth.OAuthToken;
import com.appctek.anyroshambo.social.auth.TokenManager;
import com.appctek.anyroshambo.util.WebUtils;
import com.google.inject.Inject;
import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-29-12
 */
public class VkontakteService implements SocialNetworkService {

    private static final Logger logger = LoggerFactory.getLogger(VkontakteService.class);

    private static final String APP_ID = "4087551"; // look at https://vk.com/editapp?id=4087551&section=options
    private static final String VK_TOKEN = "vkontakte";

    private static final String API_VERSION = "5.5";
    private static final String REDIRECT_URL = "https://oauth.vk.com/blank.html";

    private Context context;
    private TokenManager tokenManager;
    private HttpClient httpClient;

    @Inject
    public VkontakteService(Context context, TokenManager tokenManager, HttpClient httpClient) {
        this.context = context;
        this.tokenManager = tokenManager;
        this.httpClient = httpClient;
    }

    public void shareText(boolean revoke, final String text) {
        doWithToken(revoke, new AsyncTask<OAuthToken, Object, Boolean>() {
            @Override
            protected Boolean doInBackground(OAuthToken... token) {
                return postOnWall(token[0], text);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                Toast.makeText(context,
                        result ? R.string.vk_share_success : R.string.vk_share_error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean postOnWall(OAuthToken token, String message) {
        final String url = Uri.parse("https://api.vk.com/method/wall.post").buildUpon().
                appendQueryParameter("message", message).
                appendQueryParameter("access_token", token.getToken()).
                build().toString();
        try {

            final JSONObject jsonObject = WebUtils.executePost(httpClient, url);
            if (!jsonObject.has("response")) {
                return false;
            }
            final String postId = jsonObject.getJSONObject("response").getString("post_id");
            logger.info("Post has been created with id " + postId);
            return true;

        } catch (JSONException e) {
            logger.error("Error occured while executing JSON POST request", e);
            return false;
        }
    }

    private void doWithToken(final boolean revoke, final AsyncTask<OAuthToken, Object, Boolean> task) {
        if (!revoke) {
            final OAuthToken token = tokenManager.getToken(VK_TOKEN);
            if (token != null) {
                task.execute(token);
                return;
            }
        } else {
            tokenManager.revokeToken(VK_TOKEN);
        }

        final Uri.Builder uriBuilder = Uri.parse("https://oauth.vk.com/authorize").buildUpon().
                appendQueryParameter("client_id", APP_ID).
                appendQueryParameter("scope", "wall").
                appendQueryParameter("redirect_uri", REDIRECT_URL).
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
                if (!url.startsWith(REDIRECT_URL)) {
                    return false;
                }

                final Uri uri = Uri.parse(url);

                final Map<String, String> hashParams = WebUtils.parseUriFragmentParameters(uri.getFragment());
                final String accessToken = hashParams.get("access_token");
                if (accessToken == null) {
                    dialog.cancel();
                    return true;
                }

                final String expiresInString = hashParams.get("expires_in");
                final long expiresIn = Long.parseLong(expiresInString);

                final OAuthToken token = tokenManager.createToken(accessToken, expiresIn, TimeUnit.SECONDS);
                tokenManager.storeToken(VK_TOKEN, token);
                dialog.dismiss();

                task.execute(token);
                return true;
            }
        });
    }

}
