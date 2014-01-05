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

    private Point(float[] exact) {
        this.components = exact;
    }

    private Point(int length, float[] components) {
        this(new float[length]);
        System.arraycopy(components, 0, this.components, 0, length);
    }

    private Point(Point copy) {
        this(copy.components.length, copy.components);
    }

    public static Point zero(int componentCount) {
        return new Point(new float[componentCount]);
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

    public float getX() {
        return components[X];
    }

    public float getY() {
        return components[Y];
    }

    public float getZ() {
        return components[Z];
    }

    /**
     * Calculates sum of {@code this} and {@code pt} points
     * @param pt pt to add
     * @return sum of of {@code this} and {@code pt} points
     */
    public Point add(Point pt) {
        final Point result = new Point(this);
        for (int i=0; i<result.components.length; i++) {
            result.components[i] += pt.components[i];
        }
        return result;
    }

    /**
     * Calculates difference of {@code this} and {@code pt} points
     * @param pt pt to subtract
     * @return difference of {@code this} and {@code pt} points
     */
    public Point sub(Point pt) {
        final Point result = new Point(this);
        for (int i=0; i<result.components.length; i++) {
            result.components[i] -= pt.components[i];
        }
        return result;
    }

    /**
     * Returns multiplication of {@code this} and {@code pt} points
     * @param pt multiplicand
     * @return multiplication of {@code this} and {@code pt} points
     */
    public Point mul(Point pt) {
        final Point result = new Point(this);
        for (int i=0; i<result.components.length; i++) {
            result.components[i] *= pt.components[i];
        }
        return result;
    }

    /**
     * Returns division of {@code this} on {@code pt} point
     * @param pt divider
     * @return division of {@code this} on {@code pt} point
     */
    public Point div(Point pt) {
        final Point result = new Point(this);
        for (int i=0; i<result.components.length; i++) {
            result.components[i] /= pt.components[i];
        }
        return result;
    }

    /**
     * Returns sum of {@code this} and {@code {val,val}} points
     * @param val addend
     * @return sum of {@code this} and {@code {val,val}} points
     */
    public Point add(float val) {
        final Point result = new Point(this);
        for (int i=0; i<result.components.length; i++) {
            result.components[i] += val;
        }
        return result;
    }

    /**
     * Returns difference between {@code this} and {@code {val,val}} points
     * @param val subtrahend
     * @return difference between {@code this} and {@code {val,val}} points
     */
    public Point sub(float val) {
        final Point result = new Point(this);
        for (int i=0; i<result.components.length; i++) {
            result.components[i] -= val;
        }
        return result;
    }

    /**
     * Returns multiplication of {@code this} and {@code {val,val}} points
     * @param val multiplicand
     * @return multiplication of {@code this} and {@code {val,val}} points
     */
    public Point mul(float val) {
        final Point result = new Point(this);
        for (int i=0; i<result.components.length; i++) {
            result.components[i] *= val;
        }
        return result;
    }

    /**
     * Returns division of {@code this} and {@code {val,val}} points
     * @param val divider
     * @return division of {@code this} and {@code {val,val}} points
     */
    public Point div(float val) {
        final Point result = new Point(this);
        for (int i=0; i<result.components.length; i++) {
            result.components[i] /= val;
        }
        return result;
    }

    /**
     * Rotates point on specified amount of radians.
     * Works only for 2 dimension points.
     * @param angle amount of radians
     * @return rotated point
     */
    public Point rotate(float angle) {
        if (getComponentCount() != 2) {
            throw new IllegalStateException("Rotate function is only supported for 2 dimension points");
        }
        final float sina = (float)Math.sin(angle);
        final float cosa = (float)Math.cos(angle);
        final float x = cosa * get(X) - sina * get(Y);
        final float y = sina * get(X) + cosa * get(Y);
        return Point.fromArray(x, y);
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
        return result;
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
     * Calculates angle between {@code this} and {@code pt} vectors.
     * Returns result as cos(a). Do arccos() to get actual angle in radians.
     * @param pt point
     * @return {@code cos(x)}, where x is an angle between {@code this} and {@code pt} vectors
     */
    public float cosineOfAngle(Point pt) {
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
