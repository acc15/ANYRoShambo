package com.appctek.anyroshambo.services;

import com.appctek.anyroshambo.model.GameModel;

import java.util.Random;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-26-12
 */
public class GameService {

    private static final int ICON_COUNT = 3;
    private static final int MIN_ROTATION_COUNT = 8;
    private static final int MAX_ROTATION_COUNT = 10;
    private static final long MIN_DURATION = 8000;
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

    public GameService(Random random) {
        this.random = random;
    }

    public GameModel generateGameModel() {

        final GameModel gameModel = new GameModel();
        gameModel.setRotationCount(generateRotationCount());
        gameModel.setSelectedIcon(generateSelectedIcon());
        gameModel.setDuration(generateDuration());
        gameModel.setDecelerateFactor(generateDecelerateFactor());
        return gameModel;

    }

}
