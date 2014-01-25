package com.appctek.anyroshambo.roboguice;

import android.os.Bundle;
import com.appctek.anyroshambo.roboguice.OnRestoreInstanceStateEvent;
import roboguice.activity.RoboActivity;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-25-01
 */
public class EventRoboActivity extends RoboActivity {

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO implement..
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        eventManager.fire(new OnRestoreInstanceStateEvent(savedInstanceState));
    }
}
