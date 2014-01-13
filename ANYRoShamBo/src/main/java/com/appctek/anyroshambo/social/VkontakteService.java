package com.appctek.anyroshambo.social;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.appctek.anyroshambo.R;
import com.appctek.anyroshambo.services.DateTimeService;
import com.appctek.anyroshambo.social.auth.OAuthToken;
import com.appctek.anyroshambo.social.auth.TokenManager;
import com.google.common.net.MediaType;
import com.google.inject.Inject;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-29-12
 */
public class VkontakteService implements SocialNetworkService {

    private static final String APP_ID = "4087551"; // look at https://vk.com/editapp?id=4087551&section=options
    private static final String SERVICE_ID = "vkontakte";

    private static final String API_VERSION = "5.5";
    private static final String DISPLAY_TYPE = "mobile";
    private static final String REDIRECT_URL = "https://oauth.vk.com/blank.html";
    private static final String WALL_ACCESS_RIGHT = "wall";

    private static final Logger logger = LoggerFactory.getLogger(VkontakteService.class);

    private Context context;
    private TokenManager tokenManager;
    private DateTimeService dateTimeService;
    private HttpClient httpClient;
    public static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

    @Inject
    public VkontakteService(Context context, TokenManager tokenManager, DateTimeService dateTimeService, HttpClient httpClient) {
        this.context = context;
        this.tokenManager = tokenManager;
        this.dateTimeService = dateTimeService;
        this.httpClient = httpClient;
    }

    private static interface OAuthListener {
        void onLogin(OAuthToken token);
        void onCancel();
    }

    public void authenticate(boolean forceAuth, OAuthListener listener) {
        if (forceAuth) {
            showLoginDialog(true, listener);
            return;
        }
        final OAuthToken token = tokenManager.getToken(SERVICE_ID);
        if (token == null) {
            showLoginDialog(false, listener);
        } else {
            listener.onLogin(token);
        }
    }

    public void shareText(boolean forceAuth, final String text) {
        authenticate(forceAuth, new OAuthListener() {
            public void onLogin(final OAuthToken token) {
                new AsyncTask<Object,Object,Boolean>() {
                    @Override
                    protected Boolean doInBackground(Object... params) {
                        return postOnWall(token, text);
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        Toast.makeText(context,
                                result ? R.string.vk_share_success : R.string.vk_share_error,
                                Toast.LENGTH_LONG).show();
                    }
                }.execute();
            }

            public void onCancel() {
            }
        });
    }

    private boolean postOnWall(OAuthToken token, String message) {
        final String uri = Uri.parse("https://api.vk.com/method/wall.post").buildUpon().
                appendQueryParameter("message", message).
                appendQueryParameter("access_token", token.getToken()).
                build().toString();

        final HttpPost post = new HttpPost(uri);
        try {

            final HttpResponse response = httpClient.execute(post);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("Server responded with error status: " + statusCode);
                return false;
            }

            final JSONObject jsonObject = parseJSONFromEntity(response.getEntity());
            if (!jsonObject.has("response")) {
                return false;
            }

            final String postId = jsonObject.getJSONObject("response").getString("post_id");
            logger.info("Post has been created with id " + postId);

        } catch (IOException e) {
            logger.error("I/O error during HTTP conversation", e);
            return false;
        } catch (JSONException e) {
            logger.error("Can't parse JSON response", e);
            return false;
        }
        return true;
    }

    private void showLoginDialog(final boolean revoke, final OAuthListener listener) {
        final WebView wv = new WebView(context);

        final Uri.Builder uriBuilder = Uri.parse("https://oauth.vk.com/authorize").buildUpon().
                appendQueryParameter("client_id", APP_ID).
                appendQueryParameter("scope", WALL_ACCESS_RIGHT).
                appendQueryParameter("redirect_uri", REDIRECT_URL).
                appendQueryParameter("display", DISPLAY_TYPE).
                appendQueryParameter("v", API_VERSION).
                appendQueryParameter("response_type", "token");
        if (revoke) {
            uriBuilder.appendQueryParameter("revoke", "1");
        }

        final String url = uriBuilder.build().toString();
        wv.loadUrl(url);
        final AlertDialog ad = new AlertDialog.Builder(context).setView(wv).create();
        final WebViewClient wvc = new WebViewClient() {
            //https://oauth.vk.com/blank.html#error=access_denied&error_reason=user_denied&error_description=User denied your request
            //https://oauth.vk.com/blank.html#
            // access_token=09858b7944d137f44497b49206bbfb3b48543577b7c50ea9094ad868a07fc21a169622998801f85e9b23a&
            // expires_in=86400& // in seconds
            // user_id=1114703
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith(REDIRECT_URL)) {
                    return false;
                }

                final Uri uri = Uri.parse(url);

                final Map<String,String> hashParams = parseUriFragmentParameters(uri.getFragment());
                final String accessToken = hashParams.get("access_token");
                if (accessToken == null) {
                    ad.cancel();
                    listener.onCancel();
                    return true;
                }

                final String expiresInString = hashParams.get("expires_in");
                final long expiresIn = Long.parseLong(expiresInString);
                final long expiresAfter = dateTimeService.getTimeInMillis() + TimeUnit.SECONDS.toMillis(expiresIn);

                final OAuthToken token = new OAuthToken(accessToken, expiresAfter);
                tokenManager.storeToken(SERVICE_ID, token);
                ad.dismiss();

                listener.onLogin(token);
                return true;
            }
        };
        wv.setWebViewClient(wvc);
        ad.show();
    }

    private static Map<String,String> parseUriFragmentParameters(String fragment) {
        final Map<String,String> params = new HashMap<String, String>();
        if (fragment == null) {
            return params;
        }
        final String[] values = fragment.split("&");
        for (String keyVal: values) {
            final String[] kv = keyVal.split("=", 2);
            params.put(kv[0], kv.length > 1 ? kv[1] : null);
        }
        return params;
    }

    private static Charset parseCharset(Header contentTypeHeader) {
        if (contentTypeHeader == null) {
            return DEFAULT_CHARSET;
        }
        final MediaType mediaType = MediaType.parse(contentTypeHeader.getValue());
        return mediaType.charset().or(DEFAULT_CHARSET);
    }

    private static JSONObject parseJSONFromEntity(HttpEntity entity) throws IOException, JSONException {
        final Charset charset = parseCharset(entity.getContentType());
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        entity.writeTo(byteArrayOutputStream);

        final String charsetName = charset.name();
        final String decodedString = byteArrayOutputStream.toString(charsetName);
        logger.info("Server responded with response: {}", decodedString);

        final JSONObject jsonObject = (JSONObject)new JSONTokener(decodedString).nextValue();
        return jsonObject;
    }
}
