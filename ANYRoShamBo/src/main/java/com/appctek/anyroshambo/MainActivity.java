package com.appctek.anyroshambo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.appctek.R;
import com.appctek.anyroshambo.math.GeometryUtils;
import com.appctek.anyroshambo.math.Point;
import com.appctek.anyroshambo.model.GameModel;
import com.appctek.anyroshambo.services.*;
import com.appctek.anyroshambo.util.AnimationHandler;
import com.appctek.anyroshambo.util.AnimationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends HardwareAcceleratedActivity {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private static final float INITIAL_ANGLE = GeometryUtils.HALF_PI;
    private static final int[] goForResIds = new int[] {
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
    private ImageView goFor;
    private GameModel gameModel = new GameModel();

    private void stopListeners() {
        gameModel.setInProgress(true);
        triangle.setOnTouchListener(null);
        shakeDetector.pause();
    }

    private void initListeners() {
        triangle.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                final Point pt = Point.fromArray(event.getX(), event.getY());
                final Point t1 = Point.fromArray(v.getWidth()/2, 0),
                        t2 = Point.fromArray(v.getWidth(), v.getHeight()),
                        t3 = Point.fromArray(0, v.getHeight());
                if (GeometryUtils.ptInTriangle(pt, t1, t2, t3)) {
                    runOnUiThread(startGameAction);
                    return true;
                }
                return false;
            }
        });
        shakeDetector.start(new ShakeDetector.OnShakeListener() {
            public void onShake() {
                runOnUiThread(startGameAction);
            }
        });
        gameModel.setInProgress(false);
    }


    private float computeRotationAngleInRadians(float degrees) {
        return INITIAL_ANGLE - (float)Math.toRadians(degrees);
    }

    private void setIconPositions(float angle) {

        final int width = triangle.getWidth();
        final int height = triangle.getHeight();

        // angle between icons
        final float iconAngle = GeometryUtils.TWO_PI/icons.length;
        final float radius = GeometryUtils.calculateTriangleCenterY(height);

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

    private Runnable startGameAction = new Runnable() {
        public void run() {
            if (gameModel.isInProgress()) {
                return;
            }
            stopListeners();

            if (gameModel.getSelectedIcon() >= 0) {
                goFor.startAnimation(animationFactory.createGoForAnimationOut());
                glow.startAnimation(animationFactory.createGlowAnimationOut());
                icons[gameModel.getSelectedIcon()].startAnimation(animationFactory.createIconScaleOut());
            }

            vibrationService.feedback();
            gameService.initGame(gameModel);

            final Animation rotateAnimation = animationFactory.createRotate(gameModel);
            final ViewTreeObserver viewTreeObserver = triangle.getViewTreeObserver();
            viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    if (rotateAnimation.hasEnded()) {
                        viewTreeObserver.removeOnPreDrawListener(this);
                        return true;
                    }

                    final float interpolation = animationHelper.computeInterpolation(rotateAnimation);
                    final float degrees = (gameModel.getFromDegrees() +
                            (gameModel.getToDegrees() - gameModel.getFromDegrees()) * interpolation) % 360; // degrees
                    final float angle = computeRotationAngleInRadians(degrees);
                    setIconPositions(angle);
                    return true;
                }
            });
            rotateAnimation.setAnimationListener(new AnimationHandler() {
                public void onAnimationEnd(Animation animation) {
                    setIconPositions(computeRotationAngleInRadians(gameModel.getToDegrees()));
                    final Animation scaleAnimation = animationFactory.createIconScaleIn();
                    scaleAnimation.setAnimationListener(new AnimationHandler() {
                        public void onAnimationEnd(Animation animation) {
                            final Animation goForAnimation = animationFactory.createGoForAnimationIn();
                            goForAnimation.setAnimationListener(new AnimationHandler() {
                                public void onAnimationEnd(Animation animation) {
                                    initListeners();
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
    };

    private void initGame() {
        triangle = findViewById(R.id.triangle);
        glow = (ImageView)findViewById(R.id.glow);
        goFor = (ImageView)findViewById(R.id.go_for);
        icons = new View[] {findViewById(R.id.drink), findViewById(R.id.walk), findViewById(R.id.party)};
        setIconPositions(INITIAL_ANGLE);
        initListeners();
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
        });

    }

    public void showInfo() {
        final Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

}

