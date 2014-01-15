package com.appctek.anyroshambo.social;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import com.appctek.anyroshambo.social.auth.OAuthToken;
import com.appctek.anyroshambo.social.auth.TokenManager;
import com.appctek.anyroshambo.util.WebUtils;
import com.google.inject.Inject;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth help page:
 * http://apiok.ru/wiki/pages/viewpage.action?pageId=42476652
 *
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

                final String code = uri.getQueryParameter("code");
                task.execute(AuthParams.withAccessCode(code));
                return true;
            }
        });

    }

    public void shareText(boolean revoke, String text) {

        // http://www.odnoklassniki.ru/oauth/authorize?client_id={clientId}&scope={scope}&response_type={responseType}&redirect_uri={redirectUri}
        doWithAuthParams(revoke, new AsyncTask<AuthParams, Object, Boolean>() {
            @Override
            protected Boolean doInBackground(AuthParams... params) {
                final OAuthToken accessToken = authenticate(params[0]);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                // TODO implement..
                super.onPostExecute(aBoolean);
            }
        });

    }

    private OAuthToken authenticate(AuthParams authParams) {

        //http://api.odnoklassniki.ru/oauth/token.do
        //
        //Параметры
        //code - код авторизации, полученный в ответном адресе URL пользователя
        //redirect_uri - тот же URI для переадресации, который был указан при первом вызове
        //grant_type - _на данный момент поддерживается только код авторизации authorization_code
        //client_id - идентификатор приложения
        //client_secret - секретный ключ приложения

        if (authParams.getAccessCode() != null) {



        } else if (authParams.getRefreshToken() != null) {

        }


        // refresh token
        //
        //http://api.odnoklassniki.ru/oauth/token.do
        //
        //С параметрами
        //refresh_token - маркер обновления, полученный ранее grant_type = refresh_token
        //client_id - идентификатор приложения
        //client_secret - секретный ключ приложения

        return null;
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
