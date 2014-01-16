package com.appctek.anyroshambo.util;

import org.json.JSONException;
import org.json.JSONObject;
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

    public static String getStringOrDefault(JSONObject jsonObject, String key) {
        try {
            return jsonObject.has(key) ? jsonObject.getString(key) : null;
        } catch (JSONException e) {
            throw new RuntimeException("JSONObject throws JSONException but key " + key + " is exists", e);
        }
    }


}
