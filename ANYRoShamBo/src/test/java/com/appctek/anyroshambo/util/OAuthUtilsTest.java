package com.appctek.anyroshambo.util;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class OAuthUtilsTest {
    @Test
    public void testPercentEncode() throws Exception {

        assertThat(OAuthUtils.percentEncode("Ladies + Gentlemen")).isEqualTo("Ladies%20%2B%20Gentlemen");
        assertThat(OAuthUtils.percentEncode("An encoded string!")).isEqualTo("An%20encoded%20string%21");
        assertThat(OAuthUtils.percentEncode("Dogs, Cats & Mice")).isEqualTo("Dogs%2C%20Cats%20%26%20Mice");
        assertThat(OAuthUtils.percentEncode("☃")).isEqualTo("%E2%98%83");

    }
}
