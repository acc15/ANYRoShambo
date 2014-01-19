package com.appctek.anyroshambo.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    public void testBuildSignature() throws Exception {

        final List<Pair<String,String>> urlParams = new ArrayList<Pair<String, String>>();
        urlParams.add(new Pair<String, String>("include_entities", "true"));

        final List<Pair<String,String>> postParams = new ArrayList<Pair<String, String>>();
        postParams.add(new Pair<String, String>("status", "Hello Ladies + Gentlemen, a signed OAuth request!"));

        final List<Pair<String,String>> oauthParams = new ArrayList<Pair<String, String>>();
        oauthParams.add(new Pair<String,String>("oauth_consumer_key", "xvz1evFS4wEEPTGEFPHBog"));
        oauthParams.add(new Pair<String,String>("oauth_nonce", "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg"));
        oauthParams.add(new Pair<String,String>("oauth_signature_method", "HMAC-SHA1"));
        oauthParams.add(new Pair<String,String>("oauth_timestamp", "1318622958"));
        oauthParams.add(new Pair<String,String>("oauth_token", "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb"));
        oauthParams.add(new Pair<String,String>("oauth_version", "1.0"));

        final String signature = OAuthUtils.buildSignature(
                "POST",
                "https://api.twitter.com/1/statuses/update.json",
                "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw",
                "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE",
                urlParams,
                postParams,
                oauthParams
        );

        assertThat(signature).isEqualTo("tnnArxj06cWHq44gCs1OSKk/jLY=");
    }

    @Test
    public void testBuildHeader() throws Exception {

        final Map<String, String> values = new LinkedHashMap<String, String>();
        values.put("oauth_consumer_key", "xvz1evFS4wEEPTGEFPHBog");
        values.put("oauth_nonce", "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg");
        values.put("oauth_signature", "tnnArxj06cWHq44gCs1OSKk/jLY=");
        values.put("oauth_signature_method", "HMAC-SHA1");
        values.put("oauth_timestamp", "1318622958");
        values.put("oauth_token", "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb");
        values.put("oauth_version", "1.0");

        final String header = OAuthUtils.buildOAuthHeader(values);
        final String expectedStr = "OAuth " +
                "oauth_consumer_key=\"xvz1evFS4wEEPTGEFPHBog\", " +
                "oauth_nonce=\"kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg\", " +
                "oauth_signature=\"tnnArxj06cWHq44gCs1OSKk%2FjLY%3D\", " +
                "oauth_signature_method=\"HMAC-SHA1\", " +
                "oauth_timestamp=\"1318622958\", " +
                "oauth_token=\"370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb\", " +
                "oauth_version=\"1.0\"";

        assertThat(header).isEqualTo(expectedStr);

    }
}
