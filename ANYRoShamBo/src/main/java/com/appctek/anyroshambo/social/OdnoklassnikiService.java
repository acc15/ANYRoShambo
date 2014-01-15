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

import java.util.concurrent.TimeUnit;

/**
 * OAuth help page:
 * http://apiok.ru/wiki/pages/viewpage.action?pageId=42476652
 *
 * @author Vyacheslav Mayorov
 * @since 2014-14-01
 */
public class OdnoklassnikiService implements SocialNetworkService {

    private static final Logger logger = LoggerFactory.getLogger(OdnoklassnikiService.class);

    private static final String APP_ID = "216398080";
    private static final String OK_TOKEN = "ok";
    private static final String OK_REFRESH_TOKEN = "ok.refresh";
    private static final String SECRET_CODE = "83B562785858040AF1E5DF41";
    private static final String REDIRECT_URL = "http://appctek.com/anyroshambo";

    private final Context context;
    private final TokenManager tokenManager;
    private final HttpClient httpClient;

    @Inject
    public OdnoklassnikiService(Context context, TokenManager tokenManager, HttpClient httpClient) {
        this.context = context;
        this.tokenManager = tokenManager;
        this.httpClient = httpClient;
    }

    private void doWithAuthParams(boolean revoke, final AsyncTask<AuthParams, Object, Boolean> task) {

        if (revoke) {

            tokenManager.revokeToken(OK_TOKEN);
            tokenManager.revokeToken(OK_REFRESH_TOKEN);

        } else {

            final OAuthToken accessToken = tokenManager.getToken(OK_TOKEN);
            if (accessToken != null) {
                task.execute(AuthParams.withAccessToken(accessToken));
                return;
            }

            final OAuthToken refreshToken = tokenManager.getToken(OK_REFRESH_TOKEN);
            if (refreshToken != null) {
                task.execute(AuthParams.withRefreshToken(refreshToken));
                return;
            }

        }

        final String url = Uri.parse("http://www.odnoklassniki.ru/oauth/authorize").buildUpon().
                appendQueryParameter("client_id", APP_ID).
                appendQueryParameter("response_type", "code").
                appendQueryParameter("layout", "m").
                appendQueryParameter("scope", "VALUABLE_ACCESS").
                appendQueryParameter("redirect_uri", REDIRECT_URL).
                build().toString();

        WebUtils.showWebViewDialog(context, url, new WebUtils.UrlHandler() {
            public boolean handleUrl(DialogInterface dialog, String url) {
                if (!url.startsWith(REDIRECT_URL)) {
                    return false;
                }

                final Uri uri = Uri.parse(url);
                // example:
                // http://appctek.com/anyroshambo?code=3fc4aa4351.b59f38ab3b83207e2a84832759d49c01ad7668d67fb60aa_4b8035785363786543325665ab3a118e_1389822226

                final String error = uri.getQueryParameter("error");
                if (error != null) {
                    logger.info("Odnoklassniki authentication cancelled (returned error: " + error + ")");
                    dialog.cancel();
                    return true;
                }

                dialog.dismiss();

                final String code = uri.getQueryParameter("code");
                task.execute(AuthParams.withAccessCode(code));
                return true;
            }
        });

    }

