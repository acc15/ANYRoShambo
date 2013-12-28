package com.appctek.anyroshambo.social;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-29-12
 */
public class VkontakteService implements SocialNetworkService {

    private static final long APP_ID = 4087551; // look at https://vk.com/editapp?id=4087551&section=options
    private static final String VKONTAKTE_API_VERSION = "5.5";
    private static final String DISPLAY_TYPE = "mobile";
    private static final String STANDALONE_REDIRECT_URL = "https://oauth.vk.com/blank.html";
    private static final String WALL_ACCESS_RIGHT = "wall";

    public void shareText(String text) {

        // TODO open WebView, user authorizes. Server redirects client to url
        // which contains access_token and amount of seconds until taken may be used:
        // http://REDIRECT_URI#access_token= 533bacf01e11f55b536a565b57531ad114461ae8736d6506a3&expires_in=86400&user_id=8492
        /*
        https://oauth.vk.com/authorize?
  client_id=" + APP_ID +
 &scope=wall
 &redirect_uri=REDIRECT_URI&
 display=" + DISPLAY_TYPE + "
 &v="+VKONTAKTE_API_VERSION+"
 &response_type=token
         */

        // POST request to
        // http://vk.com/dev/wall.post
        // https://api.vk.com/method/postw?'''PARAMETERS'''&access_token='''ACCESS_TOKEN''&lang=en|ru&v=VKONTAKTE_API_VERSION


    }
}
