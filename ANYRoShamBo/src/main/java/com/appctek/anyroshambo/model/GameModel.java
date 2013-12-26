package com.appctek.anyroshambo.model;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-26-12
 */
public class GameModel {

    private int rotationCount;
    private int selectedIcon;
    private long duration;
    private float decelerateFactor;

    public int getRotationCount() {
        return rotationCount;
    }

    public void setRotationCount(int rotationCount) {
        this.rotationCount = rotationCount;
    }

    public int getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(int selectedIcon) {
        this.selectedIcon = selectedIcon;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public float getDecelerateFactor() {
        return decelerateFactor;
    }

    public void setDecelerateFactor(float decelerateFactor) {
        this.decelerateFactor = decelerateFactor;
    }
}
