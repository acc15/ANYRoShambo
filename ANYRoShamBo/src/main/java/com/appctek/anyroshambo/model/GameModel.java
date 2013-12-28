package com.appctek.anyroshambo.model;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-26-12
 */
public class GameModel {

    private int rotationCount;
    private int selectedIcon = -1;
    private long duration;
    private float decelerateFactor;
    private float fromDegrees;
    private float toDegrees;
    private boolean inProgress;

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

    public float getFromDegrees() {
        return fromDegrees;
    }

    public void setFromDegrees(float fromDegrees) {
        this.fromDegrees = fromDegrees;
    }

    public float getToDegrees() {
        return toDegrees;
    }

    public void setToDegrees(float toDegrees) {
        this.toDegrees = toDegrees;
    }


    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }
}
