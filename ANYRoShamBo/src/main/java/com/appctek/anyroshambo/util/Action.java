package com.appctek.anyroshambo.util;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public interface Action<P> {
    void execute(P param);
}
