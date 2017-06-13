package com.brucetoo.expandrecyclerview.lockscreen;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;

import com.brucetoo.expandrecyclerview.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruce Too
 * On 13/06/2017.
 * At 15:31
 */

public class SwipeableLayout extends ViewGroup {

    private static final String TAG = SwipeableLayout.class.getSimpleName();

    private static final int DEFAULT_MAX_WIDTH = 500;

    private boolean mDragLeft = false;
    private boolean mDragTop = false;
    private boolean mDragRight = false;
    private boolean mDragBottom = false;

    private ViewDragHelper mViewDragHelper;

    private float mInitMotionX;
    private float mInitMotionY;

    private int mDragMaxWidth;//0 , default max width is screen width
    @IdRes
    private int mDragViewResId;
    @IdRes
    private int mContentViewResId;
    private View mDragView;
    private View mContentView;

    private int mDragViewLeft;
    private int mDragViewTop;
    private int mScreenWidth;
    private int mScreenHeight;
    private float mDragRatio;
    private View mReleaseView;
    private List<DragListener> mDragRatioListeners = new ArrayList<DragListener>();
    private boolean mCanDragHorizontal = false;
    private boolean mCanDragVertical = false;
    private boolean mCanRecordDirection = true;
    private ListView mListView;

    private boolean mNeedDragHorizontal = false;
    private boolean mNeedDragVertical = false;

    public SwipeableLayout(Context context) {
        this(context, null);
    }

    public SwipeableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SwipeableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SwipeableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SwipeableLayout);
        if (array != null) {
            mDragMaxWidth = array.getDimensionPixelSize(R.styleable.SwipeableLayout_dragMaxWidth, DEFAULT_MAX_WIDTH);
            mDragViewResId = array.getResourceId(R.styleable.SwipeableLayout_dragView, -1);
            mContentViewResId = array.getResourceId(R.styleable.SwipeableLayout_contentView, -1);
            array.recycle();
        }

        //Init ViewDragHelper
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallBack());
        //Disable edge drag
