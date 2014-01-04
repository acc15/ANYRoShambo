package com.appctek.anyroshambo.math;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-27-12
 */
public class GeometryUtils {

    public static final float PI = (float)Math.PI;
    public static final float HALF_PI = PI/2;
    public static final float TWO_PI = PI*2;
    public static final float TWO_DIV_THREE = 2f/3;
    public static final float HALF = 0.5f;
    public static final float DEGREES_IN_CIRCLE = 360;

    public static float interpolate(float interpolation, float min, float max) {
        return min + (max - min) * interpolation;
    }

    public static float calculateTriangleCenterY(int height) {
        return height * TWO_DIV_THREE;
    }
}
