package com.brucetoo.expandrecyclerview;

import android.view.View;

/**
 * Created by Bruce Too
 * On 04/01/2017.
 * At 15:43
 */

public interface IRefreshHeader {

    enum HeaderState {
        VIEW_STATE_NORMAL,
        VIEW_STATE_RELEASE_REFRESH,
        VIEW_STATE_REFRESHING,
        VIEW_STATE_REFRESHED
    }

    View getView();

    int onPullDown(float delta);

    void onNormal();

    void onReleaseRefresh();

    void onRefreshing();

    void onRefreshed();

    boolean onPullRelease();

    void updateState(HeaderState state);

    void onRefreshState(boolean success);

    HeaderState getCurrentState();

    int getRefreshHeaderHeight();

    void setStateDesc(String pullToRefresh,String releaseToRefresh,String refreshing,String refreshedSuccess,String refreshedFailed);
}
