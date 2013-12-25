package com.appctek.anyroshambo.services;

import android.content.Context;
import android.os.Vibrator;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-26-12
 */
public class VibrationService {

    private static final int FEEDBACK_TIME = 100;
    private Context context;

    public VibrationService(Context context) {
        this.context = context;
    }

    public void feedback() {
        final Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(FEEDBACK_TIME);
    }

}
