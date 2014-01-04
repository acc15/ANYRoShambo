package com.appctek.anyroshambo.sequences;

/**
* @author Vyacheslav Mayorov
* @since 2014-04-01
*/
public interface LazyAction {
    void setListener(Runnable listener);
    void execute();
}
