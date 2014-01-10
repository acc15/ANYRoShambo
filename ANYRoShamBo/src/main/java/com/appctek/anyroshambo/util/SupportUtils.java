package com.appctek.anyroshambo.util;

import android.view.ViewTreeObserver;

/**
 * Utility class. Added to suppress all deprecation warnings in single place
 * @author Vyacheslav Mayorov
 * @since 2014-11-01
 */
@SuppressWarnings("deprecation")
public class SupportUtils {

    /**
     * Support method for
     * {@link android.view.ViewTreeObserver#removeOnGlobalLayoutListener(android.view.ViewTreeObserver.OnGlobalLayoutListener)}
     * @see android.view.ViewTreeObserver#removeOnGlobalLayoutListener(android.view.ViewTreeObserver.OnGlobalLayoutListener)
     */
    public static void removeOnGlobalLayoutListener(
            ViewTreeObserver viewTreeObserver, ViewTreeObserver.OnGlobalLayoutListener listener) {
        viewTreeObserver.removeGlobalOnLayoutListener(listener);
    }

}
