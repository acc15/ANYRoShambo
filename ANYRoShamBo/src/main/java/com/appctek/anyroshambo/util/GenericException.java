package com.appctek.anyroshambo.util;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public class GenericException extends RuntimeException {
    public GenericException(String message) {
        super(message);
    }

    public GenericException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public GenericException(Throwable throwable) {
        super(throwable);
    }
}
