package com.appctek.anyroshambo.roboguice;

import android.os.Bundle;
import roboguice.activity.RoboActivity;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-25-01
 */
public class EventRoboActivity extends RoboActivity {

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        eventManager.fire(new OnSaveInstanceStateEvent(outState));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        eventManager.fire(new OnRestoreInstanceStateEvent(savedInstanceState));
    }
}
