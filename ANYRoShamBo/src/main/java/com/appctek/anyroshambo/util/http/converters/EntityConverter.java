package com.appctek.anyroshambo.util.http.converters;

import com.appctek.anyroshambo.util.WebUtils;
import com.appctek.anyroshambo.util.GenericException;
import com.appctek.anyroshambo.util.converters.Converter;
import org.apache.http.HttpEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public abstract class EntityConverter<T> implements Converter<HttpEntity, T> {

    public T convert(HttpEntity entity) {
        final String charset = WebUtils.parseCharset(entity.getContentType());
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            entity.writeTo(byteArrayOutputStream);
        } catch (IOException e) {
            throw new GenericException(e);
        }
        final String data;
        try {
            data = byteArrayOutputStream.toString(charset);
        } catch (UnsupportedEncodingException e) {
            throw new GenericException(e);
        }
        return parseByteData(data, charset);
    }

    protected abstract T parseByteData(String data, String charset);

}
