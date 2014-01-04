package com.appctek.anyroshambo.sequences;

import android.view.View;

/**
* @author Vyacheslav Mayorov
* @since 2014-04-01
*/
public class DelayAction implements LazyAction {
    private final long delay;
    private final View view;
    private Runnable listener;

    public DelayAction(View view, long delay) {
        this.view = view;
        this.delay = delay;
    }

    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    public void execute() {
        view.postDelayed(listener, delay);
    }
}
