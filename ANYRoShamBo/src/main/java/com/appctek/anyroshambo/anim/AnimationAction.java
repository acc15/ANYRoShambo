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

    public AnimationAction(View view, Animation animation) {
        this.view = view;
        this.animation = animation;
    }

    public void setListener(final Runnable listener) {
        this.animation.setAnimationListener(listener != null ? new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                listener.run();
            }

            public void onAnimationRepeat(Animation animation) {
            }
        } : null);
    }

    public void run() {
        view.clearAnimation();
        view.startAnimation(animation);
    }

}
