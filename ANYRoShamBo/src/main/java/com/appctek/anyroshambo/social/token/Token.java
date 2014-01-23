package com.appctek.anyroshambo.social.token;

/**
* @author Vyacheslav Mayorov
* @since 2014-13-01
*/
public class Token {

    public static final long NEVER_EXPIRES = -1;

    private final String token;
    private final long expiresAfter;

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
