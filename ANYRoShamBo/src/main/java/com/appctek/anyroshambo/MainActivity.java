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
import com.appctek.anyroshambo.services.AnimationFactory;
import com.appctek.anyroshambo.services.ShakeDetector;
import com.appctek.anyroshambo.services.VibrationService;
import com.appctek.anyroshambo.social.SocialNetworkService;
import com.appctek.anyroshambo.social.TwitterService;
import com.appctek.anyroshambo.social.auth.ErrorInfo;
import com.appctek.anyroshambo.util.Action;
import com.appctek.anyroshambo.util.MediaPlayerUtils;
import com.appctek.anyroshambo.util.RandomUtils;
import com.appctek.anyroshambo.util.ViewUtils;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import java.util.Random;

public class MainActivity extends HardwareAcceleratedActivity {

    private static final String ANIMATION_POSITION_KEY = "key.animationPosition";

    private static final float INITIAL_ANGLE = GeometryUtils.HALF_PI;

    private static final int ICON_COUNT = 3;
    private static final int[] goForResIds = new int[]{
            R.string.go_celebrate_text,
            R.string.go_forawalk_text,
            R.string.go_party_text
    };
    private static final int[] shareActionIds = new int[] {
            R.string.share_action_celebrate,
            R.string.share_action_walk,
            R.string.share_action_party
    };

    private static final int[] goForAudioIds = new int[]{
            R.raw.prazdnovaty,
            R.raw.gulaty,
            R.raw.tancevaty
    };

    @Inject
    private ShakeDetector shakeDetector;
    @Inject
    private VibrationService vibrationService;
    @Inject
    private Animator animator;
    @Inject
    private AnimationFactory animationFactory;
    @Inject
    private MediaPlayer mediaPlayer;
    @Inject
    private Random random;

    @Inject
    @Named("vkService")
    private SocialNetworkService vkService;
    @Inject
    @Named("okService")
    private SocialNetworkService okService;
    @Inject
    @Named("fbService")
    private SocialNetworkService fbService;
    @Inject
    @Named("twService")
    private SocialNetworkService twService;

    @InjectView(R.id.game_container)
    private FrameLayout gameContainer;
    @InjectView(R.id.triangle)
    private ImageView triangle;
    @InjectView(R.id.drink)
    private ImageView drinkIcon;
    @InjectView(R.id.walk)
    private ImageView walkIcon;
    @InjectView(R.id.party)
    private ImageView partyIcon;
    @InjectView(R.id.glow)
    private ImageView glow;
    @InjectView(R.id.go_for_label)
    private TextView goForLabel;
    @InjectView(R.id.splash)
    private ImageView splash;
    @InjectView(R.id.preloader)
    private View preloader;
    @InjectView(R.id.game_view)
    private View gameView;
    @InjectView(R.id.share_label)
    private TextView shareLabel;
    @InjectView(R.id.vk_button)
    private ImageButton vkButton;
    @InjectView(R.id.ok_button)
    private ImageButton okButton;
    @InjectView(R.id.fb_button)
    private ImageButton fbButton;
    @InjectView(R.id.tw_button)
    private ImageButton twButton;

    private ImageView[] icons;
    private int selectedIcon = -1;

    @InjectResource(R.string.share_link)
    private String shareLink;

    @InjectResource(R.string.share_title)
    private String shareTitle;

    @InjectResource(R.string.share_text)
    private String shareText;

    @InjectResource(R.string.share_action_text)
    private String shareActionText;


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

    private float calculateToDegrees() {
        return GeometryUtils.DEGREES_IN_CIRCLE / ICON_COUNT * selectedIcon;
    }

