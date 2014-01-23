package com.appctek.anyroshambo.social.auth;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public class ErrorInfo {

    public static final String ERROR_CODE = "error";
    public static final String EXCEPTION_DETAIL = "exception";

    private String code;
    private final Map<String,Object> params = new LinkedHashMap<String, Object>();

    private ErrorInfo() {
        this.code = "";
    }

    public ErrorInfo withCode(String code) {
        this.code = code;
        return this;
    }

    public ErrorInfo withDetail(String name, Object value) {
        this.params.put(name, value);
        return this;
    }

    public boolean isError() {
        return code.length() > 0;
    }

    public String getCode() {
        return code;
    }

    public Object getDetail(String name) {
        return params.get(name);
    }

    public Map<String,Object> getDetails() {
        return Collections.unmodifiableMap(params);
    }

    public ErrorInfo fromThrowable(Throwable e) {
        return withCode(ERROR_CODE).withDetail(EXCEPTION_DETAIL, e);
    }

    public static ErrorInfo success() {
        return create();
    }

    public static ErrorInfo create(String code) {
        return create().withCode(code);
    }

    public static ErrorInfo create() {
        return new ErrorInfo();
    }

}
