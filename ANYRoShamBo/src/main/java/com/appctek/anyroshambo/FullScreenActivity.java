package com.appctek.anyroshambo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-29-12
 */
class FullScreenActivity extends RoboActivity {

    @Inject
    private AppInfo appInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (BuildConfig.DEBUG) {
            final ViewGroup decorView = (ViewGroup)getWindow().getDecorView();
            final TextView textView = new TextView(this);
            textView.setText("Version: " + appInfo.getVersion());
            textView.setTextColor(Color.WHITE);
            decorView.addView(textView);
        }

    }

}