    private Sequencer gameSequencer = new Sequencer(new ActionSequence() {
        private ViewTreeObserver.OnPreDrawListener preDrawListener;

        public LazyAction executeStep(int step, Sequencer sequencer) {
            switch (step) {
            case 0:

                if (selectedIcon >= 0) {
                    goForLabel.setText(null);
                    glow.startAnimation(animationFactory.createGlowAnimationOut());
                    icons[selectedIcon].startAnimation(animationFactory.createIconScaleOut());
                }

                vibrationService.feedback();

                final float fromDegrees = calculateToDegrees();
                selectedIcon = random.nextInt(ICON_COUNT);

                final int rotationCount = RandomUtils.nextPositiveOrNegative(random, 8, 10);
                final float toDegrees = rotationCount * GeometryUtils.DEGREES_IN_CIRCLE + calculateToDegrees();

                final Animation rotateAnimation = animationFactory.createRotate(fromDegrees, toDegrees);
                this.preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        final float interpolation = animator.computeInterpolation(rotateAnimation);
                        final float degrees = GeometryUtils.interpolate(interpolation, fromDegrees, toDegrees) %
                                GeometryUtils.DEGREES_IN_CIRCLE;
                        setIconPositions(computeRotationAngleInRadians(degrees));
                        return true;
                    }
                };
                triangle.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
                MediaPlayerUtils.play(MainActivity.this, mediaPlayer, R.raw.ruletka);
                return animator.animate(triangle).with(rotateAnimation).build();

            case 1:
                triangle.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
                this.preDrawListener = null;

                setIconPositions(computeRotationAngleInRadians(calculateToDegrees()));
                glow.startAnimation(animationFactory.createGlowAnimationIn());
                return animator.animate(icons[selectedIcon]).
                        with(animationFactory.createIconScaleIn()).build();

            case 2:
                MediaPlayerUtils.play(MainActivity.this, mediaPlayer, goForAudioIds[selectedIcon]);
                goForLabel.setText(goForResIds[selectedIcon]);
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
        shakeDetector.resume();
        MediaPlayerUtils.unmute(mediaPlayer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }

    private final class ShareButtonListener implements View.OnLongClickListener {
        private SocialNetworkService socialNetworkService;

        private ShareButtonListener(SocialNetworkService socialNetworkService) {
            this.socialNetworkService = socialNetworkService;
        }

        public boolean onLongClick(View v) {
            shareGameResults(socialNetworkService, true);
            return true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_with_preloader);
        initContainer(gameContainer);

        vkButton.setOnLongClickListener(new ShareButtonListener(vkService));
        okButton.setOnLongClickListener(new ShareButtonListener(okService));
        fbButton.setOnLongClickListener(new ShareButtonListener(fbService));
        twButton.setOnLongClickListener(new ShareButtonListener(twService));

        icons = new ImageView[]{drinkIcon, walkIcon, partyIcon};
        goForLabel.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/kremlinctt.ttf"));
        shareLabel.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/myriadpro.ttf"));

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
        shareGameResults(vkService, false);
    }

    public void shareOk(View view) {
        shareGameResults(okService, false);
    }

    public void shareFb(View view) {
        shareGameResults(fbService, false);
    }

    public void shareTw(View view) {
        shareGameResults(twService, false);
    }

    private String getMessageForShare() {
        if (selectedIcon < 0 || gameSequencer.isRunning()) {
            return shareText;
        }
        final String action = getString(shareActionIds[selectedIcon]);
        return String.format(shareActionText, action);
    }

    private void shareGameResults(SocialNetworkService service, boolean forceAuth) {
        final String message = getMessageForShare();
        service.share(new SocialNetworkService.ShareParams().
                revoke(forceAuth).
                title(shareTitle).
                text(message).
                link(shareLink).
                onFinish(new Action<ErrorInfo>() {
                    public void execute(ErrorInfo error) {
                        if (error.is(SocialNetworkService.CommonError.USER_CANCELLED)) {
                            return;
                        }
                        final int messageResId;
                        if (error.is(TwitterService.Error.DUPLICATE_STATUS_ERROR)) {
                            messageResId = R.string.share_duplicate;
                        } else if (error.isError()) {
                            messageResId = R.string.share_error;
                        } else {
                            messageResId = R.string.share_success;
                        }
                        Toast.makeText(MainActivity.this, messageResId, Toast.LENGTH_LONG).show();
                    }
                }));
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

