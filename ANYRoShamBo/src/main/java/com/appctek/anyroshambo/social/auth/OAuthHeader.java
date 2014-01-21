package com.appctek.anyroshambo.social.auth;

import com.appctek.anyroshambo.util.WebUtils;
import org.apache.http.NameValuePair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-22-01
 */
public class OAuthHeader {
    public static final String CONSUMER_KEY_PARAM = "oauth_consumer_key";
    public static final String NONCE_PARAM = "oauth_nonce";
    public static final String SIGNATURE_METHOD_PARAM = "oauth_signature_method";
    public static final String TIMESTAMP_PARAM = "oauth_timestamp";
    public static final String VERSION_PARAM = "oauth_version";
    public static final String SIGNATURE_PARAM = "oauth_signature";
    public static final String CALLBACK_PARAM = "oauth_callback";
    public static final String TOKEN_PARAM = "oauth_token";

    public static final String DEFAULT_VERSION = "1.0";
    public static final String DEFAULT_SIGNATURE_METHOD = "HMAC-SHA1";

    private Map<String,String> oauthParams = new TreeMap<String, String>();

    private OAuthHeader putOAuthParam(String param, String value) {
        if (value != null) {
            this.oauthParams.put(param, value);
        }
        return this;
    }

    public OAuthHeader token(String token) {
        return putOAuthParam(TOKEN_PARAM, token);
    }

    public OAuthHeader consumerKey(String consumerKey) {
        return putOAuthParam(CONSUMER_KEY_PARAM, consumerKey);
    }

    public OAuthHeader callbackUri(String callbackUri) {
        return putOAuthParam(CALLBACK_PARAM, callbackUri);
    }

    public OAuthHeader signatureMethod(String method) {
        return putOAuthParam(SIGNATURE_METHOD_PARAM, method);
    }

    public OAuthHeader version(String version) {
        return putOAuthParam(VERSION_PARAM, version);
    }

    public OAuthHeader timestamp(long timestamp) {
        return putOAuthParam(TIMESTAMP_PARAM, Long.toString(timestamp));
    }

    public OAuthHeader nonce(String nonce) {
        return putOAuthParam(NONCE_PARAM, nonce);
    }

    public OAuthHeader signature(String signature) {
        return putOAuthParam(SIGNATURE_PARAM, signature);
    }

    public String toString() {
        return OAuthUtils.buildOAuthHeader(asNameValuePairs());
    }

    public Map<String,String> asMap() {
        return Collections.unmodifiableMap(oauthParams);
    }

    public List<NameValuePair> asNameValuePairs() {
        return WebUtils.entriesToNameValuePairs(oauthParams.entrySet());
    }

    public static OAuthHeader create() {
        return new OAuthHeader();
    }
}
