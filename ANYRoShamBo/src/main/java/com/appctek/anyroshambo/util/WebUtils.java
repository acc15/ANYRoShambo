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
import com.appctek.anyroshambo.social.VkontakteService;
import com.google.common.net.MediaType;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-15-01
 */
public class WebUtils {


    public static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");
    public static final Logger logger = LoggerFactory.getLogger(VkontakteService.class);

    public static Charset parseCharset(Header contentTypeHeader) {
        if (contentTypeHeader == null) {
            return DEFAULT_CHARSET;
        }
        final MediaType mediaType = MediaType.parse(contentTypeHeader.getValue());
        return mediaType.charset().or(DEFAULT_CHARSET);
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

    private static boolean dontPercentEncode(byte b) {
        return b >= 0x30 && b <= 0x39 ||
               b >= 0x41 && b <= 0x5A ||
               b >= 0x61 && b <= 0x7A ||
               b == 0x2D || b == 0x2E || b == 0x5F || b == 0x7E;
    }

    /**
     * <p>Percent encoding as proposed by Twitter.</p>
     * <p>See <a href="https://dev.twitter.com/docs/auth/percent-encoding-parameters">
     *     https://dev.twitter.com/docs/auth/percent-encoding-parameters</a> to get details.</p>
     * <p>Key difference between {@link java.net.URLEncoder#encode(String, String)} in that space character
     * is inserted as '%20' instead of '+' character</p>
     * @param str string to encode
     * @return percent encoded string
     */
    public static String percentEncode(String str) {
        final byte[] bytes = HexUtils.getBytesInUTF8(str);
        final StringBuilder encoded = new StringBuilder();
        for (byte b: bytes) {
            if (dontPercentEncode(b)) {
                encoded.append((char)b);
            } else {
                encoded.append('%');
                HexUtils.appendByteAsHex(encoded, b, true);
            }
        }
        return encoded.toString();
    }

    public static interface UrlHandler {
        boolean handleUrl(DialogInterface dialog, String url);
    }
}
