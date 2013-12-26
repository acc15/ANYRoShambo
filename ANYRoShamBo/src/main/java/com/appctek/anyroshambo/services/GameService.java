package com.appctek.anyroshambo.services;

import com.appctek.anyroshambo.model.GameModel;

import java.util.Random;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-26-12
 */
public class GameService {

    private static final int ICON_COUNT = 3;
    private static final int MIN_ROTATION_COUNT = 5;
    private static final int MAX_ROTATION_COUNT = 10;
    private static final long MIN_DURATION = 3000;
    private static final long MAX_DURATION = 15000;
    private static final float MIN_DECECELATE_FACTOR = 1f;
    private static final float MAX_DECELERATE_FACTOR = 3f;

    private final Random random;

    private float generateDecelerateFactor() {
        return MIN_DECECELATE_FACTOR + random.nextFloat() * (MAX_DECELERATE_FACTOR-MIN_DECECELATE_FACTOR);
    }

    private long generateDuration() {
        final long randomLong = random.nextLong();
        final long duration = MIN_DURATION + Math.abs(randomLong) % (MAX_DURATION-MIN_DURATION+1);
        return duration;
    }

    private int generateRotationCount() {
        final int fullRotations = random.nextInt((MAX_ROTATION_COUNT-MIN_ROTATION_COUNT)*2+1);
        return fullRotations <= MIN_ROTATION_COUNT ? fullRotations - MAX_ROTATION_COUNT : fullRotations;
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
