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
import com.appctek.anyroshambo.services.*;
import com.appctek.anyroshambo.util.AnimationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends HardwareAcceleratedActivity {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

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


    private static class SequentialAnimator {

        interface SequentialListener {
            View onAction(int step, SequentialAnimator animator);
        }

        private int step = -1;

        private void executeStep(final int step, final SequentialListener listener) {
            final View view = listener.onAction(step, this);
            if (view == null) {
                return;
            }

            view.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    SequentialAnimator.this.step = step;
                    executeStep(step + 1, listener);
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
        }

        public int getStep() {
            return this.step;
        }

        public void start(int initialStep, SequentialListener listener) {
            executeStep(initialStep, listener);
        }

    }

    public void startGame() {
        if (gameModel.isInProgress()) {
            return;
        }
        gameModel.setInProgress(true);

        if (gameModel.getSelectedIcon() >= 0) {
            goForLabel.startAnimation(animationFactory.createGoForAnimationOut());
            glow.startAnimation(animationFactory.createGlowAnimationOut());
            icons[gameModel.getSelectedIcon()].startAnimation(animationFactory.createIconScaleOut());
        }

        vibrationService.feedback();
        gameService.initGame(gameModel);

        new SequentialAnimator().start(0, new SequentialAnimator.SequentialListener() {
            private ViewTreeObserver.OnPreDrawListener preDrawListener;

            public View onAction(int step, SequentialAnimator animator) {
                switch (step) {
                case 0:
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
                    triangle.startAnimation(rotateAnimation);
                    return triangle;

                case 1:
                    triangle.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
                    this.preDrawListener = null;

                    setIconPositions(computeRotationAngleInRadians(gameModel.getToDegrees()));
                    final Animation scaleAnimation = animationFactory.createIconScaleIn();
                    final View selectedIcon = icons[gameModel.getSelectedIcon()];
                    setIconGlow(selectedIcon);
                    selectedIcon.startAnimation(scaleAnimation);
                    return selectedIcon;

                case 2:
                    final Animation goForAnimation = animationFactory.createGoForAnimationIn();
                    goForLabel.setImageResource(goForResIds[gameModel.getSelectedIcon()]);
                    goForLabel.startAnimation(goForAnimation);
                    return goForLabel;

                case 3:
                    gameModel.setInProgress(false);
                    break;
                }
                return null;
            }
        });
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
                        startGame();
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

        // TODO handle restore
        logger.debug("Initializing main activity...");
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
                              }
        );

    }

    public void showInfo(View view) {
        final Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

}

