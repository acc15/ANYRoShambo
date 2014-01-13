package com.appctek.anyroshambo.social.auth;

import android.content.SharedPreferences;
import com.appctek.anyroshambo.services.DateTimeService;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public OAuthToken getToken(String serviceId) {
        final String token = sharedPreferences.getString(composeProperty(serviceId, TOKEN), null);
        if (token == null) {
            logger.debug("Token for service \"" + serviceId + "\" isn't stored yet. Returning null");
            return null;
        }
        final long expiresAfter = sharedPreferences.getLong(composeProperty(serviceId, EXPIRES_AFTER), 0);
        if (dateTimeService.getTimeInMillis() > expiresAfter) {
            logger.info("Token for service \"" + serviceId + "\" has been expired at " + expiresAfter + ". Returning null");
            return null;
        }
        logger.debug("Token for service \"" + serviceId + "\" has been successfully loaded: " + token +
                ". Expires after " + expiresAfter);
        return new OAuthToken(token, expiresAfter);
    }

    public void storeToken(String serviceId, OAuthToken token) {
        sharedPreferences.edit().
                putString(composeProperty(serviceId, TOKEN), token.getToken()).
                putLong(composeProperty(serviceId, EXPIRES_AFTER), token.getExpiresAfter()).
                commit();
    }

    private static String composeProperty(String serviceId, String propertyName) {
        return serviceId + "." + propertyName;
    }

}
