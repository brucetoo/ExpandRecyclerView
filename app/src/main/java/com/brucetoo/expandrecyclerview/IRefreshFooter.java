package com.brucetoo.expandrecyclerview;

import android.view.View;

/**
 * Created by Bruce Too
 * On 04/01/2017.
 * At 15:43
 */

public interface IRefreshFooter {

    enum FooterState{
        VIEW_STATE_LOADING,
        VIEW_STATE_COMPLETE,
        VIEW_STATE_NO_MORE
    }

    View getView();

    void onLoading(String loading);

    void onComplete();

    void onNoMore(String noMore);

    void updateState(FooterState state, String stateText);
}
