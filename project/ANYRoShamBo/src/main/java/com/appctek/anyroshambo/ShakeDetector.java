package com.appctek.anyroshambo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.appctek.anyroshambo.math.Point;
import com.appctek.anyroshambo.services.DateTimeService;

/**
 * Not perfect and sometimes
 *
 * @author Vyacheslav Mayorov
 * @since 2013-22-12
 */
public class ShakeDetector implements SensorEventListener {

    public static interface ShakeListener {
        void onShake();
    }

    private ShakeListener listener = null;
    private Context context;

    private float sensitivity = 10f;
    private int maxMoveCount = 5;
    private long shakeTimeout = 500; // 500ms to reset shaking

    private Point prevAccel, accelDir;
    private int moveCount = 0;
    private long lastMoveTime = 0;

    public int getMaxMoveCount() {
        return maxMoveCount;
    }

    public void setMaxMoveCount(int maxMoveCount) {
        this.maxMoveCount = maxMoveCount;
    }

    public float getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }

    public long getShakeTimeout() {
        return shakeTimeout;
    }

    public void setShakeTimeout(long shakeTimeout) {
        this.shakeTimeout = shakeTimeout;
    }

    public void onSensorChanged(SensorEvent event) {

        final Point currentAccel = Point.fromArray(
                         event.values[SensorManager.DATA_X],
                         event.values[SensorManager.DATA_Y],
                         event.values[SensorManager.DATA_Z]);

        if (prevAccel == null) {
            prevAccel = currentAccel;
            accelDir = prevAccel.identity();
            return;
        }

        final Point accelDiff = currentAccel.sub(prevAccel);
        prevAccel = currentAccel;

        if (isNoise(accelDiff)) {
            return;
        }

        final float angle = accelDir.angle(accelDiff);
        if (angle >= 0) {
            accelDir = accelDir.add(accelDiff).identity();
            return;
        }

        // direction of phone changed..
        accelDir = accelDiff.identity();

        final long currentTime = System.currentTimeMillis();
        if (moveCount > 0 && currentTime - lastMoveTime > shakeTimeout) {
            moveCount = 0;
        }

        lastMoveTime = currentTime;
        if (++moveCount >= maxMoveCount) {
            listener.onShake();
            moveCount = 0;
        }

    }

    public ShakeDetector(Context context, DateTimeService dateTimeService) {
        this.context = context;
    }

    public void pause() {
        if (listener != null) {
            getSensorManager().unregisterListener(this);
        }
    }

    public void resume() {
        if (listener != null) {
            start(listener);
        }
    }

    public void start(ShakeListener listener) {
        this.listener = listener;
        final SensorManager sensorManager = getSensorManager();
        final Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private SensorManager getSensorManager() {
        return (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
    }

    private boolean isNoise(float coord) {
        return Math.abs(coord) < sensitivity;
    }

    private boolean isNoise(Point pt) {
        for (int i=0; i<pt.getComponentCount(); i++) {
            if (!isNoise(pt.get(i))) {
                return false;
            }
        }
        return true;
    }




}
