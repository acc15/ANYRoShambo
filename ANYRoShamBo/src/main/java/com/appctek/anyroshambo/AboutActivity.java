package com.appctek.anyroshambo;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import com.appctek.anyroshambo.util.ViewUtils;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-28-12
 */
public class AboutActivity extends FullScreenActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        final Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/myriadpro.ttf");
        ((TextView)findViewById(R.id.info_header)).setTypeface(typeface);
        ((TextView)findViewById(R.id.info_text)).setTypeface(typeface);

        final View container = findViewById(android.R.id.content);
        final View rootView = findViewById(R.id.about_screen);
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                ViewUtils.scaleComponents(container, rootView);
            }
        });

    }
}
