package com.appctek.anyroshambo;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageView;
import com.appctek.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends Activity {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private static class SplashAction {
        private SplashAction next;
        private Runnable runnable;
        public boolean scheduled;

        public static SplashAction createSequence(Runnable... runnables) {
            final SplashAction head = new SplashAction();
            SplashAction current = head;
            for (int i = 0; i < runnables.length; i++) {
                current.runnable = runnables[i];
                if (i < runnables.length - 1) {
                    current.next = new SplashAction();
                    current = current.next;
                }
            }
            return head;
        }

    }

    private void scheduleDelayedActions(final View view, final SplashAction action) {
        action.runnable.run();

        final Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        if (action.next != null) {

            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    view.postDelayed(new Runnable() {
                        public void run() {
                            setFadeOutAnimation(view, action.next);
                        }
                    }, getResources().getInteger(R.integer.splash_duration));
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });

            // ability to skip action
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setFadeOutAnimation(v, action.next);
                }
            });

        }

        // starting fade-in animation
        view.startAnimation(fadeIn);

    }

    private void setFadeOutAnimation(final View view, final SplashAction action) {

        if (action.scheduled) {
            return;
        }
        action.scheduled = true;

        float alpha = 1f;
        long duration = getResources().getInteger(R.integer.fade_duration);

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
                scheduleDelayedActions(view, action);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.setOnClickListener(null);
        view.startAnimation(fadeOut);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger.debug("Initializing main activity...");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // enable hardware acceleration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        logger.debug("Screen resolution: " + width + "x" + height);

        setContentView(R.layout.main);

        final ImageView imageView = (ImageView) findViewById(R.id.splash);
        final SplashAction splashActions = SplashAction.
                createSequence(new Runnable() {
                                   public void run() {
                                       imageView.setImageResource(R.drawable.logoscreen);
                                   }
                               }, new Runnable() {
                                   public void run() {
                                       imageView.setImageResource(R.drawable.splashscreen);
                                   }
                               }, new Runnable() {
                                   public void run() {
                                       imageView.setImageResource(R.drawable.gamescreen);
                                   }
                               }
                );

        scheduleDelayedActions(imageView, splashActions);

    }

}

