package com.brucetoo.expandrecyclerview.animator;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruce Too
 * On 12/01/2017.
 * At 14:20
 */

public class ViewAnimator {

    private static final long DEFAULT_DURATION = 3000;

    private List<AnimatorBuilder> mAnimatorBuilderList = new ArrayList<>();
    private long mAnimatorDuration = DEFAULT_DURATION;
    private long mAnimatorStartDelay = 0;
    private Interpolator mAnimatorInterpolator = null;

    private int mAnimatorRepeatCount = 0;
    private int mAnimatorRepeatMode = ValueAnimator.RESTART;

    private AnimatorSet mAnimatorSet;
    private View mWaitForThisViewSize = null;

    private AnimatorListener.Start mStartListener;
    private AnimatorListener.End mEndListener;

    private ViewAnimator mPreviousViewAnimator = null;
    private ViewAnimator mNextViewAnimator = null;

    private View[] mViews;

    public ViewAnimator() {}

    public ViewAnimator(View... view) {
        this.mViews = view;
    }

    @IntDef(value = {ValueAnimator.RESTART, ValueAnimator.REVERSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RepeatMode {
    }

    /* ----- Set Properties ------*/

    public static ViewAnimator putOn(View... view) {
        return new ViewAnimator(view);
    }

    public ViewAnimator andPutOn(View... view) {
        this.mViews = view;
        return this;
    }

    public ViewAnimator alpha(float alpha) {
        if (mViews != null) {
            for (View view : mViews) {
                ViewCompat.setAlpha(view, alpha);
            }
        }
        return this;
    }

    public ViewAnimator scaleX(float scale) {
        if (mViews != null) {
            for (View view : mViews) {
                ViewCompat.setScaleX(view, scale);
            }
        }
        return this;
    }

    public ViewAnimator scaleY(float scale) {
        if (mViews != null) {
            for (View view : mViews) {
                ViewCompat.setScaleY(view, scale);
            }
        }
        return this;
    }

    public ViewAnimator scale(float scale) {
        if (mViews != null) {
            for (View view : mViews) {
                ViewCompat.setScaleX(view, scale);
                ViewCompat.setScaleY(view, scale);
            }
        }
        return this;
    }

    public ViewAnimator translationX(float translation) {
        if (mViews != null) {
            for (View view : mViews) {
                ViewCompat.setTranslationX(view, translation);
            }
        }
        return this;
    }

    public ViewAnimator translationY(float translation) {
        if (mViews != null) {
            for (View view : mViews) {
                ViewCompat.setTranslationY(view, translation);
            }
        }
        return this;
    }

    public ViewAnimator translation(float translationX, float translationY) {
        if (mViews != null) {
            for (View view : mViews) {
                ViewCompat.setTranslationX(view, translationX);
                ViewCompat.setTranslationY(view, translationY);
            }
        }
        return this;
    }

    public ViewAnimator pivotX(float percent) {
        if (mViews != null) {
            for (View view : mViews) {
                ViewCompat.setPivotX(view, view.getWidth() * percent);
            }
        }
        return this;
    }

    public ViewAnimator pivotY(float percent) {
        if (mViews != null) {
            for (View view : mViews) {
                ViewCompat.setPivotY(view, view.getHeight() * percent);
            }
        }
        return this;
    }

    public ViewAnimator visible() {
        if (mViews != null) {
            for (View view : mViews) {
                view.setVisibility(View.VISIBLE);
            }
        }
        return this;
    }

    public ViewAnimator invisible() {
        if (mViews != null) {
            for (View view : mViews) {
                view.setVisibility(View.INVISIBLE);
            }
        }
        return this;
    }

    public ViewAnimator gone() {
        if (mViews != null) {
            for (View view : mViews) {
                view.setVisibility(View.GONE);
            }
        }
        return this;
    }


    /* ----- Animator Properties ------*/

    /**
     * Execute animator in views be add by {@link #putOn(View...)}
     * @return AnimatorBuilder
     */
    public AnimatorBuilder animate() {
        ViewAnimator viewAnimator = new ViewAnimator();
        return viewAnimator.addAnimatorBuilder(mViews);
    }

    /**
     * Execute animator in views by given now
     * @param views views by animated
     * @return AnimatorBuilder
     */
    public static AnimatorBuilder animate(View... views) {
        ViewAnimator viewAnimator = new ViewAnimator();
        return viewAnimator.addAnimatorBuilder(views);
    }

    public AnimatorBuilder thenAnimate(View... views) {
        ViewAnimator nextViewAnimator = new ViewAnimator();
        this.mNextViewAnimator = nextViewAnimator;
        nextViewAnimator.mPreviousViewAnimator = this;
        return nextViewAnimator.addAnimatorBuilder(views);
    }

    public AnimatorBuilder addAnimatorBuilder(View... views) {
        AnimatorBuilder animatorBuilder = new AnimatorBuilder(this, views);
        mAnimatorBuilderList.add(animatorBuilder);
        return animatorBuilder;
    }

    protected AnimatorSet createAnimatorSet() {

        List<Animator> animators = new ArrayList<>();
        //add all pending property animators
        for (AnimatorBuilder animatorBuilder : mAnimatorBuilderList) {
            List<Animator> animatorList = animatorBuilder.getPendingAnimators();
            if (animatorBuilder.getSingleInterpolator() != null) {
                for (Animator animator : animatorList) {
                    animator.setInterpolator(animatorBuilder.getSingleInterpolator());
                }
            }
            animators.addAll(animatorList);
        }

        //find if need wait for size
        for (AnimatorBuilder animatorBuilder : mAnimatorBuilderList) {
            if (animatorBuilder.isWaitForSize()) {
                mWaitForThisViewSize = animatorBuilder.getViews()[0];
                break;
            }
        }

        //set repeat mode and count if had be set
        for (Animator animator : animators) {
            if (animator instanceof ValueAnimator) {
                ValueAnimator valueAnimator = (ValueAnimator) animator;
                valueAnimator.setRepeatCount(mAnimatorRepeatCount);
                valueAnimator.setRepeatMode(mAnimatorRepeatMode);
            }
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);

        animatorSet.setDuration(mAnimatorDuration);
        animatorSet.setStartDelay(mAnimatorStartDelay);
        if (mAnimatorInterpolator != null)
            animatorSet.setInterpolator(mAnimatorInterpolator);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mStartListener != null) mStartListener.onStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mEndListener != null) mEndListener.onStop();
                if (mNextViewAnimator != null) {
                    mNextViewAnimator.mPreviousViewAnimator = null;
                    mNextViewAnimator.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        return animatorSet;
    }

    public ViewAnimator start() {
        if (mPreviousViewAnimator != null) {
            mPreviousViewAnimator.start();
        } else {
            mAnimatorSet = createAnimatorSet();

            if (mWaitForThisViewSize != null) {
                mWaitForThisViewSize.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mAnimatorSet.start();
                        mWaitForThisViewSize.getViewTreeObserver().removeOnPreDrawListener(this);
                        return false;
                    }
                });
            } else {
                mAnimatorSet.start();
            }
        }
        return this;
    }

    public void cancel() {
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
        if (mNextViewAnimator != null) {
            mNextViewAnimator.cancel();
            mNextViewAnimator = null;
        }
    }

    public ViewAnimator duration(long duration) {
        this.mAnimatorDuration = duration;
        return this;
    }

    public ViewAnimator startDelay(long startDelay) {
        this.mAnimatorStartDelay = startDelay;
        return this;
    }

    /**
     * Repeat count of animation.
     *
     * @param repeatCount the repeat count
     * @return the view animation
     */
    public ViewAnimator repeatCount(@IntRange(from = -1) int repeatCount) {
        this.mAnimatorRepeatCount = repeatCount;
        return this;
    }

    /**
     * Repeat mode view animation.
     *
     * @param repeatMode the repeat mode
     * @return the view animation
     */
    public ViewAnimator repeatMode(@RepeatMode int repeatMode) {
        this.mAnimatorRepeatMode = repeatMode;
        return this;
    }

    public ViewAnimator onStart(AnimatorListener.Start startListener) {
        this.mStartListener = startListener;
        return this;
    }

    public ViewAnimator onEnd(AnimatorListener.End endListener) {
        this.mEndListener = endListener;
        return this;
    }

    /**
     * Interpolator view animator.
     *
     * @param interpolator the mAnimatorInterpolator
     * @return the view animator
     * @link https://github.com/cimi-chen/EaseInterpolator
     */
    public ViewAnimator interpolator(Interpolator interpolator) {
        this.mAnimatorInterpolator = interpolator;
        return this;
    }

}
