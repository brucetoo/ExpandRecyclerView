package com.brucetoo.expandrecyclerview.reyclerview;

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

    void onLoading();

    void onComplete();

    void onNoMore();

    void updateState(FooterState state);

    void setStateDesc(String loadingDesc,String noMoreDesc);
}
