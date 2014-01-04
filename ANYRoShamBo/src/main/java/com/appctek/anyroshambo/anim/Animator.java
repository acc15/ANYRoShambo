package com.appctek.anyroshambo.anim;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import com.appctek.anyroshambo.math.GeometryUtils;
import com.appctek.anyroshambo.services.DateTimeService;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-26-12
 */
public class Animator {

    private DateTimeService dateTimeService;

    public Animator(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public float computeInterpolation(Animation animation) {
        final long currentTime = dateTimeService.getAnimationTimeMillis();
        final long startTime = animation.getStartTime();
        final long duration = animation.getDuration();
        final long time = currentTime - startTime;
        if (time >= duration) {
            return 1.0f;
        }

        final float timePosition = (float)time/duration;
        final Interpolator interpolator = animation.getInterpolator();
        return interpolator.getInterpolation(timePosition);
    }

    public InOutAnimationActionBuilder animateInOut(View view) {
        return new InOutAnimationActionBuilder(view);
    }

    public AnimationActionBuilder animate(View view) {
        return new AnimationActionBuilder(view);
    }

    public class AnimationActionBuilder {
        private View view;
        private Animation animation;

        public AnimationActionBuilder(View view) {
            this.view = view;
        }

        public AnimationActionBuilder with(Animation animation) {
            this.animation = animation;
            return this;
        }

        public LazyAction build() {
           return new AnimationAction(view, animation);
        }
    }

    public class InOutAnimationActionBuilder {
        private View view;
        private Animation inAnimation;
        private Animation outAnimation;
        private long delay;
        private boolean skipOnClick = false;

        public InOutAnimationActionBuilder(View view) {
            this.view = view;
        }

        public InOutAnimationActionBuilder in(Animation animation) {
            this.inAnimation = animation;
            return this;
        }

        public InOutAnimationActionBuilder out(Animation animation) {
            this.outAnimation = animation;
            return this;
        }

        public InOutAnimationActionBuilder withDelay(long delay) {
            this.delay = delay;
            return this;
        }

        public InOutAnimationActionBuilder skipOnClick() {
            this.skipOnClick = true;
            return this;
        }

        public LazyAction build() {
            final Sequencer seq = new Sequencer(new ActionSequence() {
                public LazyAction executeStep(int step, Sequencer sequencer) {
                    switch (step) {
                    case 0:
                        return new AnimationAction(view, inAnimation);

                    case 1:
                        return delay > 0 ? new DelayAction(view, delay) : new NopAction();

                    case 2:
                        final float interpolation = computeInterpolation(inAnimation);
                        if (interpolation < 1.f) {

                            final Interpolator oldInterpolator = outAnimation.getInterpolator();
                            outAnimation.setInterpolator(new Interpolator() {
                                public float getInterpolation(float input) {
                                    // should interpolate between [1 - interpolation; 1]
                                    final float newInterpolation = GeometryUtils.interpolate(input,
                                            1-interpolation, 1);
                                    return oldInterpolator.getInterpolation(newInterpolation);
                                }
                            });
                            outAnimation.setDuration((long)(outAnimation.getDuration() * interpolation));

                        }
                        return new AnimationAction(view, outAnimation);
                    }
                    return null;
                }
            });
            if (skipOnClick) {
                view.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        seq.run(2);
                    }
                });
            }
            return seq;

        }
    }


}
