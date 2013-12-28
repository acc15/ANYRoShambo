package com.appctek.anyroshambo;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;
import com.appctek.R;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-28-12
 */
public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        final Typeface typeface = Typeface.createFromAsset(getAssets(), "myriadpro.ttf");
        ((TextView)findViewById(R.id.info_header)).setTypeface(typeface);
        ((TextView)findViewById(R.id.info_text)).setTypeface(typeface);
    }
}
