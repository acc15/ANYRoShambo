package com.appctek.anyroshambo.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-15-01
 */
public class WebUtils {


    public static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");
    public static final Logger logger = LoggerFactory.getLogger(WebUtils.class);

    public static Charset parseCharset(Header contentTypeHeader) {
        if (contentTypeHeader == null) {
            return DEFAULT_CHARSET;
        }
        for (final HeaderElement headerElement: contentTypeHeader.getElements()) {
            if ("charset".equals(headerElement.getName())) {
                final String cs = headerElement.getValue();
                return Charset.forName(cs);
            }
        }
        return DEFAULT_CHARSET;
    }

    public static Map<String,String> parseUriFragmentParameters(String fragment) {
        final Map<String,String> params = new HashMap<String, String>();
        if (fragment == null) {
            return params;
        }
        final String[] values = fragment.split("&");
        for (String keyVal: values) {
            final String[] kv = keyVal.split("=", 2);
            params.put(kv[0], kv.length > 1 ? kv[1] : null);
        }
        return params;
    }

    public static void showWebViewDialog(Context context, String url, final UrlHandler handler) {
        final WebView wv = new WebView(context);
        wv.loadUrl(url);

        final LinearLayout wrapper = new LinearLayout(context);
        final EditText keyboardHack = new EditText(context);
        keyboardHack.setVisibility(View.GONE);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.addView(wv, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        wrapper.addView(keyboardHack, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final AlertDialog dialog = new AlertDialog.Builder(context).setView(wrapper).create();
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handler.handleUrl(dialog, url);
            }
        });
        dialog.show();
    }

    @SuppressWarnings("unchecked")
    public static String executePost(HttpClient httpClient, String url) throws IOException {
        final HttpPost post = new HttpPost(url);
        final HttpResponse response = httpClient.execute(post);
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("Server responded with error status: " + statusCode);
        }

        final HttpEntity entity = response.getEntity();
        final Charset charset = parseCharset(entity.getContentType());
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        entity.writeTo(byteArrayOutputStream);

        final String charsetName = charset.name();
        final String decodedString = byteArrayOutputStream.toString(charsetName);
        logger.info("Server responded with response: {}", decodedString);
        return decodedString;
    }

    public static Uri.Builder appendQueryParameters(Uri.Builder uriBuilder, Map<String,String> params) {
        for (Map.Entry<String,String> param: params.entrySet()) {
            uriBuilder.appendQueryParameter(param.getKey(), param.getValue());
        }
        return uriBuilder;
    }

    public static String getUriWithoutParams(String uri) {
        final int paramPos = uri.indexOf('?');
        if (paramPos >= 0) {
            return uri.substring(0, paramPos);
        }
        final int hashPos = uri.indexOf('#');
        if (hashPos >= 0) {
            return uri.substring(0, hashPos);
        }
        return uri;
    }

    public static List<NameValuePair> entriesToNameValuePairs(Iterable<Map.Entry<String,String>> entries) {
        final List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String,String> e: entries) {
            pairs.add(new BasicNameValuePair(e.getKey(), e.getValue()));
        }
        return pairs;
    }

    public static List<NameValuePair> parseParams(HttpEntity entity) {
        try {
            return URLEncodedUtils.parse(entity);
        } catch (IOException e) {
            throw new RuntimeException("Can't parse params from http entity", e);
        }
    }

    public static interface UrlHandler {
        boolean handleUrl(DialogInterface dialog, String url);
    }
}
