package com.appctek.anyroshambo.util;

import android.view.animation.Animation;
import android.view.animation.Interpolator;
import com.appctek.anyroshambo.services.DateTimeService;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-26-12
 */
public class AnimationHelper {

    private DateTimeService dateTimeService;

    public AnimationHelper(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public float computeInterpolation(Animation animation) {
        final long currentTime = dateTimeService.getAnimationTimeMillis();
        final long startTime = animation.getStartTime();
        final long duration = animation.getDuration();
        final long time = currentTime - startTime;
        if (time >= duration) {
            return 1.0f;
        }

        final float timePosition = (float)time/duration;
        final Interpolator interpolator = animation.getInterpolator();
        return interpolator.getInterpolation(timePosition);
    }

}
