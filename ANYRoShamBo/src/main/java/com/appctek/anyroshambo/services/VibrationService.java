package com.appctek.anyroshambo.services;

import android.os.Vibrator;
import com.google.inject.Inject;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-26-12
 */
public class VibrationService {

    private static final int FEEDBACK_TIME = 100;
    private Vibrator vibrator;

    @Inject
    public VibrationService(Vibrator vibrator) {
        this.vibrator = vibrator;
    }

    public void feedback() {
        vibrator.vibrate(FEEDBACK_TIME);
    }

}
