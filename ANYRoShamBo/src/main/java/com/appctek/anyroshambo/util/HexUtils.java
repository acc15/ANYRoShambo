package com.appctek.anyroshambo.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class HexUtils {


    private static char hexChar(int val, boolean upperCase) {
        return (char) (val < 10 ? '0' + val : (upperCase ? 'A' : 'a') + (val - 10));
    }

    public static void appendByteAsHex(StringBuilder stringBuilder, byte value, boolean upperCase) {
        final int hexBase = 16;
        final int v = value & 0xff;
        stringBuilder.append(hexChar((v / hexBase) % hexBase, upperCase)).
                      append(hexChar(v % hexBase, upperCase));
    }

    public static String convertBytesToHexString(byte[] byteArray, boolean upperCase) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            appendByteAsHex(sb, b, upperCase);
        }
        return sb.toString();
    }

    public static byte[] getBytesInUTF8(String str) {
        final String utf8 = "utf-8";
        try {
            return str.getBytes(utf8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding \"" + utf8 + "\" isn't supported on current VM", e);
        }
    }

    public static String md5Hex(String str) {
        try {
            final MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            final byte[] paramBytes = getBytesInUTF8(str);
            final byte[] digest = md5Digest.digest(paramBytes);
            final String md5String = convertBytesToHexString(digest, false);
            return md5String;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Can't find md5 digest algorithm", e);
        }
    }
}
