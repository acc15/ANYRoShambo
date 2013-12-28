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

    public static float calculateTriangleCenterY(int height) {
        return height * TWO_DIV_THREE;
    }

    private static float sign(Point p1, Point p2, Point p3) {
        return (p1.get(Point.X) - p3.get(Point.X)) * (p2.get(Point.Y) - p3.get(Point.Y)) -
               (p2.get(Point.X) - p3.get(Point.X)) * (p1.get(Point.Y) - p3.get(Point.Y));
    }

    public static boolean ptInTriangle(Point pt, Point t1, Point t2, Point t3) {
        final boolean b1 = sign(pt, t1, t2) < 0.0f,
                      b2 = sign(pt, t2, t3) < 0.0f,
                      b3 = sign(pt, t3, t1) < 0.0f;
        return ((b1 == b2) && (b1 == b3));
    }

}
