package com.appctek.anyroshambo.services;

import android.view.animation.AnimationUtils;

/**
 * For testing purposes. To allow mocking of current time
 * @author Vyacheslav Mayorov
 * @since 2013-23-12
 */
public class DateTimeService {

    public long getTimeInMillis() {
        return System.currentTimeMillis();
    }

    public long getAnimationTimeMillis() {
        return AnimationUtils.currentAnimationTimeMillis();
    }

}
