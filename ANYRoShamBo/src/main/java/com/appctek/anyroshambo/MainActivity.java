package com.appctek.anyroshambo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.appctek.anyroshambo.anim.*;
import com.appctek.anyroshambo.math.GeometryUtils;
import com.appctek.anyroshambo.model.GameModel;
import com.appctek.anyroshambo.services.*;
import com.appctek.anyroshambo.util.ViewUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends HardwareAcceleratedActivity {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private static final String ANIMATION_POSITION_KEY = "key.animationPosition";

    private static final float INITIAL_ANGLE = GeometryUtils.HALF_PI;
    private static final int[] goForResIds = new int[]{
            R.drawable.text_gocelebrate,
            R.drawable.text_goforawalk,
            R.drawable.text_goparty
    };

    private ShakeDetector shakeDetector = ServiceRepository.getRepository().getShakeDetector(this);
    private VibrationService vibrationService = ServiceRepository.getRepository().getVibrationService(this);
    private Animator animator = ServiceRepository.getRepository().getAnimationHelper();
    private GameService gameService = ServiceRepository.getRepository().getGameService();
    private AnimationFactory animationFactory = ServiceRepository.getRepository().getAnimationFactory();

    private ImageView triangle;
    private View[]    icons;
    private ImageView glow;
    private ImageView goForLabel;
    private GameModel gameModel = new GameModel();

    private Sequencer mainSequencer = new Sequencer(new ActionSequence() {
        public LazyAction executeStep(int step, Sequencer sequencer) {
            switch (step) {
            case 0:
                setContentView(R.layout.splash);
                final ImageView imageView = (ImageView) findViewById(R.id.splash);
                imageView.setImageResource(R.drawable.logoscreen);
                return animator.animateInOut(imageView).
                        in(animationFactory.createSplashAnimationIn()).
                        out(animationFactory.createSplashAnimationOut()).
                        withDelay(5000).skipOnClick().build();

            case 1:
                initView(sequencer.getStep() == 0);
                break;
            }
            return null;
        }
    });

    private Sequencer gameSequencer = new Sequencer(new ActionSequence() {
        private ViewTreeObserver.OnPreDrawListener preDrawListener;

        public LazyAction executeStep(int step, Sequencer sequencer) {
            switch (step) {
            case 0:

                if (gameModel.getSelectedIcon() >= 0) {
                    goForLabel.setImageDrawable(null);
                    glow.startAnimation(animationFactory.createGlowAnimationOut());
                    icons[gameModel.getSelectedIcon()].startAnimation(animationFactory.createIconScaleOut());
                }

                vibrationService.feedback();
                gameService.initGame(gameModel);

                final Animation rotateAnimation = animationFactory.createRotate(gameModel);
                this.preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        final float interpolation = animator.computeInterpolation(rotateAnimation);
                        final float degrees = GeometryUtils.interpolate(interpolation,
                                gameModel.getFromDegrees(), gameModel.getToDegrees()) % GeometryUtils.DEGREES_IN_CIRCLE;
                        setIconPositions(computeRotationAngleInRadians(degrees));
                        return true;
                    }
                };
                triangle.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
                return animator.animate(triangle).with(rotateAnimation).build();

            case 1:
                triangle.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
                this.preDrawListener = null;

                setIconPositions(computeRotationAngleInRadians(gameModel.getToDegrees()));
                final View selectedIcon = icons[gameModel.getSelectedIcon()];
                setIconGlow(selectedIcon);
                return animator.animate(selectedIcon).with(animationFactory.createIconScaleIn()).build();

            case 2:
                goForLabel.setImageResource(goForResIds[gameModel.getSelectedIcon()]);
                return animator.animate(goForLabel).with(animationFactory.createGoForAnimationIn()).build();

            case 3:
                gameSequencer.reset();
                break;
            }
            return null;
        }
    });

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
        final int initialStep = savedInstanceState != null ? savedInstanceState.getInt(ANIMATION_POSITION_KEY, 0) : 0;
        logger.debug("Starting animation from " + initialStep + " step");
        mainSequencer.run(initialStep);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ANIMATION_POSITION_KEY, mainSequencer.getStep());
    }

    public void showInfo(View view) {
        final Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private float computeRotationAngleInRadians(float degrees) {
        return INITIAL_ANGLE - (float) Math.toRadians(degrees);
    }

    private void setIconPositions(float angle) {

        final int width = triangle.getWidth();
        final int height = triangle.getHeight();

        // angle between icons
        final float iconAngle = GeometryUtils.TWO_PI / icons.length;
        final float radius = GeometryUtils.calculateTriangleCenterY(height);

        final float centerX = triangle.getLeft() + width / 2,
                centerY = triangle.getTop() + radius;
        for (final View icon : icons) {

            final int iconWidth = icon.getWidth();
            final int iconHeight = icon.getHeight();

            final float xAbsolute = (float) Math.cos(angle) * radius;
            final float yAbsolute = (float) Math.sin(angle) * radius;

            final float x = centerX + xAbsolute - iconWidth / 2;
            final float y = centerY - yAbsolute - iconHeight / 2;

            final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) icon.getLayoutParams();
            layoutParams.leftMargin = (int) x;
            layoutParams.topMargin = (int) y;
            icon.setLayoutParams(layoutParams);
            angle += iconAngle;

        }
    }

    private void setIconGlow(View icon) {
        final float glowScale = 2f;

        final RelativeLayout.LayoutParams iconParams = (RelativeLayout.LayoutParams)icon.getLayoutParams();
        final int w = icon.getWidth(), h = icon.getHeight();
        final float scaledWidth = w * glowScale;
        final float scaledHeight = h * glowScale;
        final float x = iconParams.leftMargin - (scaledWidth - w) / 2;
        final float y = iconParams.topMargin - (scaledHeight - h) / 2;

        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) glow.getLayoutParams();
        layoutParams.leftMargin = (int) x;
        layoutParams.topMargin = (int) y;
        layoutParams.width = (int) scaledWidth;
        layoutParams.height = (int) scaledHeight;
        glow.setLayoutParams(layoutParams);
        glow.startAnimation(animationFactory.createGlowAnimationIn());
    }

    private void initGame() {
        if (BuildConfig.DEBUG) {
            triangle.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    gameSequencer.run();
                }
            });
        }
        shakeDetector.start(new ShakeDetector.OnShakeListener() {
            public void onShake() {
                runOnUiThread(gameSequencer);
            }
        });
        setIconPositions(INITIAL_ANGLE);

    }

    private void initView(final boolean doAnimate) {

        setContentView(R.layout.game_with_preloader);
        triangle = (ImageView)findViewById(R.id.triangle);
        glow = (ImageView) findViewById(R.id.glow);
        goForLabel = (ImageView) findViewById(R.id.go_for);
        icons = new View[]{findViewById(R.id.drink), findViewById(R.id.walk), findViewById(R.id.party)};

        final ViewGroup gameContainer = (ViewGroup)findViewById(R.id.game_container);
        gameContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {

                final View gameView = findViewById(R.id.game_view);
                if (gameView.getWidth() != gameContainer.getWidth() ||
                    gameView.getHeight() != gameContainer.getHeight()) {
                    ViewUtils.scaleComponents(gameContainer, gameView);
                    return;
                }

                gameContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                initGame();

                final View preloader = findViewById(R.id.preloader);
                gameContainer.removeView(preloader);

                if (doAnimate) {
                    gameContainer.startAnimation(animationFactory.createSplashAnimationIn());
                }
            }
        });
    }


}

