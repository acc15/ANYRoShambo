package com.appctek.anyroshambo.util;

import java.util.Random;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class RandomUtils {

    public static final char[] ALNUM_FIRST_CHARS = new char[]{'A', 'a', '0'};

    private static char generateAlphaNumericChar(Random random) {
        final int letterCount = ('Z' - 'A' + 1);
        final int digitCount = ('9'-'0' + 1);
        final int alnumChars = letterCount * 2 + digitCount;
        final int val = random.nextInt(alnumChars);
        final char ch = (char)(ALNUM_FIRST_CHARS[val / letterCount] + val % letterCount);
        return ch;
    }

    public static String randomAlphaNumericString(Random random, int length) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i<length; i++) {
            final char ch = generateAlphaNumericChar(random);
            stringBuilder.append(ch);
        }
        return stringBuilder.toString();
    }

    public static int nextPositiveOrNegative(Random random, int min, int max) {
        final int values = max - min + 1;
        final int randomValue = random.nextInt(values*2);
        return randomValue < values ? min + randomValue : -(min + randomValue - values);
    }

    public static long nextLong(Random random, long min, long max) {
        final long randomValue = random.nextLong();
        return min + Math.abs(randomValue) % (max-min+1);
    }

    public static float nextFloat(Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }
}
