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
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-15-01
 */
public class WebUtils {

    public static final Logger logger = LoggerFactory.getLogger(WebUtils.class);
    public static final String UTF_8 = "utf-8";

    public static String parseCharset(Header contentTypeHeader) {
        if (contentTypeHeader == null) {
            return UTF_8;
        }

        final HeaderElement[] elements = contentTypeHeader.getElements();
        if (elements.length == 0) {
            return UTF_8;
        }

        final HeaderElement first = elements[0];
        final NameValuePair charsetPair = first.getParameterByName("charset");
        if (charsetPair == null) {
            return UTF_8;
        }
        return charsetPair.getValue();
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

    public static byte[] getBytesInUTF8(String str) {
        try {
            return str.getBytes(UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding \"" + UTF_8 + "\" isn't supported on current VM", e);
        }
    }

    private static class ResponseData {
        private String charset;
        private String data;

        private ResponseData(String charset, String data) {
            this.charset = charset;
            this.data = data;
        }
    }

    @SuppressWarnings("unchecked")
    private static ResponseData executeRequest(HttpClient httpClient, HttpUriRequest request) throws IOException {
        final HttpResponse response = httpClient.execute(request);
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("Server responded with error status: " + statusCode);
        }

        final HttpEntity entity = response.getEntity();
        final String charset = parseCharset(entity.getContentType());
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        entity.writeTo(byteArrayOutputStream);

        final String decodedString = byteArrayOutputStream.toString(charset);
        logger.info("Server responded with response: {}", decodedString);
        return new ResponseData(charset, decodedString);
    }

    public static String executeRequestString(HttpClient httpClient, HttpUriRequest request) throws IOException {
        return executeRequest(httpClient, request).data;
    }

    public static List<NameValuePair> executeRequestParams(HttpClient httpClient, HttpUriRequest request) throws IOException {
        final ResponseData response = executeRequest(httpClient, request);
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        URLEncodedUtils.parse(params, new Scanner(response.data), response.charset);
        return params;
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

    public static List<NameValuePair> parseRequestParams(HttpUriRequest httpUriRequest) {
        if (!(httpUriRequest instanceof HttpPost)) {
            return Collections.emptyList();
        }
        final HttpEntity entity = ((HttpPost) httpUriRequest).getEntity();
        if (entity == null) {
            return Collections.emptyList();
        }
        try {
            return URLEncodedUtils.parse(entity);
        } catch (IOException e) {
            throw new RuntimeException("Can't parse params from http entity", e);
        }
    }

    public static Map<String,String> nameValuePairsToMap(Iterable<NameValuePair> pairs) {
        final Map<String,String> params = new HashMap<String, String>();
        for (NameValuePair nvp: pairs) {
            params.put(nvp.getName(), nvp.getValue());
        }
        return params;
    }

    public static interface UrlHandler {
        boolean handleUrl(DialogInterface dialog, String url);
    }
}
