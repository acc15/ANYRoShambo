package com.appctek.anyroshambo.roboguice;

import android.os.Bundle;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-25-01
 */
public class OnRestoreInstanceStateEvent {

    private Bundle savedInstanceState;

    public OnRestoreInstanceStateEvent(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
    }

    public Bundle getSavedInstanceState() {
        return savedInstanceState;
    }
}
