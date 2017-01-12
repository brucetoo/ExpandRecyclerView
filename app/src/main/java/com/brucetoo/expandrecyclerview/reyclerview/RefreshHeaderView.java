package com.brucetoo.expandrecyclerview.reyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brucetoo.expandrecyclerview.R;

/**
 * Created by Bruce Too
 * On 04/01/2017.
 * At 15:46
 */

public class RefreshHeaderView extends LinearLayout implements IRefreshHeader {

    private static final String TAG = "RefreshHeaderView";
    private View mRootView;
    private TextView mTextPullRefresh;
    private TextView mTextLastRefreshTime;
    private View mLayoutRefreshDown;
    private ImageView mArrowIcon;
    private ProgressBar mProgressRefreshing;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    private static final int ANIM_ROTATE_DURATION = 150;
    private static final int ANIM_REFRESH_DOWN_DELAY = 500;

    private HeaderState mCurrentState = HeaderState.VIEW_STATE_NORMAL;
    private int mMeasureHeight;
    private long mLastUpdateTime = -1;

    private String mPullToRefreshText = "Pull To Refresh";
    private String mReleaseToRefreshText = "Release To Refresh";
    private String mRefreshingText = "Refreshing";
    private String mRefreshedSuccessText = "Refresh Success";
    private String mRefreshedFailedText = "Refresh Failed";
    private boolean mIsRefreshSuccess = true;

    public RefreshHeaderView(Context context) {
        super(context);
        init();
    }

