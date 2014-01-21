package com.appctek.anyroshambo.social.auth;

/**
* @author Vyacheslav Mayorov
* @since 2014-13-01
*/
public class Token {

    private String token;
    private long expiresAfter;

    public Token(String token, long expiresAfter) {
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
