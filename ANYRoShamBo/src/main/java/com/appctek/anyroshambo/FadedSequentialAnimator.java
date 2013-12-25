package com.appctek.anyroshambo;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import com.appctek.R;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-23-12
 */
public class FadedSequentialAnimator {


    public static interface Action {

        /**
         * Executes action and returns on which animation should be applied
         * @return view to animate
         */
        View execute();
    }

    private static class ActionEntry {
        private Action action;
        private ActionEntry next;
        private boolean scheduled;
    }

    private static ActionEntry createLinkedEntries(Action... actions) {
        final ActionEntry head = new ActionEntry();
        ActionEntry current = head;
        for (int i = 0; i < actions.length; i++) {
            current.action = actions[i];
            if (i < actions.length - 1) {
                current.next = new ActionEntry();
                current = current.next;
            }
        }
        return head;
    }

    public void start(Action... actions) {
        executeAction(createLinkedEntries(actions));
    }

    private void executeAction(final ActionEntry entry) {
        final View view = entry.action.execute();

        final Animation fadeIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
        if (entry.next != null) {
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    view.postDelayed(new Runnable() {
                        public void run() {
                            setFadeOutAnimation(view, entry.next);
                        }
                    }, view.getResources().getInteger(R.integer.splash_duration));
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });

            // ability to skip action
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setFadeOutAnimation(view, entry.next);
                }
            });
        }

        // starting fade-in animation
        view.startAnimation(fadeIn);
    }

    private void setFadeOutAnimation(final View view, final ActionEntry entry) {

        if (entry.scheduled) {
            return;
        }
        entry.scheduled = true;

        float alpha = 1f;
        long duration = view.getResources().getInteger(R.integer.fade_duration);

        final Animation animation = view.getAnimation();
        if (animation != null) {

            animation.setAnimationListener(null);
            animation.cancel();
            animation.reset();

            final Transformation tr = new Transformation();
            final long currentTime = AnimationUtils.currentAnimationTimeMillis();
            animation.getTransformation(currentTime, tr);

            alpha = tr.getAlpha();
            duration = animation.hasEnded() ? animation.getDuration() : currentTime - animation.getStartTime();

        }

        final AlphaAnimation fadeOut = new AlphaAnimation(alpha, 0);
        fadeOut.setDuration(duration);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                executeAction(entry);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.setOnClickListener(null);
        view.startAnimation(fadeOut);

    }

}