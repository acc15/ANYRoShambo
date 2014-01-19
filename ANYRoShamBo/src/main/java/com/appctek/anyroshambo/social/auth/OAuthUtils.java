package com.appctek.anyroshambo.social.auth;

import com.appctek.anyroshambo.util.HexUtils;
import com.appctek.anyroshambo.util.RandomUtils;
import com.google.common.io.BaseEncoding;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-19-01
 */
public class OAuthUtils {

    public static final String HMAC_SHA_1 = "HmacSHA1";

    private static boolean dontPercentEncode(byte b) {
        return b >= 0x30 && b <= 0x39 ||
               b >= 0x41 && b <= 0x5A ||
               b >= 0x61 && b <= 0x7A ||
               b == 0x2D || b == 0x2E || b == 0x5F || b == 0x7E;
    }

    public static String generateNonce(Random random) {
        return RandomUtils.randomAlphaNumericString(random, 40);
    }

    private static void addAndPercentEncodeAll(Map<String, String> params, Map<String, String> values) {
        for (final Map.Entry<String,String> param: params.entrySet()) {
            final String key = percentEncode(param.getKey());
            final String value = percentEncode(param.getValue());
            values.put(key, value);
        }
    }

    public static String buildSignature(
            final String method,
            final String baseUrl,
            final String consumerSecret,
            final String tokenSecret,
            final Map<String,String> urlParams,
            final Map<String,String> httpParams,
            final Map<String,String> oauthParams) {

        final TreeMap<String,String> paramMap = new TreeMap<String, String>();
        addAndPercentEncodeAll(urlParams, paramMap);
        addAndPercentEncodeAll(httpParams, paramMap);
        addAndPercentEncodeAll(oauthParams, paramMap);

        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> e: paramMap.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(e.getKey()).append('=').append(e.getValue());
        }

        final String paramString = sb.toString();

        sb.setLength(0);
        sb.append(method.toUpperCase()).append('&').
           append(percentEncode(baseUrl)).append('&').
           append(percentEncode(paramString));

        final String signKey = percentEncode(consumerSecret) + "&" + percentEncode(tokenSecret);
        final String signData = sb.toString();

        final byte[] encodedSignature = encodeHmacSHA1(signKey, signData);
        final String signature = BaseEncoding.base64().encode(encodedSignature);
        return signature;
    }

    private static byte[] encodeHmacSHA1(String key, String data) {
        final SecretKeySpec keySpec = new SecretKeySpec(HexUtils.getBytesInUTF8(key), HMAC_SHA_1);
        try {
            final Mac mac = Mac.getInstance(HMAC_SHA_1);
            mac.init(keySpec);
            final byte[] result = mac.doFinal(HexUtils.getBytesInUTF8(data));
            return result;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Can't encode values by " + HMAC_SHA_1 + " algorithm", e);
        }
    }

    /**
     * <p>Percent encoding as proposed by Twitter.</p>
     * <p>See <a href="https://dev.twitter.com/docs/auth/percent-encoding-parameters">
     *     https://dev.twitter.com/docs/auth/percent-encoding-parameters</a> to get details.</p>
     * <p>Key difference between {@link java.net.URLEncoder#encode(String, String)} in that space character
     * is inserted as '%20' instead of '+' character</p>
     * @param str string to encode
     * @return percent encoded string
     */
    public static String percentEncode(String str) {
        final byte[] bytes = HexUtils.getBytesInUTF8(str);
        final StringBuilder encoded = new StringBuilder();
        for (byte b: bytes) {
            if (dontPercentEncode(b)) {
                encoded.append((char)b);
            } else {
                encoded.append('%');
                HexUtils.appendByteAsHex(encoded, b, true);
            }
        }
        return encoded.toString();
    }

    public static String buildOAuthHeader(Map<String,String> values) {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String,String> e: values.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(percentEncode(e.getKey())).append("=\"").append(percentEncode(e.getValue())).append("\"");
        }
        sb.insert(0, "OAuth ");
        return sb.toString();
    }

}
