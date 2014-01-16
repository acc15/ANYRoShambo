package com.appctek.anyroshambo.util;

import org.json.JSONException;
import org.json.JSONTokener;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-16-01
 */
public class JSONUtils {

    @SuppressWarnings("unchecked")
    public static <T> T parseJSON(String str) throws JSONException {
        return (T)new JSONTokener(str).nextValue();
    }

}
