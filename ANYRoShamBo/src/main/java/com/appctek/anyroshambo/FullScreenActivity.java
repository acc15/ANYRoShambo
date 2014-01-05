package com.appctek.anyroshambo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.appctek.anyroshambo.services.ServiceRepository;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-29-12
 */
public class FullScreenActivity extends Activity {

    private AppInfo appInfo = ServiceRepository.getRepository().getAppInfo();

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
