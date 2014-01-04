package com.appctek.anyroshambo.sequences;

/**
 * @author Vyacheslav Mayorov
 * @since 2014-04-01
 */
public class Sequencer implements LazyAction {

    private int step = -1;

    private final ActionSequence actionSequence;
    private Runnable listener;

    public Sequencer(ActionSequence listener) {
        this.actionSequence = listener;
    }

    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    public void execute() {
        execute(0);
    }

    public void execute(final int step) {
        if (this.step >= step) {
            return;
        }

        final LazyAction lazyAction = actionSequence.executeStep(step, this);
        if (lazyAction == null) {
            if (listener != null) {
                listener.run();
            }
            return;
        }

        this.step = step;
        lazyAction.setListener(new Runnable() {
            public void run() {
                execute(step+1);
            }
        });
        lazyAction.execute();
    }

    public int getStep() {
        return step;
    }

    public void reset() {
        this.step = -1;
    }
}
