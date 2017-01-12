package com.brucetoo.expandrecyclerview.animator;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.annotation.IntRange;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruce Too
 * On 12/01/2017.
 * At 14:22
 */

public class AnimatorBuilder {

    private final ViewAnimator mViewAnimator;
    private final View[] mViews;
    private final List<Animator> mAnimatorList = new ArrayList<>();
    private boolean mWaitForSize;
    private boolean mNextValueWillBeDp = false;
    private Interpolator mSingleInterpolator = null;//interpolator for a single ViewAnimator

    /**
     * Init a new Animation builder with changeable views
     *
     * @param viewAnimator the view animator
     * @param views        the animated views
     */
    public AnimatorBuilder(ViewAnimator viewAnimator, View... views) {
        this.mViewAnimator = viewAnimator;
        this.mViews = views;
    }

    /**
     * Make next value be dp in animator builder.
     *
     * @return the animator builder
     */
    public AnimatorBuilder dp() {
        mNextValueWillBeDp = true;
        return this;
    }

    /**
     * Add a animator into {@link #mAnimatorList}
     *
     * @param animator the animator
     * @return the animator builder
     */
    protected AnimatorBuilder add(Animator animator) {
        this.mAnimatorList.add(animator);
        return this;
    }

    /**
     * To dp float.
     *
     * @param px the px
     * @return the float
     */
    protected float toDp(final float px) {
        return px / mViews[0].getContext().getResources().getDisplayMetrics().density;
    }

    /**
     * To px float.
     *
     * @param dp the dp
     * @return the float
     */
    protected float toPx(final float dp) {
        return dp * mViews[0].getContext().getResources().getDisplayMetrics().density;
    }

    /**
     * Get values float [ ].
     *
     * @param values the values
     * @return the float [ ]
     */
    protected float[] getValues(float... values) {
        if (!mNextValueWillBeDp) {
            return values;
        }
        float[] pxValues = new float[values.length];
        for (int i = 0; i < values.length; ++i) {
            pxValues[i] = toPx(values[i]);
        }
        return pxValues;
    }

    /**
     * Property animator builder.
     *
     * @param propertyName the property name
     * @param values       the values
     * @return the animator builder
     */
    public AnimatorBuilder property(String propertyName, float... values) {
        for (View view : mViews) {
            this.mAnimatorList.add(ObjectAnimator.ofFloat(view, propertyName, getValues(values)));
        }
        return this;
    }

    /**
     * Translation y animator builder.
     *
     * @param y the y
     * @return the animator builder
     */
    public AnimatorBuilder translationY(float... y) {
        return property("translationY", y);
    }

    /**
     * Translation x animator builder.
     *
     * @param x the x
     * @return the animator builder
     */
    public AnimatorBuilder translationX(float... x) {
        return property("translationX", x);
    }

    /**
     * Alpha animator builder.
     *
     * @param alpha the alpha
     * @return the animator builder
     */
    public AnimatorBuilder alpha(float... alpha) {
        return property("alpha", alpha);
    }

    /**
     * Scale x animator builder.
     *
     * @param scaleX the scale x
     * @return the animator builder
     */
    public AnimatorBuilder scaleX(float... scaleX) {
        return property("scaleX", scaleX);
    }

    /**
     * Scale y animator builder.
     *
     * @param scaleY the scale y
     * @return the animator builder
     */
    public AnimatorBuilder scaleY(float... scaleY) {
        return property("scaleY", scaleY);
    }

    /**
     * Scale animator builder.
     *
     * @param scale the scale
     * @return the animator builder
     */
    public AnimatorBuilder scale(float... scale) {
        scaleX(scale);
        scaleY(scale);
        return this;
    }

    /**
     * Pivot x animator builder.
     *
     * @param pivotX the pivot x
     * @return the animator builder
     */
    public AnimatorBuilder pivotX(float pivotX) {
        for (View view : mViews) {
            ViewCompat.setPivotX(view, pivotX);
        }
        return this;
    }

    /**
     * Pivot y animator builder.
     *
     * @param pivotY the pivot y
     * @return the animator builder
     */
    public AnimatorBuilder pivotY(float pivotY) {
        for (View view : mViews) {
            ViewCompat.setPivotY(view, pivotY);
        }
        return this;
    }

    public AnimatorBuilder rotationX(float... rotationX) {
        return property("rotationX", rotationX);
    }

    /**
     * Rotation y animator builder.
     *
     * @param rotationY the rotation y
     * @return the animator builder
     */
    public AnimatorBuilder rotationY(float... rotationY) {
        return property("rotationY", rotationY);
    }

    /**
     * Rotation animator builder.
     *
     * @param rotation the rotation
     * @return the animator builder
     */
    public AnimatorBuilder rotation(float... rotation) {
        return property("rotation", rotation);
    }

