package com.appctek.anyroshambo.util.converters;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public interface Converter<S,T> {

    T convert(S src);

}
