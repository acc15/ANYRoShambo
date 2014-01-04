package com.appctek.anyroshambo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.appctek.R;
import com.appctek.anyroshambo.math.GeometryUtils;
import com.appctek.anyroshambo.model.GameModel;
import com.appctek.anyroshambo.sequences.*;
import com.appctek.anyroshambo.services.*;
import com.appctek.anyroshambo.util.AnimationHelper;
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
    private AnimationHelper animationHelper = ServiceRepository.getRepository().getAnimationHelper();
    private GameService gameService = ServiceRepository.getRepository().getGameService();
    private AnimationFactory animationFactory = ServiceRepository.getRepository().getAnimationFactory();

    private View triangle;
    private View[] icons;
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
                final Sequencer splashSeq = new Sequencer(new ActionSequence() {
                    private Animation fadeIn;
                    public LazyAction executeStep(int step, Sequencer sequencer) {
                        switch (step) {
                        case 0:
                            fadeIn = animationFactory.createSplashAnimationIn();
                            return new AnimationAction(imageView, fadeIn);

                        case 1:
                            return new DelayAction(imageView, 5000);

                        case 2:
                            final float interpolation = animationHelper.computeInterpolation(fadeIn);
                            return new AnimationAction(imageView,
                                    animationFactory.createSplashAnimationOut(interpolation));
                        }
                        return null;
                    }
                });
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        splashSeq.execute(2);
                    }
                });
                return splashSeq;

            case 1:
                final View gameView = getLayoutInflater().inflate(R.layout.game, null);
                gameView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        initGame();
                        gameView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });
                setContentView(gameView);
                return sequencer.getStep() == 0
                        ? new AnimationAction(gameView, animationFactory.createSplashAnimationIn())
                        : null;
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
                    goForLabel.startAnimation(animationFactory.createGoForAnimationOut());
                    glow.startAnimation(animationFactory.createGlowAnimationOut());
                    icons[gameModel.getSelectedIcon()].startAnimation(animationFactory.createIconScaleOut());
                }

                vibrationService.feedback();
                gameService.initGame(gameModel);

                final Animation rotateAnimation = animationFactory.createRotate(gameModel);
                this.preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        final float interpolation = animationHelper.computeInterpolation(rotateAnimation);
                        final float degrees = (gameModel.getFromDegrees() +
                                (gameModel.getToDegrees() - gameModel.getFromDegrees()) * interpolation) % 360; // degrees
                        final float angle = computeRotationAngleInRadians(degrees);
                        setIconPositions(angle);
                        return true;
                    }
                };
                triangle.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
                return new AnimationAction(triangle, rotateAnimation);

            case 1:
                triangle.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
                this.preDrawListener = null;
                setIconPositions(computeRotationAngleInRadians(gameModel.getToDegrees()));
                final View selectedIcon = icons[gameModel.getSelectedIcon()];
                setIconGlow(selectedIcon);
                return new AnimationAction(selectedIcon, animationFactory.createIconScaleIn());

            case 2:
                goForLabel.setImageResource(goForResIds[gameModel.getSelectedIcon()]);
                return new AnimationAction(goForLabel, animationFactory.createGoForAnimationIn());

            case 3:
                sequencer.reset();
                break;
            }
            return null;
        }
    });

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
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) glow.getLayoutParams();

        final float glowScale = 2f;
        final float scaledWidth = icon.getWidth() * glowScale;
        final float scaledHeight = icon.getHeight() * glowScale;
        final float x = icon.getLeft() - (scaledWidth - icon.getWidth()) / 2;
        final float y = icon.getTop() - (scaledHeight - icon.getHeight()) / 2;

        layoutParams.leftMargin = (int) x;
        layoutParams.topMargin = (int) y;
        layoutParams.width = (int) scaledWidth;
        layoutParams.height = (int) scaledHeight;
        glow.setLayoutParams(layoutParams);
        glow.startAnimation(animationFactory.createGlowAnimationIn());
    }

    private void initGame() {
        triangle = findViewById(R.id.triangle);
        glow = (ImageView) findViewById(R.id.glow);
        goForLabel = (ImageView) findViewById(R.id.go_for);
        icons = new View[]{findViewById(R.id.drink), findViewById(R.id.walk), findViewById(R.id.party)};
        setIconPositions(INITIAL_ANGLE);
        shakeDetector.start(new ShakeDetector.OnShakeListener() {
            public void onShake() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        gameSequencer.execute();
                    }
                });
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
        final int initialStep = savedInstanceState != null ? savedInstanceState.getInt(ANIMATION_POSITION_KEY, 0) : 0;
        logger.debug("Starting animation from " + initialStep + " step");
        mainSequencer.execute(initialStep);
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

}

