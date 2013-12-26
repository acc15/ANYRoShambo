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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.appctek.R;
import com.appctek.anyroshambo.model.GameModel;
import com.appctek.anyroshambo.services.AnimationFactory;
import com.appctek.anyroshambo.services.GameService;
import com.appctek.anyroshambo.services.ServiceRepository;
import com.appctek.anyroshambo.services.VibrationService;
import com.appctek.anyroshambo.util.AnimationHandler;
import com.appctek.anyroshambo.util.AnimationHelper;
import com.appctek.anyroshambo.util.ShakeDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends Activity implements ShakeDetector.ShakeListener {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private static final float PI = (float)Math.PI;


    // to make sure it compiles using old android SDK jars
    private static final int FLAG_HARDWARE_ACCELERATED = 0x01000000;
    private static final int SDK_VERSION_HONEYCOMB = 11;

    private ShakeDetector shakeDetector = ServiceRepository.getRepository().getShakeDetector(this);
    private VibrationService vibrationService = ServiceRepository.getRepository().getVibrationService(this);
    private AnimationHelper animationHelper = ServiceRepository.getRepository().getAnimationHelper();
    private GameService gameService = ServiceRepository.getRepository().getGameService();
    private AnimationFactory animationFactory = ServiceRepository.getRepository().getAnimationFactory();

    private View[] icons;
    private ImageView glow;
    private ImageView goFor;

    private static final int[] goForResIds = new int[] {
            R.drawable.text_gocelebrate,
            R.drawable.text_goforawalk,
            R.drawable.text_goparty
    };

    private GameModel gameModel = new GameModel();

    private static final float INITIAL_ANGLE = PI / 2;

    private float computeRotationAngleInRadians(float degrees) {
        return INITIAL_ANGLE - (float)Math.toRadians(degrees);
    }

    private void setIconPositions(View triangle, float angle) {

        final int width = triangle.getWidth();
        final int height = triangle.getHeight();

        // angle between icons
        final float iconAngle = 2*PI/icons.length;
        final float radius = height* AnimationFactory.TWO_DIV_THREE;

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

    private void setIconGlow(View icon) {
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)glow.getLayoutParams();

        final float glowScale = 2f;
        final float scaledWidth = icon.getWidth() * glowScale;
        final float scaledHeight = icon.getHeight() * glowScale;
        final float x = icon.getLeft() - (scaledWidth - icon.getWidth())/2;
        final float y = icon.getTop() - (scaledHeight - icon.getHeight())/2;

        layoutParams.leftMargin = (int)x;
        layoutParams.topMargin = (int)y;
        layoutParams.width = (int)scaledWidth;
        layoutParams.height = (int)scaledHeight;
        glow.setLayoutParams(layoutParams);
        glow.startAnimation(animationFactory.createGlowAnimationIn());
    }

    public void onShake() {

        shakeDetector.pause();
        if (gameModel.getSelectedIcon() >= 0) {
            goFor.startAnimation(animationFactory.createGoForAnimationOut());
            glow.startAnimation(animationFactory.createGlowAnimationOut());
            icons[gameModel.getSelectedIcon()].startAnimation(animationFactory.createIconScaleOut());
        }

        vibrationService.feedback();
        gameService.initGame(gameModel);

        final ImageView triangle = (ImageView) findViewById(R.id.triangle);
        final Animation rotateAnimation = animationFactory.createRotate(gameModel);
        triangle.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                if (rotateAnimation.hasEnded()) {
                    triangle.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }

                final float interpolation = animationHelper.computeInterpolation(rotateAnimation);
                final float degrees = (gameModel.getFromDegrees() +
                        (gameModel.getToDegrees() - gameModel.getFromDegrees()) * interpolation) % 360; // degrees
                final float angle = computeRotationAngleInRadians(degrees);
                setIconPositions(triangle, angle);
                return true;
            }
        });
        rotateAnimation.setAnimationListener(new AnimationHandler() {
            public void onAnimationEnd(Animation animation) {
                setIconPositions(triangle, computeRotationAngleInRadians(gameModel.getToDegrees()));
                final Animation scaleAnimation = animationFactory.createIconScaleIn();
                scaleAnimation.setAnimationListener(new AnimationHandler() {
                    public void onAnimationEnd(Animation animation) {
                        final Animation goForAnimation = animationFactory.createGoForAnimationIn();
                        goForAnimation.setAnimationListener(new AnimationHandler() {
                            public void onAnimationEnd(Animation animation) {
                                shakeDetector.resume();
                            }
                        });
                        goFor.setImageResource(goForResIds[gameModel.getSelectedIcon()]);
                        goFor.startAnimation(goForAnimation);
                    }
                });

                final View selectedIcon = icons[gameModel.getSelectedIcon()];
                setIconGlow(selectedIcon);
                selectedIcon.startAnimation(scaleAnimation);
            }
        });
        triangle.startAnimation(rotateAnimation);
    }

    private void initGame() {

        final ImageView triangle = (ImageView)findViewById(R.id.triangle);
        glow = (ImageView)findViewById(R.id.glow);
        goFor = (ImageView)findViewById(R.id.go_for);

        icons = new View[] {findViewById(R.id.drink), findViewById(R.id.walk), findViewById(R.id.party)};
        setIconPositions(triangle, INITIAL_ANGLE);
        shakeDetector.start(this);

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
                        initGame();
                        gameView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });
                setContentView(gameView);
                return gameView;
            }
        });

    }


}

