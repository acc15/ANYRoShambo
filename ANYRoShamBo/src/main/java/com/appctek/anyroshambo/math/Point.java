package com.appctek.anyroshambo.math;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-22-12
 */
public class Point {

    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;

    private float[] components;

    private Point(float[] components) {
        this.components = components;
    }

    private Point(Point copy) {
        this.components = new float[copy.components.length];
        System.arraycopy(copy.components, 0, components, 0, components.length);
    }

    /**
     * Creates point from specified components
     * @param components point components
     * @return created point
     */
    public static Point fromArray(float... components) {
        return new Point(components);
    }

    /**
     * Returns count of dimensions for this point
     * @return count of dimensions
     */
    public int getComponentCount() {
        return components.length;
    }

    /**
     * Returns coordinate by specified coordinate index (0 for X, 1 for Y, 2 for Z and so on)
     * @param comp coordinate index
     * @return coordinate value
     */
    public float get(int comp) {
        return components[comp];
    }

    /**
     * Calculates sum of <code>this</code> and <code>pt</code> points
     * @param pt pt to add
     * @return sum of of <code>this</code> and <code>pt</code> points
     */
    public Point add(Point pt) {
        final Point result = new Point(this);
        for (int i=0; i<result.components.length; i++) {
            result.components[i] += pt.components[i];
        }
        return result;
    }

    /**
     * Calculates difference of <code>this</code> and <code>pt</code> points
     * @param pt pt to subtract
     * @return difference of <code>this</code> and <code>pt</code> points
     */
    public Point sub(Point pt) {
        final Point result = new Point(this);
        for (int i=0; i<result.components.length; i++) {
            result.components[i] -= pt.components[i];
        }
        return result;
    }

    /**
     * Returns multiplication of <code>this</code> and <code>pt</code> points
     * @param pt multiplicand
     * @return multiplication of <code>this</code> and <code>pt</code> points
     */
    public Point mul(Point pt) {
        final Point result = new Point(this);
        for (int i=0; i<result.components.length; i++) {
            result.components[i] *= pt.components[i];
        }
        return result;
    }

    /**
     * Returns division of <code>this</code> on <code>pt</code> point
     * @param pt divider
     * @return division of <code>this</code> on <code>pt</code> point
     */
    public Point div(Point pt) {
        final Point result = new Point(this);
        for (int i=0; i<result.components.length; i++) {
            result.components[i] /= pt.components[i];
        }
        return result;
    }

    /**
     * Calculates squared length of vector
     * @return squared length of vector
     */
    public float moduleSquare() {
        float sum = 0f;
        for (float comp: components) {
            sum += comp*comp;
        }
        return sum;
    }

    /**
     * Calculates length of vector
     * @return length of vector
     */
    public float module() {
        return (float)Math.sqrt(moduleSquare());
    }

    /**
     * Calculates identity vector
     * @return identity vector
     */
    public Point identity() {
        final Point result = new Point(this);
        final float module = module();
        for (int i=0; i<result.components.length; i++) {
            result.components[i] /= module;
        }
        return this;
    }

    /**
     * Returns sum of all components
     * @return sum of all components
     */
    public float sum() {
        float sum = 0f;
        for (float c: components) {
            sum += c;
        }
        return sum;
    }

    /**
     * Calculates angle between <code>this</code> and <code>pt</code> vectors.
     * Returns result as cos(a). Do arccos() to get actual angle in radians.
     * @param pt point
     * @return {@code cos(x)}, where x is an angle between <code>this</code> and <code>pt</code> vectors
     */
    public float angle(Point pt) {
        return mul(pt).sum() / (module() * pt.module());
    }

    /**
     * Returns point with negated components
     * @return point with negated components
     */
    public Point negate() {
        final Point result = new Point(this);
        for (int i=0; i<result.components.length; i++) {
            result.components[i] = -result.components[i];
        }
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        for (int i=0; i<components.length; i++) {
            if (i>0) {
                sb.append(',');
            }
            sb.append(components[i]);
        }
        sb.append('}');
        return sb.toString();
    }
}
