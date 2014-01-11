package com.appctek.anyroshambo.util;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.appctek.anyroshambo.BuildConfig;
import com.appctek.anyroshambo.math.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Vyacheslav Mayorov
 * @since 2014-07-01
 */
public class ViewUtils {

    private static final Logger logger = LoggerFactory.getLogger(ViewUtils.class);

    public static void printViewHierarchy(View view) {
        printViewHierarchy("", view);
    }

    private static void printViewHierarchy(final String prefix, final View view) {
        if (BuildConfig.DEBUG) {
            logger.info(prefix + view.getClass().getName() + "[" + view.getId() + "]");
            if (view instanceof ViewGroup) {
                final ViewGroup vg = (ViewGroup) view;
                for (int i=0; i<vg.getChildCount(); i++) {
                    final View child = vg.getChildAt(i);
                    printViewHierarchy(prefix + "  ", child);
                }
            }
        }
    }

    public static boolean scaleComponents(View container, View view) {
        final int cw = container.getWidth(), ch = container.getHeight();

        final int vw = view.getWidth(), vh = view.getHeight();
        if (cw == vw) {
            return false;
        }

        final ViewGroup.LayoutParams params = view.getLayoutParams();
        final int lw = params.width, lh = params.height;
        if (isSpecialDimension(lw) || isSpecialDimension(lh)) {
            throw new IllegalArgumentException("View layout width and height should be fixed to perform scaling");
        }
        if (cw == lw) {
            logger.debug("View already scaled. Skipping scale");
            return true;
        }

        final float factor = (float)cw/lw;
        final Point scale = Point.create(factor, factor);
        logger.debug("Scaling view from " + lw + "x" + lh + " to " + cw + "x" + ch + ". Scale factors: " + scale);
        scaleView(view, scale);
        return true;
    }

    private static boolean isSpecialDimension(int val) {
        return val == ViewGroup.LayoutParams.MATCH_PARENT || val == ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private static void scaleWH(ViewGroup.LayoutParams lp, float xScale, float yScale) {
        if (!isSpecialDimension(lp.width)) {
            lp.width *= xScale;
        }
        if (!isSpecialDimension(lp.height)) {
            lp.height *= yScale;
        }
    }

    public static void scaleView(View view, Point factor) {

        final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        scaleWH(layoutParams, factor.getX(), factor.getY());

        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            final ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            marginLayoutParams.leftMargin *= factor.getX();
            marginLayoutParams.rightMargin *= factor.getX();
            marginLayoutParams.topMargin *= factor.getY();
            marginLayoutParams.bottomMargin *= factor.getY();
        }

        view.setLayoutParams(layoutParams);
        view.setPadding(
                (int) (view.getPaddingLeft() * factor.getX()),
                (int) (view.getPaddingTop() * factor.getY()),
                (int) (view.getPaddingRight() * factor.getX()),
                (int) (view.getPaddingBottom() * factor.getY()));

        if (view instanceof ImageView) {
            final ImageView imageView = (ImageView) view;
            if (imageView.getScaleType() == ImageView.ScaleType.MATRIX) {
                final Matrix matrix = new Matrix();
                matrix.setScale(factor.getX(), factor.getY());
                imageView.setImageMatrix(matrix);
            }
        } else if (view instanceof TextView) {

            final TextView textView = (TextView) view;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getTextSize() * Math.min(factor.getX(), factor.getY()));

            final Drawable[] drawables = textView.getCompoundDrawables();
            for (final Drawable drawable: drawables) {
                if (drawable == null) {
                    continue;
                }

                final Rect oldBounds = drawable.getBounds();
                drawable.setBounds((int)(oldBounds.left * factor.getX()),
                                   (int)(oldBounds.top * factor.getY()),
                                   (int)(oldBounds.right * factor.getX()),
                                   (int)(oldBounds.bottom * factor.getY()));
            }
            textView.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);

        } else if (view instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                final View child = viewGroup.getChildAt(i);
                scaleView(child, factor);
            }
        }
    }
}
