package com.appctek.anyroshambo.services;

import com.appctek.anyroshambo.model.GameModel;
import com.google.inject.Inject;

import java.util.Random;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-26-12
 */
public class GameService {

    private static final int ICON_COUNT = 3;
    private static final int MIN_ROTATION_COUNT = 8;
    private static final int MAX_ROTATION_COUNT = 10;
    private static final long MIN_DURATION = 10000;
    private static final long MAX_DURATION = 10000;
    private static final float MIN_DECELERATE_FACTOR = 1f;
    private static final float MAX_DECELERATE_FACTOR = 1f;

    private final Random random;

    public static float nextFloat(Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    public static long nextLong(Random random, long min, long max) {
        final long randomValue = random.nextLong();
        return min + Math.abs(randomValue) % (max-min+1);
    }

    public static int nextPositiveOrNegative(Random random, int min, int max) {
        final int values = max - min + 1;
        final int randomValue = random.nextInt(values*2);
        return randomValue < values ? min + randomValue : -(min + randomValue - values);
    }

    private float generateDecelerateFactor() {
        return nextFloat(random, MIN_DECELERATE_FACTOR, MAX_DECELERATE_FACTOR);
    }

    private long generateDuration() {
        return nextLong(random, MIN_DURATION, MAX_DURATION);
    }

    private int generateRotationCount() {
        return nextPositiveOrNegative(random, MIN_ROTATION_COUNT, MAX_ROTATION_COUNT);
    }

    private int generateSelectedIcon() {
        return random.nextInt(ICON_COUNT);
    }

    @Inject
    public GameService(Random random) {
        this.random = random;
    }

    public void initGame(GameModel model) {
        model.setRotationCount(generateRotationCount());
        model.setSelectedIcon(generateSelectedIcon());
        model.setDuration(generateDuration());
        model.setDecelerateFactor(generateDecelerateFactor());
        model.setFromDegrees(model.getToDegrees()%360);
        model.setToDegrees(360 * model.getRotationCount() + (360/ICON_COUNT) * model.getSelectedIcon());
    }

}
