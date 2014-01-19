package com.appctek.anyroshambo.util;

import java.util.Map;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-19-01
 */
public class OAuthUtils {
    private static boolean dontPercentEncode(byte b) {
        return b >= 0x30 && b <= 0x39 ||
               b >= 0x41 && b <= 0x5A ||
               b >= 0x61 && b <= 0x7A ||
               b == 0x2D || b == 0x2E || b == 0x5F || b == 0x7E;
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
