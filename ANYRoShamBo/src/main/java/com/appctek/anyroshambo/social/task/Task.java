package com.appctek.anyroshambo.social.task;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-24-01
 */
public interface Task<P,R> {
    R execute(P param);
    void onFinish(R result);
}
