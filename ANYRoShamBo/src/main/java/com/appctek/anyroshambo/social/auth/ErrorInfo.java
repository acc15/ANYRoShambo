package com.appctek.anyroshambo.social.auth;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public class ErrorInfo {

    public static final int SUCCESS = 0;
    public static final int GENERIC_ERROR = 1;

    private int code;
    private Throwable throwable;
    private final Map<String,Object> details = new LinkedHashMap<String, Object>();

    private ErrorInfo() {
        this.code = 0;
    }

    public ErrorInfo withCode(int code) {
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
        return code != SUCCESS;
    }

    public int getCode() {
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

    public boolean is(int code) {
        return code == this.code;
    }

    public static ErrorInfo success() {
        return create(SUCCESS);
    }

    public static ErrorInfo create(int code) {
        return new ErrorInfo().withCode(code);
    }

    public static ErrorInfo create() {
        return create(GENERIC_ERROR);
    }
}
