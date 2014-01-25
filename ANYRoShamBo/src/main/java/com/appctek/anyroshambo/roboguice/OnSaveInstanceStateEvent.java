package com.appctek.anyroshambo.roboguice;

import android.os.Bundle;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-25-01
 */
public class OnSaveInstanceStateEvent {
    private Bundle outBundle;

    public OnSaveInstanceStateEvent(Bundle bundle) {
        this.outBundle = bundle;
    }

    public Bundle getOutBundle() {
        return outBundle;
    }
}
