package com.appctek.anyroshambo.util.http.converters;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public class StringEntityConverter extends EntityConverter<String> {
    @Override
    protected String parseByteData(String data, String charset) {
        return data;
    }
}
