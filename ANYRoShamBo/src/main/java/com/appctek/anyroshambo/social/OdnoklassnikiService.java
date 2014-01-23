package com.appctek.anyroshambo.social;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;
import com.appctek.anyroshambo.R;
import com.appctek.anyroshambo.social.auth.ErrorInfo;
import com.appctek.anyroshambo.social.auth.Token;
import com.appctek.anyroshambo.social.auth.TokenManager;
import com.appctek.anyroshambo.util.HexUtils;
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
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * OAuth help page:
 * http://apiok.ru/wiki/pages/viewpage.action?pageId=42476652
 *
 * @author Vyacheslav Mayorov
 * @since 2014-14-01
 */
public class OdnoklassnikiService implements SocialNetworkService {

    public static final String AUTH_ERROR = "auth.error";
    public static final String RESPONSE_DETAIL = "response";

    public static final String STREAM_PUBLISH_ERROR = "stream.publish.error";


    private static final Logger logger = LoggerFactory.getLogger(OdnoklassnikiService.class);

    private static final String OK_TOKEN = "ok";
    private static final String OK_REFRESH_TOKEN = "ok.refresh";
    public static final String METHOD_SUFFIX = "Odnoklassniki API";

    private final Context context;
    private final TokenManager tokenManager;
    private final HttpClient httpClient;

    private final String appId;
    private final String publicKey;
    private final String secretKey;
    private final String redirectUri;

    @Inject
    public OdnoklassnikiService(Context context, TokenManager tokenManager, HttpClient httpClient,
                                @Named("okAppId") String appId,
                                @Named("okPublicKey") String publicKey,
                                @Named("okSecretKey") String secretKey,
                                @Named("okRedirectUri") String redirectUri) {
        this.context = context;
        this.tokenManager = tokenManager;
        this.httpClient = httpClient;
        this.appId = appId;
        this.secretKey = secretKey;
        this.publicKey = publicKey;
        this.redirectUri = redirectUri;
    }

