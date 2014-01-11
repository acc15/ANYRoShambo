package com.appctek.anyroshambo.services;

import android.app.Activity;
import android.view.ViewGroup;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-12-01
 */
public class NoAdService implements AdService {
    public void init(Activity activity) {
    }

    public boolean isAdEnabled() {
        return false;
    }

    public void addBanner(ViewGroup container) {
    }
}
