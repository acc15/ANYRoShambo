package com.appctek.anyroshambo.social.auth;

/**
* @author Vyacheslav Mayorov
* @since 2014-22-01
*/
public class OAuthToken {
    private String key;
    private String secret;

    public OAuthToken(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    public String getKey() {
        return key;
    }

    public String getSecret() {
        return secret;
    }
}
