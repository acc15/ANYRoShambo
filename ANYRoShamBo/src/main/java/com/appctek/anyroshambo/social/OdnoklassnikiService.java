package com.appctek.anyroshambo.social;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import com.appctek.anyroshambo.social.auth.ErrorInfo;
import com.appctek.anyroshambo.social.token.Token;
import com.appctek.anyroshambo.social.token.TokenManager;
import com.appctek.anyroshambo.util.Action;
import com.appctek.anyroshambo.util.GenericException;
import com.appctek.anyroshambo.util.HexUtils;
import com.appctek.anyroshambo.util.WebUtils;
import com.appctek.anyroshambo.util.http.HttpExecutor;
import com.appctek.anyroshambo.util.http.HttpFormat;
import com.appctek.anyroshambo.util.task.Task;
import com.appctek.anyroshambo.util.task.TaskManager;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static enum Error {
        STREAM_PUBLISH_ERROR
    }

    public static final String RESPONSE_DETAIL = "response";

    private static final Logger logger = LoggerFactory.getLogger(OdnoklassnikiService.class);

    private static final String OK_TOKEN = "ok";
    private static final String OK_REFRESH_TOKEN = "ok.refresh";
    public static final String METHOD_SUFFIX = "Odnoklassniki API";

    private final Context context;
    private final TokenManager tokenManager;
    private final TaskManager taskManager;
    private final HttpExecutor httpExecutor;

    private final String appId;
    private final String publicKey;
    private final String secretKey;
    private final String redirectUri;

    @Inject
    public OdnoklassnikiService(Context context,
                                TokenManager tokenManager, TaskManager taskManager, HttpExecutor httpExecutor,
                                @Named("okAppId") String appId,
                                @Named("okPublicKey") String publicKey,
                                @Named("okSecretKey") String secretKey,
                                @Named("okRedirectUri") String redirectUri) {
        this.context = context;
        this.tokenManager = tokenManager;
        this.taskManager = taskManager;
        this.httpExecutor = httpExecutor;
        this.appId = appId;
        this.secretKey = secretKey;
        this.publicKey = publicKey;
        this.redirectUri = redirectUri;
    }

    private void doWithToken(boolean revoke, final Task<String, ErrorInfo> task) {

        if (revoke) {

            tokenManager.revokeToken(OK_TOKEN);
            tokenManager.revokeToken(OK_REFRESH_TOKEN);

        } else {

            final String accessToken = tokenManager.getTokenAsString(OK_TOKEN);
            if (accessToken != null) {
                taskManager.executeAsync(task, accessToken);
                return;
            }

            final String refreshToken = tokenManager.getTokenAsString(OK_REFRESH_TOKEN);
            if (refreshToken != null) {
                authAsync(null, refreshToken, task);
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
                    task.onFinish(ErrorInfo.create(CommonError.USER_CANCELLED).withDetail("error", error));
                    return true;
                }

                dialog.dismiss();

                final String code = uri.getQueryParameter("code");
                authAsync(code, null, task);
                return true;
            }
        });

    }

    private String calculateSignature(SortedMap<String, String> params, String accessToken) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            stringBuilder.append(param.getKey()).append('=').append(param.getValue());
        }

        final String authMd5 = HexUtils.md5Hex(accessToken + secretKey);
        stringBuilder.append(authMd5);

        return HexUtils.md5Hex(stringBuilder.toString());
    }

    public void shareText(boolean revoke, final String text, final Action<ErrorInfo> errorHandler) {

        // http://www.odnoklassniki.ru/oauth/authorize?client_id={clientId}&scope={scope}&response_type={responseType}&redirect_uri={redirectUri}
        doWithToken(revoke, new Task<String, ErrorInfo>() {
            public ErrorInfo execute(String token) {

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

                    final String signature = calculateSignature(sortedParams, token);
                    sortedParams.put("sig", signature);
                    sortedParams.put("access_token", token);

                    final String uri = WebUtils.appendQueryParameters(
                            Uri.parse("http://api.odnoklassniki.ru/fb.do").buildUpon(), sortedParams).
                            build().toString();

                    final Object reply = httpExecutor.execute(new HttpPost(uri), HttpFormat.json());
                    if (reply instanceof JSONObject) {
                        final JSONObject jsonReply = (JSONObject) reply;
                        // error sample:
                        // {"error_data":null,"error_code":104,"error_msg":"PARAM_SIGNATURE : No signature specified"}
                        if (jsonReply.has("error_code")) {
                            logger.error("Error returned from server which executing " + apiMethod + ": " + jsonReply);
                            return ErrorInfo.create(Error.STREAM_PUBLISH_ERROR).withDetail(RESPONSE_DETAIL, jsonReply);
                        }


                    }
                    System.out.println(reply);

                } catch (JSONException e) {
                    logger.error("Can't fetch json data", e);
                    return ErrorInfo.create(Error.STREAM_PUBLISH_ERROR).withThrowable(e);
                } catch (GenericException e) {
                    logger.error("Publish to stream failed", e);
                    return ErrorInfo.create(Error.STREAM_PUBLISH_ERROR).withThrowable(e);
                }
                return ErrorInfo.success();
            }

            public void onFinish(ErrorInfo error) {
                errorHandler.execute(error);
            }
        });

    }

    private void authAsync(final String accessCode, final String refreshToken, final Task<String,ErrorInfo> task) {
        taskManager.executeAsync(new Task<String, ErrorInfo>() {
            public ErrorInfo execute(String param) {
                return authenticate(accessCode, refreshToken, task);
            }

            public void onFinish(ErrorInfo result) {
                task.onFinish(result);
            }
        }, null);
    }

    private ErrorInfo authenticate(String accessCode, String refreshToken, Task<String,ErrorInfo> task) {
        try {

            final JSONObject jsonObject;
            if (refreshToken != null) {
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
                        appendQueryParameter("refresh_token", refreshToken).
                        appendQueryParameter("grant_type", "refresh_token").
                        appendQueryParameter("client_id", appId).
                        appendQueryParameter("client_secret", secretKey).
                        build().toString();
                jsonObject = httpExecutor.execute(new HttpPost(url), HttpFormat.<JSONObject>json());
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
                        appendQueryParameter("code", accessCode).
                        appendQueryParameter("redirect_uri", redirectUri).
                        appendQueryParameter("grant_type", "authorization_code").
                        appendQueryParameter("client_id", appId).
                        appendQueryParameter("client_secret", secretKey).
                        build().toString();
                jsonObject = httpExecutor.execute(new HttpPost(url), HttpFormat.<JSONObject>json());
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
                return ErrorInfo.create(CommonError.AUTH_ERROR).withDetail(RESPONSE_DETAIL, jsonObject);
            }

            final Token accessToken = tokenManager.createToken(accessTokenStr, 30, TimeUnit.MINUTES);
            tokenManager.storeToken(OK_TOKEN, accessToken);
            if (jsonObject.has("refresh_token")) {
                final String refreshTokenStr = jsonObject.getString("refresh_token");
                final Token newRefreshToken = tokenManager.createToken(refreshTokenStr, 30, TimeUnit.DAYS);
                tokenManager.storeToken(OK_REFRESH_TOKEN, newRefreshToken);
            }
            return task.execute(accessTokenStr);

        } catch (GenericException e) {
            logger.error("Can't authenticate", e);
            return ErrorInfo.create(CommonError.AUTH_ERROR).withThrowable(e);
        } catch (JSONException e) {
            logger.error("Error occurred during authentication in " + METHOD_SUFFIX, e);
            return ErrorInfo.create(CommonError.AUTH_ERROR).withThrowable(e);
        }
    }

}
