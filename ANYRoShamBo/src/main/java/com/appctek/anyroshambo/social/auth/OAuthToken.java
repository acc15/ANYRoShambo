package com.appctek.anyroshambo.social.auth;

/**
* @author Vyacheslav Mayorov
* @since 2014-13-01
*/
public class OAuthToken {

    private String token;
    private long expiresAfter;

    public OAuthToken(String token, long expiresAfter) {
        this.token = token;
        this.expiresAfter = expiresAfter;
    }

    public String getToken() {
        return token;
    }

    public long getExpiresAfter() {
        return expiresAfter;
    }
}
