package com.appctek.anyroshambo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import com.appctek.anyroshambo.roboguice.EventRoboActivity;
import com.appctek.anyroshambo.services.AdService;
import com.appctek.anyroshambo.util.ViewUtils;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-29-12
 */
class FullScreenActivity extends EventRoboActivity {

    private static final Logger logger = LoggerFactory.getLogger(FullScreenActivity.class);

    @Inject
    private AdService adService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        adService.init(this);
    }

    private static void printFields(Class<?> clazz, StringBuilder sb, String prefix) {
        for (final Field f : clazz.getFields()) {
            final Object val;
            try {
                val = f.get(null);
            } catch (IllegalAccessException e) {
                logger.error("Can't get field \"" + f.getName() + "\" value", e);
                continue;
            }
            sb.append(prefix).append('.').append(f.getName()).append(": ").append(val).append('\n');
        }
    }

    public void initContainer(ViewGroup rootContainer) {
        if (BuildConfig.DEBUG) {
            final TextView textView = new TextView(this);
            textView.setText("Version: " + AppBuild.VERSION);
            textView.setTextColor(Color.WHITE);
            textView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    final StringBuilder info = new StringBuilder();
                    info.append("App Version: ").append(AppBuild.VERSION).append('\n');
                    info.append("App Ad Enabled: ").append(AppBuild.AD_ENABLED).append('\n');
                    printFields(Build.class, info, "Build");
                    printFields(Build.VERSION.class, info, "SDK");

                    final Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{"vyacheslav.mayorov@gmail.com"});
                    i.putExtra(Intent.EXTRA_SUBJECT, "ANYRoShamBo Debug info");
                    i.putExtra(Intent.EXTRA_TEXT, info.toString());
                    try {
                        startActivity(Intent.createChooser(i, "Send debug info"));
                    } catch (android.content.ActivityNotFoundException ex) {
                        logger.error("Can't send debug info", ex);
                    }

                }
            });
            ViewUtils.addViewToContainer(rootContainer, textView, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        }
        adService.addBanner(rootContainer);
    }

}
