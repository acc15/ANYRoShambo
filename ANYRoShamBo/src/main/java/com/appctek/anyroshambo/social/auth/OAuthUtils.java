package com.appctek.anyroshambo.social.auth;

import android.util.Base64;
import com.appctek.anyroshambo.util.HexUtils;
import com.appctek.anyroshambo.util.RandomUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.*;

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

    private static void addAndPercentEncodeAll(
            Iterable<NameValuePair> pairs,
            Collection<NameValuePair> params) {
        for (final NameValuePair pair: pairs) {
            final String key = percentEncode(pair.getName());
            final String value = percentEncode(pair.getValue());
            params.add(new BasicNameValuePair(key, value));
        }
    }

    public static String buildSignature(
            final String method,
            final String baseUrl,
            final String consumerSecret,
            final String tokenSecret,
            final Iterable<NameValuePair> urlParams,
            final Iterable<NameValuePair> postParams,
            final Iterable<NameValuePair> oauthParams) {

        if (baseUrl == null) {
            throw new NullPointerException("baseUrl parameter missing");
        }
        if (method == null) {
            throw new NullPointerException("httpMethod parameter missing");
        }
        if (consumerSecret == null) {
            throw new NullPointerException("consumerSecret is null");
        }
        if (tokenSecret == null) {
            throw new NullPointerException("tokenSecret is null");
        }


        final List<NameValuePair> percentEncodedParams = new ArrayList<NameValuePair>();
        addAndPercentEncodeAll(urlParams, percentEncodedParams);
        addAndPercentEncodeAll(postParams, percentEncodedParams);
        addAndPercentEncodeAll(oauthParams, percentEncodedParams);

        Collections.sort(percentEncodedParams, new Comparator<NameValuePair>() {
            public int compare(NameValuePair nameValuePair, NameValuePair nameValuePair2) {
                final int keyCmp = nameValuePair.getName().compareTo(nameValuePair2.getName());
                if (keyCmp != 0) {
                    return keyCmp;
                }
                return nameValuePair.getValue().compareTo(nameValuePair2.getValue());
            }
        });

        final StringBuilder sb = new StringBuilder();
        for (final NameValuePair param: percentEncodedParams) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(param.getName()).append('=').append(param.getValue());
        }

        final String paramString = sb.toString();

        sb.setLength(0);
        sb.append(method.toUpperCase()).append('&').
           append(percentEncode(baseUrl)).append('&').
           append(percentEncode(paramString));

        final String signKey = percentEncode(consumerSecret) + "&" + percentEncode(tokenSecret);
        final String signData = sb.toString();

        final byte[] encodedSignature = encodeHmacSHA1(signKey, signData);
        final String signature = Base64.encodeToString(encodedSignature, Base64.NO_WRAP);
        return signature;
    }

    private static byte[] encodeHmacSHA1(String key, String data) {
        final SecretKeySpec keySpec = new SecretKeySpec(HexUtils.getBytesInUTF8(key), HMAC_SHA_1);
        try {
            final Mac mac = Mac.getInstance(HMAC_SHA_1);
            mac.init(keySpec);
            return mac.doFinal(HexUtils.getBytesInUTF8(data));
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

    public static String buildOAuthHeader(List<NameValuePair> values) {
        final StringBuilder sb = new StringBuilder();
        for (final NameValuePair e: values) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(percentEncode(e.getName())).append("=\"").append(percentEncode(e.getValue())).append("\"");
        }
        sb.insert(0, "OAuth ");
        return sb.toString();
    }

}
