package com.appctek.anyroshambo.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.appctek.anyroshambo.math.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-07-01
 */
public class ViewUtils {

    private static final Logger logger = LoggerFactory.getLogger(ViewUtils.class);


    public static void scaleComponents(View container, View view) {
        final ViewGroup.LayoutParams params = view.getLayoutParams();
        final int vw = params.width, vh = params.height;
        if (isSpecialDimension(vw) || isSpecialDimension(vh)) {
            throw new IllegalArgumentException("View layout width and height should be fixed to perform scaling");
        }

        final int cw = container.getWidth(), ch = container.getHeight();
        if (cw == vw && ch == vh) {
            logger.debug("View already scaled. Skipping scale");
            return;
        }

        final Point scale = Point.fromArray((float)cw/vw,(float)ch/vh);
        logger.debug("Scaling view from " + vw + "x" + vh + " to " + cw + "x" + ch + ". Scale factors: " + scale);
        scaleView(view, scale);
    }

    private static boolean isSpecialDimension(int val) {
        return val == ViewGroup.LayoutParams.MATCH_PARENT || val == ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public static void scaleView(View view, Point factor) {

        final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (!isSpecialDimension(layoutParams.width)) {
            layoutParams.width *= factor.getX();
        }
        if (!isSpecialDimension(layoutParams.height)) {
            layoutParams.height *= factor.getY();
        }

        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            final ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            marginLayoutParams.leftMargin *= factor.getX();
            marginLayoutParams.rightMargin *= factor.getX();
            marginLayoutParams.topMargin *= factor.getY();
            marginLayoutParams.bottomMargin *= factor.getY();
        }

        view.setLayoutParams(layoutParams);
        view.setPadding(
                (int)(view.getPaddingLeft() * factor.getX()),
                (int)(view.getPaddingTop() * factor.getY()),
                (int)(view.getPaddingRight() * factor.getX()),
                (int)(view.getPaddingBottom() * factor.getY()));

        if (view instanceof TextView) {
            final TextView textView = (TextView) view;
            textView.setTextSize(textView.getTextSize() * Math.min(factor.getX(), factor.getY()));
        }
        if (view instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) view;
            for (int i=0; i<viewGroup.getChildCount(); i++) {
                final View child = viewGroup.getChildAt(i);
                scaleView(child, factor);
            }
        }
    }
}
