package com.appctek.anyroshambo.util;

import ru.vmsoftware.parser.builder.CaptureContext;
import ru.vmsoftware.parser.builder.CaptureListener;
import ru.vmsoftware.parser.builder.ParserBuilder;
import ru.vmsoftware.parser.builder.matchers.TokenMatcher;

import java.util.HashMap;
import java.util.Map;

import static ru.vmsoftware.parser.builder.matchers.MatcherFactory.*;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-17-01
 */
public class MediaType {

    private String type;
    private String subtype;
    private Map<String, String> params;

    private MediaType() {
        this.params = new HashMap<String, String>();
    }

    public MediaType(String type, String subtype, Map<String, String> params) {
        this.type = type;
        this.subtype = subtype;
        this.params = params;
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getParameter(String parameter) {
        return params.get(parameter);
    }

    public static MediaType valueOf(String val) {
        final MediaType mediaType = new MediaType();
        final StringBuilder quotedStrValue = new StringBuilder();

        final TokenMatcher separators = charInArray(
                '\t', ' ', '"', '(', ')', ',', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '{', '}');
        final TokenMatcher ctl = any(charRange(0, 31), charValue(127));
        final TokenMatcher crlf = sequence("\r\n");
        final TokenMatcher whitespace = any(charValue(' '), charValue('\t'));
        final TokenMatcher skipWS = oneOrMore(whitespace);
        final TokenMatcher token = oneOrMore(all(anyChar(), invert(any(separators, ctl))));
        final TokenMatcher lws = sequence(zeroOrOne(crlf), whitespace);

        final TokenMatcher quotedText = all(
                invert(charValue('\"')),
                any(
                        capture(new CaptureListener() {
                            public void onCapture(CaptureContext context) {
                                quotedStrValue.append(context.getCapture().toString());
                            }
                        }, all(anyChar(), invert(ctl))),
                        lws)
        );

        final TokenMatcher quotedPair = capture(new CaptureListener() {
            public void onCapture(CaptureContext context) {
                quotedStrValue.append(context.getCapture().charAt(1));
            }
        }, sequence(charValue('\\'), anyChar()));

        final TokenMatcher quotedStr = sequence(
                charValue('\"'),
                repeat(0, UNBOUNDED, any(quotedPair, quotedText)),
                charValue('\"'));

        final TokenMatcher contentType = tokens(
                capture(new CaptureListener() {
                    public void onCapture(CaptureContext context) {
                        mediaType.type = context.getCapture().toString();
                    }
                }, token),
                charValue('/'),
                capture(new CaptureListener() {
                    public void onCapture(CaptureContext context) {
                        mediaType.subtype = context.getCapture().toString();
                    }
                }, token),
                repeat(0, UNBOUNDED, capture(new CaptureListener() {
                    public void onCapture(CaptureContext context) {
                        mediaType.params.put(context.<String>getValue("name"), context.<String>getValue("value"));
                    }
                }, tokens(
                        charValue(';'),
                        capture(new CaptureListener() {
                            public void onCapture(CaptureContext context) {
                                context.addValue("name", context.getCapture().toString());
                            }
                        }, token),
                        charValue('='),
                        any(
                                capture(new CaptureListener() {
                                    public void onCapture(CaptureContext context) {
                                        context.addValue("value", context.getCapture().toString());
                                    }
                                }, token),
                                capture(new CaptureListener() {
                                    public void onCapture(CaptureContext context) {
                                        context.addValue("value", quotedStrValue.toString());
                                        quotedStrValue.setLength(0);
                                    }
                                }, quotedStr)
                        )))));

        if (!ParserBuilder.parse(val).by(contentType).skip(skipWS).match()) {
            throw new IllegalArgumentException("\"" + val + "\" isn't valid Content-Type value.");
        }
        return mediaType;
    }


    public String toString() {
        final StringBuilder sb = new StringBuilder().append(type).append('/').append(subtype);
        for (Map.Entry<String, String> e : params.entrySet()) {
            sb.append(';').append(e.getKey()).
                    append('=').append('\"').append(e.getValue().replace("\"", "\\\"")).append('\"');
        }
        return sb.toString();
    }
}
