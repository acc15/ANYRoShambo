package com.appctek.anyroshambo.util.http.converters;

import com.appctek.anyroshambo.util.WebUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public class UrlEncodedFormEntityConverter extends EntityConverter<Map<String,String>> {
    @Override
    protected Map<String,String> parseByteData(String charset, String data) {
        final List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        URLEncodedUtils.parse(pairs, new Scanner(data), charset);
        return WebUtils.nameValuePairsToMap(pairs);
    }
}
