package com.appctek.anyroshambo.social.auth;

import com.appctek.anyroshambo.util.WebUtils;
import org.apache.http.NameValuePair;

import java.util.*;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-20-01
 */
public class OAuthHeaderParams {

    public static final String DEFAULT_HTTP_METHOD = "POST";

    public static final String CONSUMER_KEY_PARAM = "oauth_consumer_key";
    public static final String NONCE_PARAM = "oauth_nonce";
    public static final String SIGNATURE_METHOD_PARAM = "oauth_signature_method";
    public static final String TIMESTAMP_PARAM = "oauth_timestamp";
    public static final String VERSION_PARAM = "oauth_version";
    public static final String SIGNATURE_PARAM = "oauth_signature";
    public static final String CALLBACK_PARAM = "oauth_callback";
    public static final String TOKEN_PARAM = "oauth_token";
    public static final String DEFAULT_SIGNATURE_METHOD = "HMAC-SHA1";
    public static final String DEFAULT_VERSION = "1.0";

    private String httpMethod = DEFAULT_HTTP_METHOD;
    private String baseUrl;

    private Iterable<NameValuePair> urlParams = Collections.emptyList();
    private Iterable<NameValuePair> postParams = Collections.emptyList();

    private String consumerSecret = null;
    private String tokenSecret = "";

    private Map<String,String> oauthParams = new TreeMap<String, String>();

    private OAuthHeaderParams putOAuthParam(String param, String value) {
        this.oauthParams.put(param, value);
        return this;
    }

    public OAuthHeaderParams token(String token) {
        return putOAuthParam(TOKEN_PARAM, token);
    }

    public OAuthHeaderParams consumerKey(String consumerKey) {
        return putOAuthParam(CONSUMER_KEY_PARAM, consumerKey);
    }

    public OAuthHeaderParams callbackUri(String callbackUri) {
        return putOAuthParam(CALLBACK_PARAM, callbackUri);
    }

    public OAuthHeaderParams signatureMethod(String method) {
        return putOAuthParam(SIGNATURE_METHOD_PARAM, method);
    }

    public OAuthHeaderParams version(String version) {
        return putOAuthParam(VERSION_PARAM, version);
    }

    public OAuthHeaderParams timestamp(long timestamp) {
        return putOAuthParam(TIMESTAMP_PARAM, Long.toString(timestamp));
    }

    public OAuthHeaderParams nonce(String nonce) {
        return putOAuthParam(NONCE_PARAM, nonce);
    }

    public OAuthHeaderParams urlParams(Iterable<NameValuePair> urlParams) {
        this.urlParams = urlParams;
        return this;
    }

    public OAuthHeaderParams postParams(Iterable<NameValuePair> postParams) {
        this.postParams = postParams;
        return this;
    }

    public OAuthHeaderParams baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public OAuthHeaderParams httpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public OAuthHeaderParams signature(String signature) {
        return putOAuthParam(SIGNATURE_PARAM, signature);
    }

    public OAuthHeaderParams consumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
        return this;
    }

    public OAuthHeaderParams tokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
        return this;
    }

    public OAuthHeaderParams sign() {
        final List<NameValuePair> oauthPairs = WebUtils.entriesToNameValuePairs(oauthParams.entrySet());
        return signature(OAuthUtils.buildSignature(
                httpMethod,
                baseUrl,
                consumerSecret,
                tokenSecret,
                urlParams,
                postParams,
                oauthPairs));
    }

    public String toString() {
        final List<NameValuePair> oauthPairs = WebUtils.entriesToNameValuePairs(oauthParams.entrySet());
        return OAuthUtils.buildOAuthHeader(oauthPairs);
    }

    public static OAuthHeaderParams create() {
        return new OAuthHeaderParams();
    }
}