    public RefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mRootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_header, null);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        setLayoutParams(lp);

        addView(mRootView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.BOTTOM);

        mTextPullRefresh = (TextView) mRootView.findViewById(R.id.txt_pull_refresh);
        mTextLastRefreshTime = (TextView) mRootView.findViewById(R.id.txt_last_refresh_time);
        mLayoutRefreshDown = mRootView.findViewById(R.id.layout_refresh_done);
        mArrowIcon = (ImageView) mRootView.findViewById(R.id.img_header_arrow);
        mProgressRefreshing = (ProgressBar) mRootView.findViewById(R.id.pb_header_progressbar);

        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ANIM_ROTATE_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ANIM_ROTATE_DURATION);
        mRotateDownAnim.setFillAfter(true);

        measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mMeasureHeight = getMeasuredHeight();
        Log.e(TAG, "init -> mMeasureHeight:" + mMeasureHeight);
        setRefreshHeaderHeight(0);
    }

    @Override
    public int getRefreshHeaderHeight() {
        LayoutParams lp = (LayoutParams) mRootView.getLayoutParams();
        return lp.height;
    }

    @Override
    public void setStateDesc(String pullToRefresh, String releaseToRefresh, String refreshing, String refreshedSuccess,String refreshedFailed) {
        this.mPullToRefreshText = pullToRefresh;
        this.mReleaseToRefreshText = releaseToRefresh;
        this.mRefreshingText = refreshing;
        this.mRefreshedSuccessText = refreshedSuccess;
        this.mRefreshedFailedText = refreshedFailed;
    }

    public void setRefreshHeaderHeight(int height) {
        LayoutParams lp = (LayoutParams) mRootView.getLayoutParams();
        lp.height = height;
        mRootView.setLayoutParams(lp);
    }

    @Override
    public void updateState(HeaderState state) {
        if (mCurrentState == state) return;

        switch (state) {
            case VIEW_STATE_NORMAL:
                onNormal();
                break;
            case VIEW_STATE_RELEASE_REFRESH:
                onReleaseRefresh();
                break;
            case VIEW_STATE_REFRESHING:
                onRefreshing();
                break;
            case VIEW_STATE_REFRESHED:
                onRefreshed();
                break;
        }

        mCurrentState = state;
    }

    @Override
    public void onRefreshState(boolean success) {
        mIsRefreshSuccess = success;
    }

    @Override
    public HeaderState getCurrentState() {
        return mCurrentState;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public int onPullDown(float delta) {

//        Log.e(TAG, "onPullDown: delta" + delta + " getRefreshHeaderHeight:" + getRefreshHeaderHeight());
        if (getRefreshHeaderHeight() > 0 || delta > 0) {
            int height = (int) delta + getRefreshHeaderHeight();
            setRefreshHeaderHeight(height);
            if (mCurrentState.ordinal() <= HeaderState.VIEW_STATE_RELEASE_REFRESH.ordinal()) {
                if (getRefreshHeaderHeight() > mMeasureHeight) {
                    updateState(HeaderState.VIEW_STATE_RELEASE_REFRESH);
                } else {
                    updateState(HeaderState.VIEW_STATE_NORMAL);
                }
            }
            return height;
        }

        return 0;
    }

    @Override
    public void onNormal() {
        Log.e(TAG, "onNormal");
        mTextPullRefresh.setText(mPullToRefreshText);
        mArrowIcon.setVisibility(VISIBLE);
        mLayoutRefreshDown.setVisibility(GONE);
        mProgressRefreshing.setVisibility(GONE);

        //VIEW_STATE_RELEASE_REFRESH -> VIEW_STATE_NORMAL
        if (mCurrentState == HeaderState.VIEW_STATE_RELEASE_REFRESH) {
            mArrowIcon.startAnimation(mRotateDownAnim);
        }

        if (mCurrentState == HeaderState.VIEW_STATE_REFRESHING) {
            mArrowIcon.clearAnimation();
        }
    }

    @Override
    public void onReleaseRefresh() {
        Log.e(TAG, "onReleaseRefresh");
        mLayoutRefreshDown.setVisibility(GONE);
        mProgressRefreshing.setVisibility(GONE);
        //VIEW_STATE_NORMAL -> VIEW_STATE_RELEASE_REFRESH -> VIEW_STATE_REFRESHING
        if (mCurrentState != HeaderState.VIEW_STATE_REFRESHING) {
            mArrowIcon.setVisibility(VISIBLE);
            mArrowIcon.clearAnimation();
            mArrowIcon.startAnimation(mRotateUpAnim);
            mTextPullRefresh.setText(mReleaseToRefreshText);
        }
    }

    @Override
    public void onRefreshing() {
        Log.e(TAG, "onRefreshing");

        mArrowIcon.clearAnimation();
        mArrowIcon.setVisibility(INVISIBLE);
        mLayoutRefreshDown.setVisibility(GONE);
        mProgressRefreshing.setVisibility(VISIBLE);
        mTextPullRefresh.setText(mRefreshingText);
        smoothScrollTo(mMeasureHeight,0);

        //for test
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                updateState(IRefreshHeader.VIEW_STATE_REFRESHED);
//            }
//        }, 3000);
    }

    @Override
    public void onRefreshed() {
        Log.e(TAG, "onRefreshed");

        mArrowIcon.setVisibility(VISIBLE);
        mProgressRefreshing.setVisibility(GONE);
        if(mIsRefreshSuccess) {
            mTextPullRefresh.setText(mRefreshedSuccessText);
        }else {
            mTextPullRefresh.setText(mRefreshedFailedText);
        }
        if(mLastUpdateTime != -1) {
            mLayoutRefreshDown.setVisibility(VISIBLE);
            mTextLastRefreshTime.setText(makeTimeReadable(mLastUpdateTime));
        }else {
            mLastUpdateTime = System.currentTimeMillis();
            mLayoutRefreshDown.setVisibility(GONE);
        }
        smoothScrollTo(0,ANIM_REFRESH_DOWN_DELAY);
    }

    @Override
    public boolean onPullRelease() {

        boolean isRefreshing = false;
        int height = getRefreshHeaderHeight();
        Log.e(TAG, "onPullRelease: height:" + height);
        if (height > mMeasureHeight && mCurrentState.ordinal() < HeaderState.VIEW_STATE_REFRESHING.ordinal()) {
            updateState(HeaderState.VIEW_STATE_REFRESHING);
            isRefreshing = true;
        }

        //move to top
        if (mCurrentState != HeaderState.VIEW_STATE_REFRESHING) {
            smoothScrollTo(0,0);
        } else {//move to refresh height
            smoothScrollTo(mMeasureHeight,0);
        }

        return isRefreshing;
    }

    private void resetState() {
        mCurrentState = HeaderState.VIEW_STATE_NORMAL;
        mTextPullRefresh.setText(mPullToRefreshText);
        mArrowIcon.setVisibility(VISIBLE);
        mLayoutRefreshDown.setVisibility(GONE);
        mProgressRefreshing.setVisibility(GONE);
    }

    private void smoothScrollTo(final int destHeight,long delay) {
        ValueAnimator animator = ValueAnimator.ofInt(getRefreshHeaderHeight(), destHeight);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setRefreshHeaderHeight((int) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (destHeight == 0) {
                    resetState();
                }
            }
        });
        animator.setStartDelay(delay);
        animator.start();
    }

    public static String makeTimeReadable(long time) {
        //获取time距离当前的秒数
        int ct = (int)((System.currentTimeMillis() - time) / 1000);

        if (ct == 0) {
            return "刚刚";
        }

        if (ct > 0 && ct < 60) {
            return ct + "秒前";
        }

        if (ct >= 60 && ct < 3600) {
            return Math.max(ct / 60, 1) + "分钟前";
        }
        if (ct >= 3600 && ct < 86400)
            return ct / 3600 + "小时前";
        if (ct >= 86400 && ct < 2592000) { //86400 * 30
            int day = ct / 86400;
            return day + "天前";
        }
        if (ct >= 2592000 && ct < 31104000) { //86400 * 30
            return ct / 2592000 + "月前";
        }
        return ct / 31104000 + "年前";
    }
}