//        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL | ViewDragHelper.EDGE_BOTTOM);

        //left bound of dragView default equals screen width
        mDragViewLeft = mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = context.getResources().getDisplayMetrics().heightPixels;

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                smoothSlideToEdgeWithDuration(false, true, 0);
                return false;
            }
        });
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mDragViewResId != -1) {
            mDragView = findViewById(mDragViewResId);
        }
        if (mContentViewResId != -1) {
            mContentView = findViewById(mContentViewResId);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //get the extra width and height with spec
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        final int childCount = getChildCount();//except maskView

        if (childCount != 2) {//only have 2 children except maskView
            throw new IllegalArgumentException("Drawer layout must have exactly 2 children!");
        }

        if (mDragMaxWidth > mScreenWidth) {//dragMaxWidth can't be greater than screen width
            throw new IllegalArgumentException("Drawer width can't be greater than screen width!");
        }

        if (mDragMaxWidth == 0) {
            mDragMaxWidth = mScreenWidth;
        }


        //TODO if there are padding or margin values in this ViewGroup,may has some problems

        /**
         handle {@link mDragView} with layoutParams.
         NOTE:if {@link mDragView} has exactly width,{@link mDragMaxWidth} is not available
         */
        ViewGroup.LayoutParams dragParams = mDragView.getLayoutParams();
        int mDragViewWidthSpec;
        if (dragParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            mDragViewWidthSpec = MeasureSpec.makeMeasureSpec(mDragMaxWidth, MeasureSpec.AT_MOST);
        } else if (dragParams.width == LayoutParams.MATCH_PARENT) {
            mDragViewWidthSpec = MeasureSpec.makeMeasureSpec(mDragMaxWidth, MeasureSpec.EXACTLY);
        } else {
            mDragMaxWidth = dragParams.width;
            mDragViewWidthSpec = MeasureSpec.makeMeasureSpec(dragParams.width, MeasureSpec.EXACTLY);
        }
        //measure {@link mDragView}
        mDragView.measure(mDragViewWidthSpec, heightMeasureSpec);

        /**
         * handle {@link mContentView}
         * NOTE: the height and width must be MATCH_PARENT
         */
        LayoutParams contentParams = mContentView.getLayoutParams();
        mContentView.measure(widthMeasureSpec, heightMeasureSpec);
        if (contentParams.width != LayoutParams.MATCH_PARENT && contentParams.height == LayoutParams.MATCH_PARENT) {
            throw new IllegalArgumentException("Content View width/height must be MATCH_PARENT");
        }

        setMeasuredDimension(maxWidth, maxHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        mContentView.layout(0, 0, mScreenWidth, mScreenHeight);
        //bottom = mScreenHeight + NavigationBar to make sure the immerse status bar doesn't affect back scrolling blinking
        mDragView.layout(mDragViewLeft, mDragViewTop, mDragViewLeft + mDragMaxWidth, (int) (mScreenHeight + mDragViewTop + getNavigationBarHeight()));
        bringChildToFront(mDragView);//force bring dragView to front in case the wrong z value.
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mViewDragHelper.cancel();
            return false;
        }

        float moveX = ev.getX();
        float moveY = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mInitMotionX = moveX;
                mInitMotionY = moveY;
                break;
            case MotionEvent.ACTION_MOVE:
                float adx = Math.abs(moveX - mInitMotionX);
                float ady = Math.abs(moveY - mInitMotionY);
                int touchSlop = mViewDragHelper.getTouchSlop();

                if (mNeedDragVertical) {
                    //drag vertical
                    if (ady > adx && ady > touchSlop && mCanRecordDirection) {
                        if (mListView != null) {
                            int pos = mListView.getFirstVisiblePosition();
                            if (pos != 0) {
                                Log.e(TAG, " CanDragVertical pos != 0");
                                mViewDragHelper.cancel();
                                return false;
                            } else {
                                if (mListView.getChildAt(0).getTop() != 0 || (moveY - mInitMotionY) < 0) {
                                    Log.e(TAG, " CanDragVertical pos == 0  top != 0");
                                    mViewDragHelper.cancel();
                                    return false;
                                }
                            }
                        }
                        mCanDragHorizontal = false;
                        mCanDragVertical = true;
                        mCanRecordDirection = false;
                        Log.e(TAG, " CanDragVertical");
                    }
                } else {
                    mCanRecordDirection = true;
                }

                if (mNeedDragHorizontal) {
                    //drag horizontal
                    if (adx > ady && adx > touchSlop && mCanRecordDirection) {
                        mCanDragHorizontal = true;
                        mCanDragVertical = false;
                        mCanRecordDirection = false;
                        Log.e(TAG, " CanDragHorizontal");
                    }
                } else {
                    mCanRecordDirection = true;
                }
                break;
        }

        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //let ViewDragHelper to handle touch event
        mViewDragHelper.processTouchEvent(event);
        float moveX = event.getX();
        float moveY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitMotionX = moveX;
                mInitMotionY = moveY;
                break;
            case MotionEvent.ACTION_UP:

                if (mCanDragHorizontal && mNeedDragHorizontal) {
                    if (mDragViewLeft > (getWidth() / 3) || mDragViewLeft < -(getWidth() / 3)) {
                        closeDrawer(true);
                    } else {
                        if (mReleaseView != null && mReleaseView == mDragView) {
                            mReleaseView = null;
                            Log.i(TAG, "onTouchEvent openDrawer");
                            openDrawer(true);
                        }
                    }
                }

                if (mCanDragVertical && mNeedDragVertical) {
                    if (mDragViewTop > getHeight() / 5 || mDragViewTop < -getHeight() / 5) {
                        closeDrawer(false);
                    } else {
                        if (mReleaseView != null && mReleaseView == mDragView) {
                            mReleaseView = null;
                            openDrawer(false);
                        }
                    }
                }

                mCanRecordDirection = true;
                break;
            case MotionEvent.ACTION_CANCEL:
                mCanRecordDirection = true;
                break;
        }

        return true;
    }

    @Override
    public void computeScroll() {
        /**
         * viewDragHelper response {@link android.widget.Scroller Scroller}
         */
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    private class DragHelperCallBack extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return mDragView == child;
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            switch (edgeFlags) {
                case ViewDragHelper.EDGE_RIGHT:
                    Log.i(TAG, "onEdgeDragStarted:to right edge drag start");
                    mViewDragHelper.captureChildView(mDragView, pointerId);
                    break;
                case ViewDragHelper.EDGE_BOTTOM:
                    Log.i(TAG, "onEdgeDragStarted:to bottom edge drag start");
                    mViewDragHelper.captureChildView(mDragView, pointerId);
                    break;
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (child == mDragView && mCanDragVertical && mNeedDragVertical) {
                return constrain(top, mDragBottom ? -mScreenHeight : 0, mDragTop ? mScreenHeight : 0);
            }
            return super.clampViewPositionVertical(child, top, dy);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mDragView && mCanDragHorizontal && mNeedDragHorizontal) {
//                Log.i(TAG, "clampViewPositionHorizontal");
                float rectLeft = getWidth() - mDragMaxWidth;
                float rectRight = getWidth();
                //mDragView's drag bounds,left range must in rectLeft < left < rectRight
                return (int) Math.min(Math.max(left, mDragRight ? -rectRight : 0), mDragLeft ? rectRight : 0);
            } else {
                return super.clampViewPositionHorizontal(child, left, dx);
            }
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mCanDragVertical && mNeedDragVertical ? mScreenHeight : 0;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mCanDragHorizontal && mNeedDragHorizontal ? mDragMaxWidth : 0;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {

            if (changedView == mDragView) {
                mDragViewLeft = left;
                mDragViewTop = top;
                if (mCanDragHorizontal) {
                    mDragRatio = (float) (getWidth() - Math.abs(left)) / mDragMaxWidth;
                } else {
                    mDragRatio = (float) (getHeight() - Math.abs(top)) / getHeight();
                }
                for (DragListener listener : mDragRatioListeners) {
                    listener.onDragRatioChange(mDragRatio, mDragView, left, top, mCanDragHorizontal);
                }
                requestLayout();
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            mReleaseView = releasedChild;
            if (releasedChild == mDragView) {
                Log.i(TAG, "onViewReleased" + " xvel:" + xvel + " yvel:" + yvel);
                //  horizontal x velocity:  left -> right  >0
                //  horizontal x velocity:  right -> left  <0
                int finalLeft = 0;
                int finalTop = 0;
                if (xvel < 0 || (xvel == 0 && mDragRatio > 0.5f)) {
                    finalLeft = getWidth() - mDragMaxWidth;
                } else {
                    finalLeft = getWidth();
                }
                // yvel < 0 if bottom -> top
//                if(yvel < 0 || (yvel == 0 && mDragRatio > 0.5f)){
//                    finalTop = -getHeight();
//                }else {
//                    finalTop = 0;
//                }
                mViewDragHelper.settleCapturedViewAt(finalLeft, 0);
                invalidate();
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            for (DragListener listener : mDragRatioListeners) {
                listener.onDragStateChanged(state);
            }
            if (state == ViewDragHelper.STATE_IDLE) {
                //recover to original state
                mCanDragHorizontal = false;
                mCanDragVertical = false;

                if (Math.abs(mDragViewLeft) == mScreenWidth || Math.abs(mDragViewTop) == mScreenHeight - (mDragBottom ? -getNavigationBarHeight() : getHeightByVersion())) {
                    for (DragListener listener : mDragRatioListeners) {
                        Log.e(TAG, "horizontal:" + (mDragViewLeft == mScreenWidth) + " vertical:" + (mDragViewTop == mScreenHeight));
                        mDragView.setVisibility(GONE);
                        listener.onRelease2EdgeEnd(Math.abs(mDragViewLeft) == mScreenWidth);
                    }
                }

                mDragViewLeft = 0;
                mDragViewTop = 0;
            }
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }
    }

    private int getNavigationBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return getResources().getDimensionPixelSize(resourceId);
    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return getResources().getDimensionPixelSize(resourceId);
    }

    private int getHeightByVersion() {
        int[] location = new int[2];
        getLocationOnScreen(location);
        //判断当前容器是否置顶,用以通用的区分是否沉浸式
        if (location[1] > 0)
            return getStatusBarHeight();
        return 0;
    }

    /**
     * set the max value of {@link SwipeableLayout#mDragMaxWidth}
     * NOTE:dragMaxWidth can't be greater than screen width
     * When {@link SwipeableLayout#mDragView} has extra width,
     * {@link SwipeableLayout#mDragMaxWidth} will be invalid
     *
     * @param dragMaxWidth max width can be dragged
     * @see SwipeableLayout#onMeasure(int, int)
     */
    public void setDragMaxWidth(int dragMaxWidth) {
        this.mDragMaxWidth = dragMaxWidth;
        requestLayout();
    }

    /**
     * Open {@link SwipeableLayout#mDragView}
     *
     * @param horizontal open by horizontal or vertical
     */
    public void openDrawer(boolean horizontal) {
        smoothSlideToEdge(false, horizontal);
    }

    /**
     * Close {@link SwipeableLayout#mDragView}
     *
     * @param horizontal close by horizontal or vertical
     */
    public void closeDrawer(boolean horizontal) {
        smoothSlideToEdge(true, horizontal);
    }

    /**
     * Close {@link SwipeableLayout#mDragView} with duration
     *
     * @param horizontal close by horizontal or vertical
     * @param duration   duration be closed
     */
    public void closeDrawerWithDuration(boolean horizontal, int duration) {
        smoothSlideToEdgeWithDuration(true, horizontal, duration);
    }

    /**
     * get if {@link SwipeableLayout#mDragView} is opened
     */
    public boolean isDrawerOpen() {
        boolean isOpen = ((mDragViewLeft == mScreenWidth) && mDragView.getLeft() == 0)
            || ((mDragViewTop == mScreenHeight) && mDragView.getTop() == 0)
            || ((mDragViewLeft == 0) && (mDragViewTop == 0));
        Log.e(TAG, "isDrawerOpen isOpen: " + isOpen + " Horizontal:" + (mDragViewLeft == mScreenWidth) + "  mDragView.getLeft() == 0:" + (mDragView.getLeft() == 0)
            + " Vertical:" + (mDragViewTop == mScreenHeight) + " mDragView.getTop() == 0:" + (mDragView.getTop() == 0));
        return isOpen;
    }

    public void addDragListener(DragListener listener) {
        mDragRatioListeners.add(listener);
    }

    public void removeDragListener(DragListener listener) {
        mDragRatioListeners.remove(listener);
    }

    /**
     * smooth slide {@link SwipeableLayout#mDragView} to edge
     * drive by {@link ViewDragHelper#smoothSlideViewTo(View, int, int)} method
     *
     * @param toEdge slide to edge?
     */
    private void smoothSlideToEdge(boolean toEdge, boolean horizontal) {
        //TODO maybe padding values need be considered
//        final int leftBound = getPaddingLeft();
        int finalLeft = 0;
        int finalTop = 0;
        if (horizontal) {
            if (!toEdge) {
                finalLeft = 0;
            } else {
                finalLeft = mDragViewLeft < 0 ? -getWidth() : getWidth();
            }
        } else {
            if (!toEdge) {
                finalTop = 0;
            } else {
                finalTop = mDragViewTop < 0 ? -getHeight() : getHeight();
            }
        }

        if (mViewDragHelper.smoothSlideViewTo(mDragView, finalLeft, finalTop)) {
            //force invalidate before next frame comes.
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void smoothSlideToEdgeWithDuration(boolean toEdge, boolean horizontal, int duration) {
        //TODO maybe padding values need be considered
//        final int leftBound = getPaddingLeft();
        int finalLeft = 0;
        int finalTop = 0;
        if (horizontal) {
            if (!toEdge) {
                finalLeft = 0;
            } else {
                finalLeft = getWidth();
            }
        } else {
            if (!toEdge) {
                finalTop = 0;
            } else {
                finalTop = getHeight();
            }
        }

        if (mViewDragHelper.smoothSlideViewTo(mDragView, finalLeft, finalTop)) {
            //force invalidate before next frame comes.
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    public void setListView(ListView listView) {
        this.mListView = listView;
    }

    /**
     * Options to enable horizontal or vertical drag.
     *
     * @param needDragHorizontal can drag horizontal
     * @param needDragVertical   can drag vertical
     * @deprecated
     */
    public void setDragOptions(boolean needDragHorizontal, boolean needDragVertical) {
        this.mNeedDragHorizontal = needDragHorizontal;
        this.mNeedDragVertical = needDragVertical;
    }

    public void setDragOptions(boolean dragLeft, boolean dragTop, boolean dragRight, boolean dragBottom) {
        this.mDragLeft = dragLeft;
        this.mDragTop = dragTop;
        this.mDragRight = dragRight;
        this.mDragBottom = dragBottom;
        if (mDragLeft || mDragRight) {
            this.mNeedDragHorizontal = true;
        } else {
            this.mNeedDragHorizontal = false;
        }

        if (mDragTop || mDragBottom) {
            this.mNeedDragVertical = true;
        } else {
            this.mNeedDragVertical = false;
        }
    }


    /**
     * the listener when drag happened
     */
    public interface DragListener {
        /**
         * callback of ratio changed when drag
         *
         * @param ratio      drag ration
         * @param dragView   captured/dragged view
         * @param leftDelta  drag left delta of dragged view
         * @param topDelta   drag top delta
         * @param horizontal scroll horizontal
         */
        void onDragRatioChange(float ratio, View dragView, float leftDelta, float topDelta, boolean horizontal);

        /**
         * callback when release state is IDLE
         * Only be called when close drawer end.
         *
         * @param horizontal horizontal to dismiss?
         */
        void onRelease2EdgeEnd(boolean horizontal);

        /**
         * callback when drag state changed
         *
         * @param state drag state
         */
        void onDragStateChanged(int state);
    }
}
