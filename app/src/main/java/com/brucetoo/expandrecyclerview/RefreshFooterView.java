package com.brucetoo.expandrecyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import static com.brucetoo.expandrecyclerview.IRefreshFooter.FooterState.VIEW_STATE_LOADING;

/**
 * Created by Bruce Too
 * On 04/01/2017.
 * At 15:46
 */

public class RefreshFooterView extends LinearLayout implements IRefreshFooter {

    private View mRootView;
    private View mLoadingView;
    private TextView mTextLoading;
    private FooterState mCurrentState = VIEW_STATE_LOADING;

    private String mLoadMoreText = "Loading...";
    private String mLoadNoMoreText = "No More Data";

    public RefreshFooterView(Context context) {
        super(context);
        init();
    }

    public RefreshFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mRootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_footer, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        setLayoutParams(lp);

        addView(mRootView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.BOTTOM);

        mLoadingView = mRootView.findViewById(R.id.pb_footer_progressbar);
        mTextLoading = (TextView) mRootView.findViewById(R.id.txt_loading);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onLoading() {
        setVisibility(VISIBLE);
        mLoadingView.setVisibility(VISIBLE);
        mTextLoading.setText(mLoadMoreText);
    }

    @Override
    public void onComplete() {
        setVisibility(GONE);
    }

    @Override
    public void onNoMore() {
        setVisibility(VISIBLE);
        mLoadingView.setVisibility(GONE);
        mTextLoading.setText(mLoadNoMoreText);
    }

    @Override
    public void setStateDesc(String loadingDesc,String noMoreDesc) {
        this.mLoadMoreText = loadingDesc;
        this.mLoadNoMoreText = noMoreDesc;
    }

    @Override
    public void updateState(FooterState state) {
        if(mCurrentState == state) return;

        switch (state){
            case VIEW_STATE_LOADING:
                onLoading();
                break;
            case VIEW_STATE_COMPLETE:
                onComplete();
                break;
            case VIEW_STATE_NO_MORE:
                onNoMore();
                break;
        }

        mCurrentState = state;
    }
}