    public void shareText(boolean revoke, final String text) {

        // http://www.odnoklassniki.ru/oauth/authorize?client_id={clientId}&scope={scope}&response_type={responseType}&redirect_uri={redirectUri}
        doWithAuthParams(revoke, new AsyncTask<AuthParams, Object, Boolean>() {
            @Override
            protected Boolean doInBackground(AuthParams... params) {
                final OAuthToken accessToken = authenticate(params[0]);
                if (accessToken == null) {
                    return false;
                }

                //http://api.odnoklassniki.ru/fb.do?method=stream.publish
                try {
                    final JSONObject attachment = new JSONObject();
                    attachment.put("caption", text);
                    final String url = Uri.parse("http://api.odnoklassniki.ru/fb.do").buildUpon().
                            appendQueryParameter("access_token", accessToken.getToken()).
                            appendQueryParameter("method", "stream.publish").
                            appendQueryParameter("message", "покрутил рулетку и получил Тест ").
                            appendQueryParameter("attachment", attachment.toString()).
                            build().toString();
                    final JSONObject jsonObject = WebUtils.executePost(httpClient, url);
                    System.out.println(jsonObject);

                } catch (JSONException e) {
                    logger.error("Can't execute stream.publish Odnoklassniki API method", e);
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                Toast.makeText(context,
                        result ? R.string.share_success : R.string.share_error,
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    private OAuthToken authenticate(AuthParams authParams) {

        if (authParams.getAccessToken() != null) {
            return authParams.getAccessToken();
        }

        try {

            final JSONObject jsonObject;
            if (authParams.getAccessCode() != null) {

                //http://api.odnoklassniki.ru/oauth/token.do
                //
                //Параметры
                //code - код авторизации, полученный в ответном адресе URL пользователя
                //redirect_uri - тот же URI для переадресации, который был указан при первом вызове
                //grant_type - _на данный момент поддерживается только код авторизации authorization_code
                //client_id - идентификатор приложения
                //client_secret - секретный ключ приложения

                final String url = Uri.parse("http://api.odnoklassniki.ru/oauth/token.do").buildUpon().
                        appendQueryParameter("code", authParams.getAccessCode()).
                        appendQueryParameter("redirect_uri", REDIRECT_URL).
                        appendQueryParameter("grant_type", "authorization_code").
                        appendQueryParameter("client_id", APP_ID).
                        appendQueryParameter("client_secret", SECRET_CODE).
                        build().toString();
                jsonObject = WebUtils.executePost(httpClient, url);
            } else {

                // refresh token
                //
                //http://api.odnoklassniki.ru/oauth/token.do
                //
                //С параметрами
                //refresh_token - маркер обновления, полученный ранее
                //grant_type = refresh_token
                //client_id - идентификатор приложения
                //client_secret - секретный ключ приложения

                final String url = Uri.parse("http://api.odnoklassniki.ru/oauth/token.do").buildUpon().
                        appendQueryParameter("refresh_token", authParams.getRefreshToken().getToken()).
                        appendQueryParameter("grant_type", "refresh_token").
                        appendQueryParameter("client_id", APP_ID).
                        appendQueryParameter("client_secret", SECRET_CODE).
                        build().toString();
                jsonObject = WebUtils.executePost(httpClient, url);
            }

            //{
            //    access_token: 'kjdhfldjfhgldsjhfglkdjfg9ds8fg0sdf8gsd8fg',
            //            token_type: 'session',
            //        refresh_token: 'klsdjhf0e9dyfasduhfpasdfasdfjaspdkfjp'
            //}

            final String accessTokenStr = jsonObject.getString("access_token");
            if (accessTokenStr == null) {
                logger.error("Authentication error occurred and access token wasn't returned. " +
                        "Analyze server response: " + jsonObject);
                return null;
            }

            final OAuthToken accessToken = tokenManager.createToken(accessTokenStr, 30, TimeUnit.MINUTES);
            tokenManager.storeToken(OK_TOKEN, accessToken);

            final String refreshTokenStr = jsonObject.getString("refresh_token");
            final OAuthToken refreshToken = tokenManager.createToken(refreshTokenStr, 30, TimeUnit.DAYS);
            tokenManager.storeToken(OK_REFRESH_TOKEN, refreshToken);

            return accessToken;

        } catch (JSONException e) {
            logger.error("Error occurred during authentication", e);
            return null;
        }
    }


    private static class AuthParams {

        private OAuthToken accessToken;
        private OAuthToken refreshToken;
        private String accessCode;

        private AuthParams() {
        }

        public static AuthParams withAccessToken(OAuthToken accessToken) {
            final AuthParams authParams = new AuthParams();
            authParams.accessToken = accessToken;
            return authParams;
        }

        public static AuthParams withRefreshToken(OAuthToken refreshToken) {
            final AuthParams authParams = new AuthParams();
            authParams.refreshToken = refreshToken;
            return authParams;
        }

        public static AuthParams withAccessCode(String accessCode) {
            final AuthParams authParams = new AuthParams();
            authParams.accessCode = accessCode;
            return authParams;
        }

        public OAuthToken getAccessToken() {
            return accessToken;
        }

        public OAuthToken getRefreshToken() {
            return refreshToken;
        }

        public String getAccessCode() {
            return accessCode;
        }
    }

}