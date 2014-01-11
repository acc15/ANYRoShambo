package com.appctek.anyroshambo.services;

import android.app.Activity;
import android.view.ViewGroup;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-08-01
 */
public interface AdService {

    /**
     * Should be called before {@link android.app.Activity#setContentView(int)}
     * @param activity activity for initialization
     */
    void init(Activity activity);

    /**
     * Appends banner view to the bottom of specified container. Supported container types: <ul>
     *     <li>RelativeLayout</li>
     *     <li>FrameLayout</li>
     * </ul>
     * Must be called after {@link android.app.Activity#setContentView(int)}.
     * @param container container to append banner view
     */
    void addBanner(ViewGroup container);

    /**
     * Adds optional features provided by AD network.
     * Must be called after {@link android.app.Activity#setContentView(int)}.
     * @param activity activity for initialization
     */
    void addFeatures(Activity activity);

}
