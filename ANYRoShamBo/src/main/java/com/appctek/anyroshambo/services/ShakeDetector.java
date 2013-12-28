package com.appctek.anyroshambo.services;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.appctek.anyroshambo.math.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Not perfect and sometimes
 *
 * @author Vyacheslav Mayorov
 * @since 2013-22-12
 */
public class ShakeDetector implements SensorEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ShakeDetector.class);

    public interface OnShakeListener {
        void onShake();
    }

    private Context context;
    private DateTimeService dateTimeService;
    private float sensitivity = 10f;
    private int maxMoveCount = 5;
    private long shakeTimeout = 500; // 500ms to reset shaking
    private Point prevAccel, accelDir;
    private int moveCount = 0;
    private long lastMoveTime = 0;

    private OnShakeListener onShakeListener;

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

        final long timestamp = event.timestamp;

        logger.trace("Accelerometer event: [timestamp:" + timestamp +
                                     ";accuracy:" + event.accuracy +
                                     ";values:" + Arrays.toString(event.values) + "]");

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
            logger.trace("Accelerometer event at {} filtered by values as noise", timestamp);
            return;
        }

        final float angle = accelDir.angle(accelDiff);
        if (angle >= 0) {
            logger.trace("Move direction doesn't changes. Returning");
            accelDir = accelDir.add(accelDiff).identity();
            return;
        }

        logger.debug("Detected change of direction. Acceleration: {}. Old direction: {}", accelDiff, accelDir);

        // direction of phone changed..
        accelDir = accelDiff.identity();

        final long currentTime = dateTimeService.getTimeInMillis();
        if (moveCount > 0 && currentTime - lastMoveTime > shakeTimeout) {
            logger.debug("Shake timeout has been expired");
            moveCount = 0;
        }

        lastMoveTime = currentTime;
        if (++moveCount < maxMoveCount) {
            logger.debug("Not enough move count {} to report shake event. ", moveCount);
            return;
        }

        logger.info("Shake detected. Calling onShakeListener.");
        final OnShakeListener shakeListener = onShakeListener;
        if (shakeListener != null) {
            shakeListener.onShake();
        }
        moveCount = 0;
    }

    public ShakeDetector(Context context, DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
        this.context = context;
    }

    public void pause() {
        if (onShakeListener != null) {
            getSensorManager().unregisterListener(this);
        }
    }

    public void resume() {
        if (onShakeListener != null) {
            start(onShakeListener);
        }
    }

    public void start(OnShakeListener shakeListener) {
        this.onShakeListener = shakeListener;
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
