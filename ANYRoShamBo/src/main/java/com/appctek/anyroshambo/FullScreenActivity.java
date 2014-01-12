package com.appctek.anyroshambo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.appctek.anyroshambo.services.AdService;
import com.appctek.anyroshambo.util.ViewUtils;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-29-12
 */
class FullScreenActivity extends RoboActivity {

    @Inject private AdService adService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        adService.init(this);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        final ViewGroup contentView = (ViewGroup)findViewById(android.R.id.content);
        final ViewGroup rootContainer = (ViewGroup)contentView.getChildAt(0);
        if (BuildConfig.DEBUG) {
            final TextView textView = new TextView(this);
            textView.setText("Version: " + AppBuild.VERSION);
            textView.setTextColor(Color.WHITE);
            ViewUtils.addViewToContainer(rootContainer, textView, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        }
        adService.addBanner(rootContainer);
        rootContainer.invalidate();

    }
}
