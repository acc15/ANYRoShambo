package com.appctek.anyroshambo.services;

import android.view.animation.*;
import com.appctek.anyroshambo.math.GeometryUtils;
import com.appctek.anyroshambo.model.GameModel;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-26-12
 */
public class AnimationFactory {

    private static final float ICON_SCALE_FACTOR = 1.4f;

    private static Animation enableFillAfter(Animation animation) {
        animation.setFillEnabled(true);
        animation.setFillAfter(true);
        return animation;
    }

    public Animation createRotate(GameModel gameModel) {
        final RotateAnimation rotateAnimation = new RotateAnimation(gameModel.getFromDegrees(), gameModel.getToDegrees(),
                RotateAnimation.RELATIVE_TO_SELF, GeometryUtils.HALF,
                RotateAnimation.RELATIVE_TO_SELF, GeometryUtils.TWO_DIV_THREE);
        rotateAnimation.setInterpolator(new DecelerateInterpolator(gameModel.getDecelerateFactor()));
        rotateAnimation.setDuration(gameModel.getDuration());
        return enableFillAfter(rotateAnimation);
    }

    public Animation createIconScaleOut() {
        final ScaleAnimation scaleAnimation = new ScaleAnimation(ICON_SCALE_FACTOR, 1, ICON_SCALE_FACTOR, 1,
                Animation.RELATIVE_TO_SELF, GeometryUtils.HALF,
                Animation.RELATIVE_TO_SELF, GeometryUtils.HALF);
        scaleAnimation.setDuration(200);
        scaleAnimation.setInterpolator(new LinearInterpolator());
        return enableFillAfter(scaleAnimation);
    }

    public Animation createIconScaleIn() {
        final ScaleAnimation scaleAnimation = new ScaleAnimation(1, ICON_SCALE_FACTOR, 1, ICON_SCALE_FACTOR,
                Animation.RELATIVE_TO_SELF, GeometryUtils.HALF,
                Animation.RELATIVE_TO_SELF, GeometryUtils.HALF);
        scaleAnimation.setDuration(500);
        scaleAnimation.setInterpolator(new BounceInterpolator());
        return enableFillAfter(scaleAnimation);
    }

    public Animation createGlowAnimationIn() {
        final AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setInterpolator(new DecelerateInterpolator());
        enableFillAfter(alphaAnimation);

        final RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, GeometryUtils.HALF,
                Animation.RELATIVE_TO_SELF, GeometryUtils.HALF);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(5000);
        enableFillAfter(rotateAnimation);

        final AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(rotateAnimation);
        return enableFillAfter(animationSet);
    }

    public Animation createGlowAnimationOut() {
        final AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0);
        alphaAnimation.setDuration(50);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        return enableFillAfter(alphaAnimation);
    }

    public Animation createGoForAnimationIn() {
        final AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setInterpolator(new BounceInterpolator());
        return enableFillAfter(alphaAnimation);
    }

    public Animation createSplashAnimationIn() {
        return createFadeAnimation(1000, 0, 1f);
    }

    public Animation createSplashAnimationOut() {
        return createFadeAnimation(1000, 1f, 0);
    }

    private Animation createFadeAnimation(long duration, float from, float to) {
        final AlphaAnimation alphaAnimation = new AlphaAnimation(from, to);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        return enableFillAfter(alphaAnimation);
    }
}
