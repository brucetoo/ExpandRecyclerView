package com.brucetoo.expandrecyclerview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by Bruce Too
 * On 16/02/2017.
 * At 09:42
 */

public class ScrollableLayout extends LinearLayout {

    private static final String TAG = "ScrollableLayout";

    private float mDownX;
    private float mDownY;
    private float mLastY;

    private int mMinHeight = 0;
    private int mMaxHeight = 0;
    private int mMaxScrollY;
    @IdRes
    private int mHeaderResId;
    private int mCurrentY;

    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private DIRECTION mDirection;
    private int mLastScrollerY;
    private boolean isClickHead;

    private View mHeaderView;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private OnScrollListener onScrollListener;

    enum DIRECTION {
        UP,
        DOWN
    }

    private ScrollableHelper mHelper;

    public ScrollableHelper getHelper() {
        return mHelper;
    }

    public ScrollableLayout(Context context) {
        this(context, null);
    }

    public ScrollableLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ScrollableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScrollableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mHelper = new ScrollableHelper();
        mScroller = new Scroller(context);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ScrollableLayout);
        if (array != null) {
            mMinHeight = array.getDimensionPixelSize(R.styleable.ScrollableLayout_headerMinHeight, 0);
            mMaxHeight = array.getDimensionPixelSize(R.styleable.ScrollableLayout_headerMaxHeight, 0);
            mHeaderResId = array.getResourceId(R.styleable.ScrollableLayout_headerView, -1);
            array.recycle();
        }

        mMaxScrollY = mMaxHeight - mMinHeight;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float currentX = ev.getX();
        float currentY = ev.getY();
        float deltaY;
        int shiftX = (int) Math.abs(currentX - mDownX);
        int shiftY = (int) Math.abs(currentY - mDownY);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = currentX;
                mDownY = currentY;
                mLastY = currentY;
                checkIsClickHead((int) currentY, mMaxScrollY, getScrollY());
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                mScroller.forceFinished(true);
                break;
            case MotionEvent.ACTION_MOVE:
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(ev);
                deltaY = mLastY - currentY;

                if (shiftY > mTouchSlop && (!isHeaderStickied() || mHelper.isTop())) {
                    Log.d(TAG, "ACTION_MOVE mCurrentY:" + mCurrentY + " mMaxScrollY:" + mMaxScrollY);
                    scrollBy(0, (int) (deltaY + 0.5));
                }
                mLastY = currentY;
                break;
            case MotionEvent.ACTION_UP:
                if (shiftY > mTouchSlop) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    float yVelocity = -mVelocityTracker.getYVelocity();
                    boolean disallowChild = false;
                    if (Math.abs(yVelocity) > mMinimumVelocity) {
                        mDirection = yVelocity > 0 ? DIRECTION.UP : DIRECTION.DOWN;
                        Log.i(TAG, "dispatchTouchEvent mDirection:" + mDirection + " isHeaderStickied:" + isHeaderStickied() + " getScrollY():" + getScrollY());
                        if ((mDirection == DIRECTION.UP && isHeaderStickied()) || (!isHeaderStickied() && getScrollY() == mMinHeight && mDirection == DIRECTION.DOWN)) {
                            disallowChild = true;
                        } else {
                            mScroller.fling(0, getScrollY(), 0, (int) yVelocity, 0, 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                            mScroller.computeScrollOffset();
                            mLastScrollerY = getScrollY();
                            invalidate();
                        }
                    }
                    if (!disallowChild && (isClickHead || !isHeaderStickied())) {
                        int action = ev.getAction();
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        boolean dispatchResult = super.dispatchTouchEvent(ev);
                        ev.setAction(action);
                        return dispatchResult;
                    }
                }
                break;
            default:
                break;
        }
        super.dispatchTouchEvent(ev);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private int getScrollerVelocity(int distance, int duration) {
        if (mScroller == null) {
            return 0;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return (int) mScroller.getCurrVelocity();
        } else {
            return distance / duration;
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            final int currY = mScroller.getCurrY();
            Log.e(TAG, "computeScroll currY:" + currY + " mDirection:" + mDirection + " isHeaderStickied():" + isHeaderStickied());
            if (mDirection == DIRECTION.UP) {
                if (isHeaderStickied()) {
                    int distance = mScroller.getFinalY() - currY;
                    int duration = calcDuration(mScroller.getDuration(), mScroller.timePassed());
                    mHelper.smoothScrollBy(getScrollerVelocity(distance, duration), distance, duration);
                    //can't call computeScrollOffset again
                    mScroller.forceFinished(true);
                    return;
                } else {
                    scrollTo(0, currY);
                }
            } else {
                if (mHelper.isTop()) {
                    int deltaY = (currY - mLastScrollerY);
                    int toY = getScrollY() + deltaY;
                    scrollTo(0, toY);
                    if (mCurrentY <= mMinHeight) {
                        mScroller.forceFinished(true);
                        return;
                    }
                }
                invalidate();
            }
            mLastScrollerY = currY;
        }
    }

    @Override
    public void scrollBy(int x, int y) {
        int scrollY = getScrollY();
        int toY = scrollY + y;
        if (toY >= mMaxScrollY) {
            toY = mMaxScrollY;
        } else if (toY <= 0) {
            toY = 0;
        }
        y = toY - scrollY;
        super.scrollBy(x, y);
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y >= mMaxScrollY) {
            y = mMaxScrollY;
        } else if (y <= 0) {
            y = 0;
        }
        mCurrentY = y;
        if (onScrollListener != null) {
            onScrollListener.onScroll(y, mMaxScrollY);
        }
        super.scrollTo(x, y);
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void checkIsClickHead(int downY, int headHeight, int scrollY) {
        isClickHead = downY + scrollY <= headHeight;
    }

    private int calcDuration(int duration, int timePass) {
        return duration - timePass;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) + mMaxScrollY, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mHeaderResId != -1) {
            mHeaderView = findViewById(mHeaderResId);
        } else {
            throw new IllegalArgumentException("You must specify headerId in xml");
        }
        if (mHeaderView != null && !mHeaderView.isClickable()) {
            mHeaderView.setClickable(true);
        }

        /**
         * handle item view move in here
         * position change:
         * titleView -> spaceTitleView
         * btnView -> spaceBtnView
         *
         * alpha change:
         * iconView,descView,backView
         * and so on...
         */
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recycleVelocityTracker();
    }

    public void setScrollView(final ViewGroup scrollView) {
        mHelper.setScrollableView(new ScrollableHelper.ScrollableContainer() {
            @Override
            public View getScrollableView() {
                return scrollView;
            }
        });
    }

    public boolean isHeaderStickied() {
        return mCurrentY == mMaxScrollY;
    }

    public int getMaxScrollY() {
        return mMaxScrollY;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    public interface OnScrollListener {

        void onScroll(int currentY, int maxScrollY);

    }
}
