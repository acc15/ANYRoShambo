package com.appctek.anyroshambo;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-27-12
 */
public class HardwareAcceleratedActivity extends FullScreenActivity {

    // to make sure it compiles using old android SDK jars
    private static final int FLAG_HARDWARE_ACCELERATED = 0x01000000;
    private static final int SDK_VERSION_HONEYCOMB = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // enable hardware acceleration
        if (Build.VERSION.SDK_INT >= SDK_VERSION_HONEYCOMB) {
            getWindow().setFlags(
                    FLAG_HARDWARE_ACCELERATED,
                    FLAG_HARDWARE_ACCELERATED);
        }
        findViewById(android.R.id.content).setBackgroundColor(Color.BLACK);

    }
}
