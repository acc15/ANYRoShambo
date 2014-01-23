package com.appctek.anyroshambo.social.auth;

import android.content.SharedPreferences;
import com.appctek.anyroshambo.services.DateTimeService;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-13-01
 */
public class TokenManager {

    public static final String TOKEN = "token";
    public static final String EXPIRES_AFTER = "expiresAfter";

    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);


    private SharedPreferences sharedPreferences;
    private DateTimeService dateTimeService;

    @Inject
    public TokenManager(SharedPreferences sharedPreferences, DateTimeService dateTimeService) {
        this.sharedPreferences = sharedPreferences;
        this.dateTimeService = dateTimeService;
    }

    /**
     * Fetches token from storage.
     * Returns {@code null} if token isn't present in storage or it's expired.
     * @param tokenName name of token
     * @return early stored token or {@code null} if token isn't present in storage or it's expired.
     */
    public Token getToken(String tokenName) {
        final String token = sharedPreferences.getString(composeProperty(tokenName, TOKEN), null);
        if (token == null) {
            logger.debug("Token for service \"" + tokenName + "\" isn't stored yet. Returning null");
            return null;
        }
        final long expiresAfter = sharedPreferences.getLong(composeProperty(tokenName, EXPIRES_AFTER), 0);
        if (dateTimeService.getTimeInMillis() > expiresAfter) {
            logger.info("Token for service \"" + tokenName + "\" has been expired at " + expiresAfter + ". Returning null");
            return null;
        }
        logger.debug("Token for service \"" + tokenName + "\" has been successfully loaded: " + token +
                ". Expires after " + expiresAfter);
        return new Token(token, expiresAfter);
    }

    public String getTokenAsString(String tokenName) {
        final Token token = getToken(tokenName);
        return token != null ? token.getToken() : null;
    }

    /**
     * Stores token
     * @param tokenName name of token
     * @param token token data
     */
    public void storeToken(String tokenName, Token token) {
        sharedPreferences.edit().
                putString(composeProperty(tokenName, TOKEN), token.getToken()).
                putLong(composeProperty(tokenName, EXPIRES_AFTER), token.getExpiresAfter()).
                commit();
    }

    /**
     * Removes token from storage
     * @param tokenName name of token
     */
    public void revokeToken(String tokenName) {
        sharedPreferences.edit().
                remove(composeProperty(tokenName, TOKEN)).
                remove(composeProperty(tokenName, EXPIRES_AFTER)).
                commit();
    }

    public boolean isValid(Token token) {
        return dateTimeService.getTimeInMillis() <= token.getExpiresAfter();
    }

    private static String composeProperty(String tokenName, String propertyName) {
        return "token." + tokenName + "." + propertyName;
    }

    public Token createToken(String token, long expireTime, TimeUnit expireUnit) {
        final long millisTime = expireUnit.toMillis(expireTime) - 5000;
        return new Token(token, dateTimeService.getTimeInMillis() + millisTime);
    }
}
