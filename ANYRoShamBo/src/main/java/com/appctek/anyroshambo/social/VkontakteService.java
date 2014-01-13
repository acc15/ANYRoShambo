package com.appctek.anyroshambo.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.appctek.anyroshambo.services.DateTimeService;
import com.appctek.anyroshambo.social.auth.OAuthToken;
import com.appctek.anyroshambo.social.auth.TokenManager;
import com.google.inject.Inject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-29-12
 */
public class VkontakteService implements SocialNetworkService {

    private static final long APP_ID = 4087551; // look at https://vk.com/editapp?id=4087551&section=options
    private static final String SERVICE_ID = "vkontakte";

    private static final String API_VERSION = "5.5";
    private static final String DISPLAY_TYPE = "mobile";
    private static final String REDIRECT_URL = "https://oauth.vk.com/blank.html";
    private static final String WALL_ACCESS_RIGHT = "wall";

    private static final Logger logger = LoggerFactory.getLogger(VkontakteService.class);

    private Activity activity;
    private TokenManager tokenManager;
    private DateTimeService dateTimeService;
    private HttpClient httpClient;
    private Handler handler;

    @Inject
    public VkontakteService(Activity activity, TokenManager tokenManager, DateTimeService dateTimeService, HttpClient httpClient) {
        this.activity = activity;
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

    private static class ContentType {

        private String type;
        private String subtype;
        private Map<String,String> params;

        public String getCharset() {
            return params.get("charset");
        }

        public String getType() {
            return type;
        }

        public String getSubtype() {
            return subtype;
        }

        public static ContentType valueOf(String val) {

            final String[] values = val.split(";");
            final String[] typeVals = values[0].split("/", 2);
            final String type = typeVals[0];
            final String subtype = typeVals[1];
            for (String v: values) {
                final String[] sv = v.trim().split("=", 2);
                final String key = sv[0];
                //final String val = sv[1];
                //if (val.sta)
            }
            return null;

        }

    }

    public void shareText(boolean forceAuth, final String text) {
        authenticate(forceAuth, new OAuthListener() {
            public void onLogin(final OAuthToken token) {
                new AsyncTask<Object,Object,Object>() {

                    @Override
                    protected Object doInBackground(Object... params) {

                        final String uri = Uri.parse("https://api.vk.com/method/wall.post").buildUpon().
                                appendQueryParameter("message", text).
                                appendQueryParameter("access_token", token.getToken()).
                                build().toString();

                        final HttpPost post = new HttpPost(uri);
                        try {

                            final HttpResponse response = httpClient.execute(post);
                            final HttpEntity entity = response.getEntity();

                            // TODO parse ContentType and use charset
                            final String contentType = entity.getContentType().getValue();

                            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            entity.writeTo(byteArrayOutputStream);

                            final String responseString = byteArrayOutputStream.toString();
                            logger.info("Server responded with body: " + responseString);

                            //new JSONObject()

                            // TODO analyze responseString to get any error details
                            final int statusCode = response.getStatusLine().getStatusCode();
                            if (statusCode == HttpStatus.SC_OK) {
                                logger.info("Message has been posted to wall");
                                showToast("Success");
                            } else {
                                logger.error("ResponseMessage has been posted to wall");
                                showToast("Error");
                            }

                        } catch (IOException e) {
                            logger.error("Can't post message to wall", e);
                            showToast("Error");
                        }
                        return null;
                    }
                }.execute();
            }

            public void onCancel() {
            }
        });

        // TODO open WebView, user authorizes. Server redirects client to url
        // which contains access_token and amount of seconds until taken may be used:
        // http://REDIRECT_URI#access_token= 533bacf01e11f55b536a565b57531ad114461ae8736d6506a3&expires_in=86400&user_id=8492
        /*
        https://oauth.vk.com/authorize?client_id=" + APP_ID + &scope=wall &redirect_uri=https://oauth.vk.com/blank.html&display=mobile&v=5.5&response_type=token
         */

        // POST request to
        // http://vk.com/dev/wall.post
        // https://api.vk.com/method/postw?'''PARAMETERS'''&access_token='''ACCESS_TOKEN''&lang=en|ru&v=API_VERSION


    }

    private void showToast(final String message) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, message, 3000).show();
            }
        });
    }

    private static Map<String,String> parseHashParameters(String fragment) {
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

    private void showLoginDialog(final boolean revoke, final OAuthListener listener) {
        final WebView wv = new WebView(activity);

        final String url = "https://oauth.vk.com/authorize?client_id=" + APP_ID +
                "&scope=" + WALL_ACCESS_RIGHT +
                "&redirect_uri=" + REDIRECT_URL +
                "&display=" + DISPLAY_TYPE +
                "&v=" + API_VERSION +
                "&response_type=token" + (revoke ? "&revoke=1" : "");

        wv.loadUrl(url);
        final AlertDialog ad = new AlertDialog.Builder(activity).setView(wv).create();
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

                final Map<String,String> hashParams = parseHashParameters(uri.getFragment());
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


}
