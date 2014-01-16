package com.appctek.anyroshambo.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Random;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class RandomUtilsTest {

    @Mock
    private Random random;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRandomAlphaNumericString() throws Exception {
        when(random.nextInt(Mockito.anyInt())).thenReturn(0);
        assertThat(RandomUtils.randomAlphaNumericString(random, 3)).isEqualTo("AAA");

        when(random.nextInt(Mockito.anyInt())).thenReturn(26);
        assertThat(RandomUtils.randomAlphaNumericString(random, 3)).isEqualTo("aaa");

        when(random.nextInt(Mockito.anyInt())).thenReturn(52);
        assertThat(RandomUtils.randomAlphaNumericString(random, 3)).isEqualTo("000");
    }

    @Test
    public void testRandomAlNumWithRealRandom() throws Exception {

        final Random r = new Random();

        final String alnum = RandomUtils.randomAlphaNumericString(r, 10);

    }
}
