package com.appctek.anyroshambo.util;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class WebUtilsTest {
    @Test
    public void testPercentEncode() throws Exception {

        assertThat(WebUtils.percentEncode("Ladies + Gentlemen")).isEqualTo("Ladies%20%2B%20Gentlemen");
        assertThat(WebUtils.percentEncode("An encoded string!")).isEqualTo("An%20encoded%20string%21");
        assertThat(WebUtils.percentEncode("Dogs, Cats & Mice")).isEqualTo("Dogs%2C%20Cats%20%26%20Mice");
        assertThat(WebUtils.percentEncode("☃")).isEqualTo("%E2%98%83");

    }
}
