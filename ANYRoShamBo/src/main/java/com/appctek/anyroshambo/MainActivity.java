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
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.appctek.R;
import com.appctek.anyroshambo.model.GameModel;
import com.appctek.anyroshambo.services.GameService;
import com.appctek.anyroshambo.services.ServiceRepository;
import com.appctek.anyroshambo.services.VibrationService;
import com.appctek.anyroshambo.util.AnimationHelper;
import com.appctek.anyroshambo.util.ShakeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends Activity {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private static final float PI = (float)Math.PI;


    // to make sure it compiles using old android SDK jars
    private static final int FLAG_HARDWARE_ACCELERATED = 0x01000000;
    private static final int SDK_VERSION_HONEYCOMB = 11;

    private static final float TWO_DIV_THREE = 2f/3;
    private static final float HALF = 0.5f;

    private ShakeDetector shakeDetector = ServiceRepository.getRepository().getShakeDetector(this);
    private VibrationService vibrationService = ServiceRepository.getRepository().getVibrationService(this);
    private AnimationHelper animationHelper = ServiceRepository.getRepository().getAnimationHelper();
    private GameService gameService = ServiceRepository.getRepository().getGameService();

    private View[] icons;

    private static final float INITIAL_ANGLE = PI / 2;


    private float calculateFinalAngle(GameModel model) {
        return 360 * model.getRotationCount() + (360f/icons.length)*model.getSelectedIcon();
    }

    private float computeRotationAngleInRadians(float degrees) {
        return INITIAL_ANGLE - (float)Math.toRadians(degrees);
    }

    private void setIconPositions(View triangle, float angle) {

        final int width = triangle.getWidth();
        final int height = triangle.getHeight();

        // angle between icons
        final float iconAngle = 2*PI/icons.length;
        final float radius = height*TWO_DIV_THREE;

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

        setIconPositions(triangle, INITIAL_ANGLE);

        shakeDetector.start(new ShakeDetector.ShakeListener() {
            public void onShake() {

                shakeDetector.pause();
                vibrationService.feedback();

                final GameModel gameModel = gameService.generateGameModel();

                final float fromAngle = 0;
                final float toAngle = calculateFinalAngle(gameModel);

                final ImageView triangle = (ImageView) findViewById(R.id.triangle);
                final RotateAnimation rotateAnimation = new RotateAnimation(fromAngle, toAngle,
                        RotateAnimation.RELATIVE_TO_SELF, HALF,
                        RotateAnimation.RELATIVE_TO_SELF, TWO_DIV_THREE);

                rotateAnimation.setInterpolator(new DecelerateInterpolator(gameModel.getDecelerateFactor()));
                rotateAnimation.setDuration(gameModel.getDuration());
                rotateAnimation.setFillEnabled(true);
                rotateAnimation.setFillAfter(true);
                rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationStart(final Animation animation) {
                        final ViewTreeObserver observer = triangle.getViewTreeObserver();
                        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            public boolean onPreDraw() {
                                if (rotateAnimation.hasEnded()) {
                                    observer.removeOnPreDrawListener(this);
                                    return true;
                                }

                                final float interpolation = animationHelper.computeInterpolation(animation);
                                final float degrees = (fromAngle + (toAngle - fromAngle) * interpolation) % 360; // degrees
                                final float angle = computeRotationAngleInRadians(degrees);
                                setIconPositions(triangle, angle);
                                return true;
                            }
                        });
                    }

                    public void onAnimationEnd(Animation animation) {
                        setIconPositions(triangle, computeRotationAngleInRadians(toAngle));
                        shakeDetector.resume();
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                animationHelper.restartAnimation(triangle, rotateAnimation);
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
        animationHelper.start(new AnimationHelper.Action() {
            public View execute() {
                imageView.setImageResource(R.drawable.logoscreen);
                return imageView;
            }
        }, new AnimationHelper.Action() {
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

