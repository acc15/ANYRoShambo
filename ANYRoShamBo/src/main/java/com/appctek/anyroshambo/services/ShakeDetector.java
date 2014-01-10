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
import java.util.concurrent.TimeUnit;

/**
 * Not perfect and sometimes
 *
 * @author Vyacheslav Mayorov
 * @since 2013-22-12
 */
public class ShakeDetector implements SensorEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ShakeDetector.class);

    private static final float INITIAL_ALPHA = 0.2f;
    private static final float DEFAULT_ALPHA = 0.8f;

    public interface OnShakeListener {
        void onShake();
    }

    private Context context;
    private float sensitivity = 8f;
    private int maxMoveCount = 5;
    private long shakeTimeout = 500; // 500ms to reset shaking
    private int moveCount = 0;
    private float alpha = INITIAL_ALPHA;
    private long lastMoveTime = 0;

    private Point moveVector = Point.zero(3); // identity vector which tracks movement direction
    private Point gravity = Point.zero(3); // force of gravity

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

        logger.trace("Accelerometer event: [timestamp:{};accuracy:{};value:{}]",
                event.timestamp, event.accuracy, Arrays.toString(event.values));

        final Point currentVelocity = Point.fromArray(3, event.values);
        gravity = gravity.mul(alpha).add(currentVelocity.mul(1f-alpha));
        alpha = DEFAULT_ALPHA;

        final Point linearVelocity = currentVelocity.sub(gravity);

        logger.trace("Linear velocity of event at {}: {}", event.timestamp, linearVelocity);
        if (isNoise(linearVelocity)) {
            logger.trace("Event at {} will be ignored since it has low velocity values", event.timestamp);
            return;
        }

        final float cosineOfAngle = moveVector.cosineOfAngle(linearVelocity);
        if (cosineOfAngle >= 0) {
            logger.trace("Move direction doesn't changes due event at {}. Continue tracking...", event.timestamp);
            moveVector = moveVector.add(linearVelocity).identity();
            return;
        }

        logger.debug("Detected change of direction. New direction: {}. Old direction: {}", linearVelocity, moveVector);
        moveVector = linearVelocity.identity();

        final long currentTime = TimeUnit.NANOSECONDS.toMillis(event.timestamp);
        if (moveCount > 0 && currentTime - lastMoveTime > shakeTimeout) {
            logger.debug("Shake timeout has been expired");
            moveCount = 0;
        }

        lastMoveTime = currentTime;
        if (++moveCount < maxMoveCount) {
            logger.trace("Not enough move count {} to report shake event. ", moveCount);
            return;
        }

        logger.info("Shake detected. Calling onShakeListener.");

        final OnShakeListener shakeListener = onShakeListener;
        if (shakeListener != null) {
            shakeListener.onShake();
        }
        moveCount = 0;

    }

    public ShakeDetector(Context context) {
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

        this.alpha = INITIAL_ALPHA;
        this.onShakeListener = shakeListener;

        // Android emulator hungs unexpectedly when attempts to get sensor service
        // TODO Find a way to disable listener registration when running on emulator only
        final SensorManager sensorManager = getSensorManager();
        final Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

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
