package com.appctek.anyroshambo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import roboguice.activity.RoboActivity;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-29-12
 */
class FullScreenActivity extends RoboActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        if (BuildConfig.DEBUG) {
            final ViewGroup contentView = (ViewGroup)findViewById(android.R.id.content);
            final TextView textView = new TextView(this);
            textView.setText("Version: " + AppBuild.VERSION);
            textView.setTextColor(Color.WHITE);
            contentView.addView(textView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL | Gravity.TOP));
        }
    }
}