    /**
     * Background color animator builder.
     *
     * @param colors the colors
     * @return the animator builder
     */
    public AnimatorBuilder backgroundColor(int... colors) {
        for (View view : mViews) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(view, "backgroundColor", colors);
            objectAnimator.setEvaluator(new ArgbEvaluator());
            this.mAnimatorList.add(objectAnimator);
        }
        return this;
    }

    /**
     * Text color animator builder.
     *
     * @param colors the colors
     * @return the animator builder
     */
    public AnimatorBuilder textColor(int... colors) {
        for (View view : mViews) {
            if (view instanceof TextView) {
                ObjectAnimator objectAnimator = ObjectAnimator.ofInt(view, "textColor", colors);
                objectAnimator.setEvaluator(new ArgbEvaluator());
                this.mAnimatorList.add(objectAnimator);
            }
        }
        return this;
    }

    /**
     * Custom animator builder.
     *
     * @param update the update
     * @param values the values
     * @return the animator builder
     */
    public AnimatorBuilder custom(final AnimatorListener.Update update, float... values) {
        for (final View view : mViews) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(getValues(values));
            if (update != null)
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        //noinspection unchecked
                        update.update(view, (Float) animation.getAnimatedValue());
                    }
                });
            add(valueAnimator);
        }
        return this;
    }

    /**
     * Height animator builder.
     *
     * @param height the height
     * @return the animator builder
     */
    public AnimatorBuilder height(float... height) {
        return custom(new AnimatorListener.Update() {
            @Override
            public void update(View view, float value) {
                view.getLayoutParams().height = (int) value;
                view.requestLayout();
            }
        }, height);
    }

    /**
     * Width animator builder.
     *
     * @param width the width
     * @return the animator builder
     */
    public AnimatorBuilder width(float... width) {
        return custom(new AnimatorListener.Update() {
            @Override
            public void update(View view, float value) {
                view.getLayoutParams().width = (int) value;
                view.requestLayout();
            }
        }, width);
    }

    /**
     * Wait for height animator builder.
     *
     * @return the animator builder
     */
    public AnimatorBuilder waitForSize() {
        mWaitForSize = true;
        return this;
    }

    /**
     * Get pending animators list.
     *
     * @return the list
     */
    protected List<Animator> getPendingAnimators() {
        return mAnimatorList;
    }

    /**
     * And animate animator builder.
     *
     * @param views the mViews
     * @return the animator builder
     */
    public AnimatorBuilder andAnimate(View... views) {
        return mViewAnimator.addAnimatorBuilder(views);
    }

    /**
     * Then animate animator builder.
     *
     * @param views the mViews
     * @return the animator builder
     */
    public AnimatorBuilder thenAnimate(View... views) {
        return mViewAnimator.thenAnimate(views);
    }

    /**
     * Duration view animator.
     *
     * @param duration the duration
     * @return the animator builder
     */
    public AnimatorBuilder duration(long duration) {
        mViewAnimator.duration(duration);
        return this;
    }

    /**
     * Start delay view animator.
     *
     * @param startDelay the start delay
     * @return the animator builder
     */
    public AnimatorBuilder startDelay(long startDelay) {
        mViewAnimator.startDelay(startDelay);
        return this;
    }

    /**
     * Repeat count of animator.
     *
     * @param repeatCount the repeat count
     * @return the animator builder
     */
    public AnimatorBuilder repeatCount(@IntRange(from = -1) int repeatCount) {
        mViewAnimator.repeatCount(repeatCount);
        return this;
    }

    /**
     * Repeat mode view animator.
     *
     * @param repeatMode the repeat mode
     * @return the animator builder
     */
    public AnimatorBuilder repeatMode(@ViewAnimator.RepeatMode int repeatMode) {
        mViewAnimator.repeatMode(repeatMode);
        return this;
    }

    /**
     * On start view animator.
     *
     * @param startListener the start listener
     * @return the animator builder
     */
    public AnimatorBuilder onStart(AnimatorListener.Start startListener) {
        mViewAnimator.onStart(startListener);
        return this;
    }

    /**
     * On stop view animator.
     *
     * @param endListener the stop listener
     * @return the animator builder
     */
    public AnimatorBuilder onEnd(AnimatorListener.End endListener) {
        mViewAnimator.onEnd(endListener);
        return this;
    }

    /**
     * Interpolator for all view animators.
     *
     * @param interpolator the interpolator
     * @return the animator builder
     */
    public AnimatorBuilder interpolator(Interpolator interpolator) {
        mViewAnimator.interpolator(interpolator);
        return this;
    }

    public AnimatorBuilder setSingleInterpolator(Interpolator interpolator) {
        mSingleInterpolator = interpolator;
        return this;
    }

    public Interpolator getSingleInterpolator() {
        return mSingleInterpolator;
    }

    /**
     * Set accelerate interpolator
     * @return ViewAnimator
     */
    public ViewAnimator accelerate() {
        return mViewAnimator.interpolator(new AccelerateInterpolator());
    }

    /**
     * Set decelerate interpolator
     * @return ViewAnimator
     */
    public ViewAnimator decelerate() {
        return mViewAnimator.interpolator(new DecelerateInterpolator());
    }

    /**
     * Start current animator
     */
    public ViewAnimator start() {
        mViewAnimator.start();
        return mViewAnimator;
    }

    /**
     * Get mViews view [ ].
     *
     * @return the view [ ]
     */
    public View[] getViews() {
        return mViews;
    }

    /**
     * Wait the view be inflated
     *
     * @return the boolean
     */
    public boolean isWaitForSize() {
        return mWaitForSize;
    }
}
