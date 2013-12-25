package com.appctek.anyroshambo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.appctek.R;
import com.appctek.anyroshambo.services.DateTimeService;
import com.appctek.anyroshambo.services.ServiceRepository;
import com.appctek.anyroshambo.services.VibrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends Activity {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private static final float PI = (float)Math.PI;


    // to make sure it compiles using old android SDK jars
    private static final int FLAG_HARDWARE_ACCELERATED = 0x01000000;
    private static final int SDK_VERSION_HONEYCOMB = 11;

    private ShakeDetector shakeDetector = ServiceRepository.getRepository().getShakeDetector(this);
    private FadedSequentialAnimator sequentialAnimator = ServiceRepository.getRepository().getSequentialAnimator();
    private DateTimeService dateTimeService = ServiceRepository.getRepository().getDateTimeService();
    private VibrationService vibrationService = ServiceRepository.getRepository().getVibrationService(this);

    private View[] icons;

    private void setIconPositions(View triangle, float angle) {

        final int width = triangle.getWidth();
        final int height = triangle.getHeight();

        // angle between icons
        final float iconAngle = 2*PI/icons.length;
        final float radius = height*2f/3;

        final float centerX = triangle.getLeft() + width/2,
                    centerY = triangle.getTop() + radius;
        for (final View icon: icons) {

            final int iconWidth = icon.getWidth();
            final int iconHeight = icon.getHeight();

            final float xAbsolute = (float)Math.cos(angle) * radius;
            final float yAbsolute = (float)Math.sin(angle) * radius;

            final float x = centerX + xAbsolute - iconWidth/2;
            final float y = centerY - yAbsolute - iconHeight/2;

            final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)icon.getLayoutParams();
            layoutParams.leftMargin = (int)x;
            layoutParams.topMargin = (int)y;
            icon.setLayoutParams(layoutParams);
            angle += iconAngle;

        }


    }

    private void setupGame() {

        final ImageView triangle = (ImageView)findViewById(R.id.triangle);
        icons = new View[] {findViewById(R.id.walk), findViewById(R.id.drink), findViewById(R.id.party)};

        final float initialAngle = PI/2;
        setIconPositions(triangle, initialAngle);

        shakeDetector.start(new ShakeDetector.ShakeListener() {
            public void onShake() {

                shakeDetector.pause();
                vibrationService.feedback();

                final float fromAngle = 0;
                final float toAngle = 3600;

                final ImageView triangle = (ImageView)findViewById(R.id.triangle);
                final RotateAnimation rotateAnimation = new RotateAnimation(fromAngle, toAngle,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 2f/3);
                rotateAnimation.setDuration(10000);
                rotateAnimation.setInterpolator(new DecelerateInterpolator(2f));

                rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                        final ViewTreeObserver observer = triangle.getViewTreeObserver();
                        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            public boolean onPreDraw() {
                                if (rotateAnimation.hasEnded()) {
                                    observer.removeOnPreDrawListener(this);
                                    return true;
                                }

                                final long currentTime = dateTimeService.getAnimationTimeMillis();
                                final long startTime = rotateAnimation.getStartTime();
                                final long duration = rotateAnimation.getDuration();
                                final long time = currentTime - startTime;

                                final float timePosition = (float)time/duration;
                                final Interpolator interpolator = rotateAnimation.getInterpolator();
                                final float interpolation = interpolator.getInterpolation(timePosition);

                                float degrees = (fromAngle + (toAngle-fromAngle) * interpolation) % 360; // degrees
                                final float angle = initialAngle - (float)Math.toRadians(degrees);
                                setIconPositions(triangle, angle);
                                return true;
                            }
                        });
                    }

                    public void onAnimationEnd(Animation animation) {
                        shakeDetector.resume();
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                if (triangle.getAnimation() != null && !triangle.getAnimation().hasEnded()) {
                    triangle.getAnimation().cancel();
                }
                triangle.startAnimation(rotateAnimation);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        shakeDetector.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        shakeDetector.resume();
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
        if (Build.VERSION.SDK_INT >= SDK_VERSION_HONEYCOMB) {
            getWindow().setFlags(
                    FLAG_HARDWARE_ACCELERATED,
                    FLAG_HARDWARE_ACCELERATED);
        }

        final View contentView = findViewById(android.R.id.content);
        contentView.setBackgroundColor(Color.BLACK);

        setContentView(R.layout.splash);

        final ImageView imageView = (ImageView) findViewById(R.id.splash);
        sequentialAnimator.start(new FadedSequentialAnimator.Action() {
            public View execute() {
                imageView.setImageResource(R.drawable.logoscreen);
                return imageView;
            }
        }, new FadedSequentialAnimator.Action() {
            public View execute() {
                final View gameView = getLayoutInflater().inflate(R.layout.game, null);

                gameView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        setupGame();
                        gameView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });
                setContentView(gameView);
                return gameView;
            }
        });

    }


}

