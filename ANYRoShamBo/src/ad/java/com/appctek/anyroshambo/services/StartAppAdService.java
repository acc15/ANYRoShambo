package com.appctek.anyroshambo.services;

import android.app.Activity;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.searchboxsdk.android.StartAppSearch;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.banner.Banner;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-08-01
 */
public class StartAppAdService implements AdService {

    private String developerId;
    private String appId;

    public StartAppAdService() {
        this("101531070", "201032454");
    }

    public StartAppAdService(String developerId, String appId) {
        this.developerId = developerId;
        this.appId = appId;
    }

    private boolean isSearchSdkSupported() {
        return Build.VERSION.SDK_INT != 10;
    }

    public void init(Activity activity) {
        StartAppAd.init(activity, developerId, appId);
        if (isSearchSdkSupported()) {
            StartAppSearch.init(activity, developerId, appId);
        }
    }

    public void addBanner(Activity activity) {
        final Banner banner = new Banner(activity);
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        final FrameLayout rootView = (FrameLayout)activity.getWindow().getDecorView().getRootView();
        rootView.addView(banner, layoutParams);
        if (isSearchSdkSupported()) {
            StartAppSearch.showSearchBox(activity);
        }
    }
}
