package com.appctek.anyroshambo.util;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class MediaTypeTest {
    @Test
    public void testValueOf() throws Exception {

        final MediaType mt2 = MediaType.valueOf("  text/xml;quoted=\" quoted \\\"param\\\" with lws \r\n having value = utf-8\"");
        assertThat(mt2.getType()).isEqualTo("text");
        assertThat(mt2.getSubtype()).isEqualTo("xml");
        assertThat(mt2.getParameter("quoted")).isEqualTo(" quoted \"param\" with lws having value = utf-8");

        final MediaType mt = MediaType.valueOf("text / xml; charset = utf-8");
        assertThat(mt.getType()).isEqualTo("text");
        assertThat(mt.getSubtype()).isEqualTo("xml");
        assertThat(mt.getParameter("charset")).isEqualTo("utf-8");

        final MediaType mt3 = MediaType.valueOf("text/html; charset=UTF-8  ");
        assertThat(mt3.getType()).isEqualTo("text");
        assertThat(mt3.getSubtype()).isEqualTo("html");
        assertThat(mt3.getParameter("charset")).isEqualTo("UTF-8");
    }
}
