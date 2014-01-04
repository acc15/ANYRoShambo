package com.appctek.anyroshambo.anim;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-04-01
 */
public class NopAction implements LazyAction {

    private Runnable listener;

    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    public void run() {
        listener.run();
    }
}
