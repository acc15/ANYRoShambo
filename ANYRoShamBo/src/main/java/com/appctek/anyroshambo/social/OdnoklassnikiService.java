package com.appctek.anyroshambo.social;

/**
 * OAuth help page:
 * http://apiok.ru/wiki/pages/viewpage.action?pageId=42476652
 *
 *
 * @author Vyacheslav Mayorov
 * @since 2014-14-01
 */
public class OdnoklassnikiService implements SocialNetworkService {

    private static final String APP_ID = "216398080";
    private static final String SERVICE_ID = "odnoklassniki";
    private static final String SECRET_CODE = "83B562785858040AF1E5DF41";

    public void shareText(boolean forceAuth, String text) {

        // http://www.odnoklassniki.ru/oauth/authorize?client_id={clientId}&scope={scope}&response_type={responseType}&redirect_uri={redirectUri}

        //http://api.odnoklassniki.ru/oauth/token.do
        //
        //Параметры
        //
        //code - код авторизации, полученный в ответном адресе URL пользователя
        //redirect_uri - тот же URI для переадресации, который был указан при первом вызове
        //grant_type - _на данный момент поддерживается только код авторизации authorization_code
        //client_id - идентификатор приложения
        //client_secret - секретный ключ приложения

        // TODO implement..

    }
}
