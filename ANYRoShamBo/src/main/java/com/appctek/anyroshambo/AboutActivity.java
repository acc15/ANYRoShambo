package com.appctek.anyroshambo;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.appctek.anyroshambo.services.AdService;
import com.appctek.anyroshambo.util.ViewUtils;
import com.google.inject.Inject;
import roboguice.inject.InjectView;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-28-12
 */
public class AboutActivity extends FullScreenActivity {

    @Inject
    private AdService adService;

    @InjectView(R.id.about_screen) private RelativeLayout aboutScreen;
    @InjectView(R.id.info_header) private TextView infoHeader;
    @InjectView(R.id.info_text) private TextView infoText;
    @InjectView(R.id.main_url) private TextView mainUrl;
    @InjectView(R.id.vk_url) private TextView vkontakteUrl;
    @InjectView(R.id.tw_url) private TextView twitterUrl;
    @InjectView(R.id.ig_url) private TextView instagramUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adService.init(this);
        setContentView(R.layout.about);
        adService.addBanner(aboutScreen);

        final Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/myriadpro.ttf");
        infoHeader.setTypeface(typeface);
        infoText.setTypeface(typeface);

        mainUrl.setMovementMethod(LinkMovementMethod.getInstance());
        vkontakteUrl.setMovementMethod(LinkMovementMethod.getInstance());
        twitterUrl.setMovementMethod(LinkMovementMethod.getInstance());
        instagramUrl.setMovementMethod(LinkMovementMethod.getInstance());

        final View container = findViewById(android.R.id.content);
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                ViewUtils.scaleComponents(container, aboutScreen);
            }
        });

    }
}
