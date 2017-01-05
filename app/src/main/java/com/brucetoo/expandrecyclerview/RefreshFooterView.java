package com.brucetoo.expandrecyclerview;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Bruce Too
 * On 04/01/2017.
 * At 15:46
 */

public class RefreshFooterView extends LinearLayout implements IRefreshFooter {

    private View mRootView;
    private View mLoadingView;
    private TextView mTextLoading;
    private FooterState mCurrentState = IRefreshFooter.FooterState.VIEW_STATE_LOADING;

    private String mLoadingDesc = "Loading...";
//    private String mLoadedDesc = "Loaded";
    private String mNoMoreDesc = "No More Data";

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
    public void onLoading(String loading) {
        setVisibility(VISIBLE);
        mLoadingView.setVisibility(VISIBLE);
        if(TextUtils.isEmpty(loading)){
            mTextLoading.setText(mLoadingDesc);
        }else {
            mTextLoading.setText(loading);
        }
    }

    @Override
    public void onComplete() {
        setVisibility(GONE);
    }

    @Override
    public void onNoMore(String noMore) {
        setVisibility(VISIBLE);
        mLoadingView.setVisibility(GONE);

        if(TextUtils.isEmpty(noMore)){
            mTextLoading.setText(mNoMoreDesc);
        }else {
            mTextLoading.setText(noMore);
        }
    }

    @Override
    public void updateState(FooterState state,String stateDesc) {
        if(mCurrentState == state) return;

        switch (state){
            case VIEW_STATE_LOADING:
                onLoading(stateDesc);
                break;
            case VIEW_STATE_COMPLETE:
                onComplete();
                break;
            case VIEW_STATE_NO_MORE:
                onNoMore(stateDesc);
                break;
        }

        mCurrentState = state;
    }
}
