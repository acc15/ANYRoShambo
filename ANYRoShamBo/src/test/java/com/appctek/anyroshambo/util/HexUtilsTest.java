package com.appctek.anyroshambo.util;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class HexUtilsTest {
    @Test
    public void testBytesToHexString() throws Exception {
        final byte[] byteArray = new byte[] {0x00, 0x7f, (byte)0xff, 0x01, (byte)0xec, 0x22};
        final String str = HexUtils.convertBytesToHexString(byteArray);
        assertThat(str).isEqualTo("007fff01ec22");
    }

    @Test
    public void testCalculateMD5() throws Exception {
        assertThat(HexUtils.md5Hex("xyz")).isEqualTo("d16fb36f0911f878998c136191af705e");
        assertThat(HexUtils.md5Hex("qwertyu")).isEqualTo("e86fdc2283aff4717103f2d44d0610f7");

        final String str = "application_key=CBAKPGONABABABABAattachment={\"caption\":\"Test message for testing test service in test social network of this test world\"}message=покрутил рулетку и получилmethod=stream.publish83B562785858040AF1E5DF41";
        final String md5 = HexUtils.md5Hex(str);

    }
}
