package com.appctek.anyroshambo;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.*;
import com.appctek.anyroshambo.anim.ActionSequence;
import com.appctek.anyroshambo.anim.Animator;
import com.appctek.anyroshambo.anim.LazyAction;
import com.appctek.anyroshambo.anim.Sequencer;
import com.appctek.anyroshambo.math.GeometryUtils;
import com.appctek.anyroshambo.model.GameModel;
import com.appctek.anyroshambo.services.AnimationFactory;
import com.appctek.anyroshambo.services.GameService;
import com.appctek.anyroshambo.services.ShakeDetector;
import com.appctek.anyroshambo.services.VibrationService;
import com.appctek.anyroshambo.social.SocialNetworkService;
import com.appctek.anyroshambo.util.MediaPlayerUtils;
import com.appctek.anyroshambo.util.ViewUtils;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import roboguice.inject.InjectView;

public class MainActivity extends HardwareAcceleratedActivity {

    private static final String ANIMATION_POSITION_KEY = "key.animationPosition";

    private static final float INITIAL_ANGLE = GeometryUtils.HALF_PI;
    private static final int[] goForResIds = new int[]{
            R.string.go_celebrate_text,
            R.string.go_forawalk_text,
            R.string.go_party_text
    };

    private static final int[] goForAudioIds = new int[] {
            R.raw.prazdnovaty,
            R.raw.gulaty,
            R.raw.tancevaty
    };

    @Inject private ShakeDetector shakeDetector;
    @Inject private VibrationService vibrationService;
    @Inject private Animator animator;
    @Inject private GameService gameService;
    @Inject private AnimationFactory animationFactory;
    @Inject private MediaPlayer mediaPlayer;

    @Inject @Named("vkontakteService") private SocialNetworkService vkontakteService;

    @InjectView(R.id.game_container) private FrameLayout gameContainer;
    @InjectView(R.id.triangle) private ImageView triangle;
    @InjectView(R.id.drink) private ImageView drinkIcon;
    @InjectView(R.id.walk) private ImageView walkIcon;
    @InjectView(R.id.party) private ImageView partyIcon;
    @InjectView(R.id.glow) private ImageView glow;
    @InjectView(R.id.go_for) private TextView goForLabel;
    @InjectView(R.id.splash) private ImageView splash;
    @InjectView(R.id.preloader) private View preloader;
    @InjectView(R.id.game_view) private View gameView;
    @InjectView(R.id.share_text) private TextView shareText;

    private ImageView[] icons;
    private GameModel gameModel = new GameModel();

    private Sequencer mainSequencer = new Sequencer(new ActionSequence() {
        public LazyAction executeStep(int step, final Sequencer sequencer) {
            switch (step) {
            case 0:
                return animator.animateInOut(splash).
                        in(animationFactory.createSplashAnimationIn()).
                        out(animationFactory.createSplashAnimationOut()).
                        withDelay(5000).skipOnClick().build();

            case 1:
                gameContainer.post(new Runnable() {
                    public void run() {
                        initView(sequencer.getStep() == 0);
                    }
                });
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
                    goForLabel.setText(null);
                    glow.startAnimation(animationFactory.createGlowAnimationOut());
                    icons[gameModel.getSelectedIcon()].startAnimation(animationFactory.createIconScaleOut());
                }

                vibrationService.feedback();
                gameService.initGame(gameModel);

                MediaPlayerUtils.play(MainActivity.this, mediaPlayer, R.raw.ruletka);

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
                glow.startAnimation(animationFactory.createGlowAnimationIn());
                return animator.animate(icons[gameModel.getSelectedIcon()]).
                        with(animationFactory.createIconScaleIn()).build();

            case 2:
                MediaPlayerUtils.play(MainActivity.this, mediaPlayer, goForAudioIds[gameModel.getSelectedIcon()]);
                goForLabel.setText(goForResIds[gameModel.getSelectedIcon()]);
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
        MediaPlayerUtils.mute(mediaPlayer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaPlayerUtils.unmute(mediaPlayer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_with_preloader);
        initContainer(gameContainer);

        icons = new ImageView[] {drinkIcon, walkIcon, partyIcon};
        goForLabel.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/kremlinctt.ttf"));
        shareText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/myriadpro.ttf"));

        gameContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (!ViewUtils.scaleComponents(gameContainer, gameView)) {
                    return;
                }
                gameContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                setIconPositions(INITIAL_ANGLE);
            }
        });

        final int initialStep = savedInstanceState != null ? savedInstanceState.getInt(ANIMATION_POSITION_KEY, 0) : 0;
        mainSequencer.run(initialStep);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ANIMATION_POSITION_KEY, mainSequencer.getStep());
    }

    public void showAbout(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void shareVk(View view) {
        vkontakteService.shareText(false, "WTF? Where is this fucking message on my wall");
    }

    public void shareOk(View view) {
    }

    public void shareFb(View view) {
    }

    public void shareTw(View view) {
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

    private void initView(final boolean doAnimate) {

        gameContainer.removeView(splash);
        gameContainer.removeView(preloader);
        gameContainer.invalidate();

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
        if (doAnimate) {
            gameView.startAnimation(animationFactory.createSplashAnimationIn());
        }

    }


}

