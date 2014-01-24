package com.appctek.anyroshambo.util.http.converters;

import com.appctek.anyroshambo.util.GenericException;
import com.appctek.anyroshambo.util.converters.Converter;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONTokener;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public class JSONEntityConverter<T> implements Converter<HttpEntity,T> {

    private Converter<HttpEntity, String> stringConverter;

    public JSONEntityConverter(Converter<HttpEntity, String> stringConverter) {
        this.stringConverter = stringConverter;
    }

    @SuppressWarnings("unchecked")
    public T convert(HttpEntity src) {
        final String str = stringConverter.convert(src);
        try {
            return (T)new JSONTokener(str).nextValue();
        } catch (JSONException e) {
            throw new GenericException(e);
        }
    }
}
