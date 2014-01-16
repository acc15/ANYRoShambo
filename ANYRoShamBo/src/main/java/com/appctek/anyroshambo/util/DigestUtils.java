package com.appctek.anyroshambo.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class DigestUtils {

    public static final String UTF_8 = "utf-8";

    private static char hexChar(int val) {
        return (char) (val < 10 ? '0' + val : 'a' + (val - 10));
    }

    public static String bytesToHexString(byte[] byteArray) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            final int v = b & 0xff;
            sb.append(hexChar((v / 16) % 16));
            sb.append(hexChar(v % 16));
        }
        return sb.toString();
    }

    public static String calculateMD5(String str) {
        try {
            final byte[] paramBytes = str.getBytes(UTF_8);
            final MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            final byte[] digest = md5Digest.digest(paramBytes);
            final String md5String = bytesToHexString(digest);
            return md5String;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding \"" + UTF_8 + "\" isn't supported on current VM", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Can't find md5 digest algorithm", e);
        }
    }
}
