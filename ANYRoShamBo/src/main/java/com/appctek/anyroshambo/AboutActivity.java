package com.appctek.anyroshambo;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import com.appctek.anyroshambo.services.AdService;
import com.appctek.anyroshambo.util.ViewUtils;
import com.google.inject.Inject;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-28-12
 */
public class AboutActivity extends FullScreenActivity {

    @Inject
    private AdService adService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adService.init(this);
        setContentView(R.layout.about);

        adService.addBanner(this);

        final Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/myriadpro.ttf");
        ((TextView)findViewById(R.id.info_header)).setTypeface(typeface);
        ((TextView)findViewById(R.id.info_text)).setTypeface(typeface);

        ((TextView)findViewById(R.id.main_url)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView)findViewById(R.id.vk_url)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView)findViewById(R.id.tw_url)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView)findViewById(R.id.ig_url)).setMovementMethod(LinkMovementMethod.getInstance());

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
