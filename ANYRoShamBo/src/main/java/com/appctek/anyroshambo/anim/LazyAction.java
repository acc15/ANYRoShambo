package com.appctek.anyroshambo.anim;

/**
* @author Vyacheslav Mayorov
* @since 2014-04-01
*/
public interface LazyAction extends Runnable {
    void setListener(Runnable listener);
}