    private void doWithAuthParams(boolean revoke, final AsyncTask<AuthParams, ?, ?> task) {

        if (revoke) {

            tokenManager.revokeToken(OK_TOKEN);
            tokenManager.revokeToken(OK_REFRESH_TOKEN);

        } else {

            final Token accessToken = tokenManager.getToken(OK_TOKEN);
            if (accessToken != null) {
                task.execute(AuthParams.withAccessToken(accessToken));
                return;
            }

            final Token refreshToken = tokenManager.getToken(OK_REFRESH_TOKEN);
            if (refreshToken != null) {
                task.execute(AuthParams.withRefreshToken(refreshToken));
                return;
            }

        }

        final String url = Uri.parse("http://www.odnoklassniki.ru/oauth/authorize").buildUpon().
                appendQueryParameter("client_id", appId).
                appendQueryParameter("response_type", "code").
                appendQueryParameter("layout", "m").
                appendQueryParameter("scope", "VALUABLE_ACCESS").
                appendQueryParameter("redirect_uri", redirectUri).
                build().toString();

        WebUtils.showWebViewDialog(context, url, new WebUtils.UrlHandler() {
            public boolean handleUrl(DialogInterface dialog, String url) {
                if (!url.startsWith(redirectUri)) {
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

    private String calculateSignature(SortedMap<String, String> params, Token accessToken) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            stringBuilder.append(param.getKey()).append('=').append(param.getValue());
        }

        final String authMd5 = HexUtils.md5Hex(accessToken.getToken() + secretKey);
        stringBuilder.append(authMd5);

        return HexUtils.md5Hex(stringBuilder.toString());
    }

    public void shareText(boolean revoke, final String text) {

        // http://www.odnoklassniki.ru/oauth/authorize?client_id={clientId}&scope={scope}&response_type={responseType}&redirect_uri={redirectUri}
        doWithAuthParams(revoke, new AsyncTask<AuthParams, Object, ErrorInfo>() {
            @Override
            protected ErrorInfo doInBackground(AuthParams... params) {
                final ErrorInfo errorInfo = ErrorInfo.success();
                final Token accessToken = authenticate(params[0], errorInfo);
                if (errorInfo.isError()) {
                    return errorInfo;
                }

                //http://api.odnoklassniki.ru/fb.do?method=stream.publish
                final String apiMethod = "stream.publish";
                try {
                    final JSONObject attachment = new JSONObject();
                    attachment.put("caption", text);

                    final TreeMap<String, String> sortedParams = new TreeMap<String, String>();
                    sortedParams.put("method", apiMethod);
                    sortedParams.put("message", "покрутил рулетку и получил");
                    sortedParams.put("attachment", attachment.toString());
                    sortedParams.put("application_key", publicKey);

                    final String signature = calculateSignature(sortedParams, accessToken);
                    sortedParams.put("sig", signature);
                    sortedParams.put("access_token", accessToken.getToken());

                    final String uri = WebUtils.appendQueryParameters(
                            Uri.parse("http://api.odnoklassniki.ru/fb.do").buildUpon(), sortedParams).
                            build().toString();

                    final Object reply = JSONUtils.parseJSON(WebUtils.executeRequestString(httpClient, new HttpPost(uri)));
                    if (reply instanceof JSONObject) {
                        final JSONObject jsonReply = (JSONObject) reply;
                        // error sample:
                        // {"error_data":null,"error_code":104,"error_msg":"PARAM_SIGNATURE : No signature specified"}
                        if (jsonReply.has("error_code")) {
                            final Iterator keyIter = jsonReply.keys();
                            errorInfo.withCode(STREAM_PUBLISH_ERROR);
                            while (keyIter.hasNext()) {
                                final String key = (String) keyIter.next();
                                final String value = jsonReply.getString(key);
                                errorInfo.withDetail(key, value);
                            }
                            logger.error("Error returned from server which executing " + apiMethod + ": " + jsonReply);
                            return errorInfo;
                        }



                    }
                    System.out.println(reply);

                } catch (JSONException e) {
                    logger.error("Can't execute " + apiMethod + " in " + METHOD_SUFFIX, e);
                    return errorInfo.fromThrowable(e);
                } catch (IOException e) {
                    logger.error("I/O error occurred while executing " + apiMethod + " in " + METHOD_SUFFIX, e);
                    return errorInfo.fromThrowable(e);
                }
                return errorInfo;
            }

            @Override
            protected void onPostExecute(ErrorInfo result) {
                Toast.makeText(context,
                        result.isError() ? R.string.share_success : R.string.share_error,
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    private Token authenticate(AuthParams authParams, ErrorInfo errorInfo) {

        if (authParams.getAccessToken() != null) {
            return authParams.getAccessToken();
        }

        try {

            final JSONObject jsonObject;
            if (authParams.getRefreshToken() != null) {
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
                        appendQueryParameter("client_id", appId).
                        appendQueryParameter("client_secret", secretKey).
                        build().toString();
                jsonObject = JSONUtils.parseJSON(WebUtils.executeRequestString(httpClient, new HttpPost(url)));
            } else {
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
                        appendQueryParameter("redirect_uri", redirectUri).
                        appendQueryParameter("grant_type", "authorization_code").
                        appendQueryParameter("client_id", appId).
                        appendQueryParameter("client_secret", secretKey).
                        build().toString();
                jsonObject = JSONUtils.parseJSON(WebUtils.executeRequestString(httpClient, new HttpPost(url)));
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
                errorInfo.withCode(AUTH_ERROR).withDetail(RESPONSE_DETAIL, jsonObject);
                return null;
            }

            final Token accessToken = tokenManager.createToken(accessTokenStr, 30, TimeUnit.MINUTES);
            tokenManager.storeToken(OK_TOKEN, accessToken);

            if (jsonObject.has("refresh_token")) {
                final String refreshTokenStr = jsonObject.getString("refresh_token");
                final Token refreshToken = tokenManager.createToken(refreshTokenStr, 30, TimeUnit.DAYS);
                tokenManager.storeToken(OK_REFRESH_TOKEN, refreshToken);
            }

            return accessToken;

        } catch (IOException e) {
            logger.error("I/O error occurred during authentication in " + METHOD_SUFFIX);
            errorInfo.fromThrowable(e);
            return null;
        } catch (JSONException e) {
            logger.error("Error occurred during authentication in " + METHOD_SUFFIX, e);
            errorInfo.fromThrowable(e);
            return null;
        }
    }


    private static class AuthParams {

        private Token accessToken;
        private Token refreshToken;
        private String accessCode;

        private AuthParams() {
        }

        public static AuthParams withAccessToken(Token accessToken) {
            final AuthParams authParams = new AuthParams();
            authParams.accessToken = accessToken;
            return authParams;
        }

        public static AuthParams withRefreshToken(Token refreshToken) {
            final AuthParams authParams = new AuthParams();
            authParams.refreshToken = refreshToken;
            return authParams;
        }

        public static AuthParams withAccessCode(String accessCode) {
            final AuthParams authParams = new AuthParams();
            authParams.accessCode = accessCode;
            return authParams;
        }

        public Token getAccessToken() {
            return accessToken;
        }

        public Token getRefreshToken() {
            return refreshToken;
        }

        public String getAccessCode() {
            return accessCode;
        }
    }

}
