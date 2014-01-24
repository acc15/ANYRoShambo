package com.appctek.anyroshambo.social.auth;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public class ErrorInfo {

    private Enum<?> code;
    private Throwable throwable;
    private final Map<String,Object> details = new LinkedHashMap<String, Object>();

    private ErrorInfo() {
    }

    public ErrorInfo withCode(Enum<?> code) {
        this.code = code;
        return this;
    }

    public ErrorInfo withThrowable(Throwable e) {
        this.throwable = e;
        return this;
    }

    public ErrorInfo withDetail(String name, Object value) {
        this.details.put(name, value);
        return this;
    }

    public boolean isError() {
        return code != null;
    }

    public Enum<?> getCode() {
        return code;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Object getDetail(String name) {
        return details.get(name);
    }

    public Map<String,Object> getDetails() {
        return Collections.unmodifiableMap(details);
    }

    public boolean is(Enum<?> code) {
        return code == this.code;
    }

    public static ErrorInfo success() {
        return new ErrorInfo();
    }

    public static ErrorInfo create(Enum<?> code) {
        return new ErrorInfo().withCode(code);
    }

}
