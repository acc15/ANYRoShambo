package com.appctek.anyroshambo.util.http;

import com.appctek.anyroshambo.util.converters.Converter;
import com.appctek.anyroshambo.util.http.converters.JSONEntityConverter;
import com.appctek.anyroshambo.util.http.converters.StringEntityConverter;
import com.appctek.anyroshambo.util.http.converters.UrlEncodedFormEntityConverter;
import org.apache.http.HttpEntity;

import java.util.Map;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public class HttpFormat {
    private static StringEntityConverter STRING = new StringEntityConverter();
    private static JSONEntityConverter<?> JSON = new JSONEntityConverter<Object>(STRING);
    private static UrlEncodedFormEntityConverter FORM = new UrlEncodedFormEntityConverter();

    public static Converter<HttpEntity, Map<String,String>> form() {
        return FORM;
    }

    public static Converter<HttpEntity, String> string() {
        return STRING;
    }

    @SuppressWarnings("unchecked")
    public static <T> Converter<HttpEntity, T> json() {
        return (JSONEntityConverter<T>)JSON;
    }

}
