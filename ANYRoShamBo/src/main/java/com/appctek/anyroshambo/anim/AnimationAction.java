package com.appctek.anyroshambo.anim;

import android.view.View;
import android.view.animation.Animation;

/**
* @author Vyacheslav Mayorov
* @since 2014-04-01
*/
public class AnimationAction implements LazyAction {

    private View view;
    private Animation animation;
    private Runnable listener;

    public AnimationAction(View view, Animation animation) {
        this.view = view;
        this.animation = animation;
    }

    public void setListener(final Runnable listener) {
        this.listener = listener;
    }

    public void run() {
        view.clearAnimation();
        view.startAnimation(animation);
        if (listener != null) {
            view.postDelayed(new Runnable() {
                public void run() {
                    listener.run();
                }
            }, animation.getDuration());
        }
    }

}
