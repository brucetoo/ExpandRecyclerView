package com.brucetoo.expandrecyclerview.reyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruce Too
 * On 04/01/2017.
 * At 14:46
 */

public class ExpandRecyclerView extends RecyclerView {

    private static final int PULL_REFRESH_RATIO = 3;
    //refresh header and footer
    private static final int VIEW_TYPE_HEADER = 10000;
    private static final int VIEW_TYPE_FOOTER = 10001;
    //normal header
    private static final int VIEW_TYPE_HEADER_START = 10002;
    private SparseArray<View> mHeaderViews = new SparseArray<>();
    private List<Integer> mHeaderTypes = new ArrayList<>();

    private boolean mCanPullDownRefresh = true;
    private boolean mCanPullUpRefresh = true;

    private IRefreshHeader mRefreshHeaderView;
    private IRefreshFooter mRefreshFooterView;

    private AdapterWrapper mAdapterWrapper;
    private float mLastY = -1;

    private OnRefreshListener mOnRefreshListener;
    private List<OnPullDeltaChangeListener> mOnPullDeltaChangeListeners;

    private boolean mIsLoadingMore;
    private boolean mIsNoMoreData;

    public static class Builder {

        boolean canPullDownRefresh;
        boolean canPullUpRefresh;
        IRefreshHeader refreshHeader;
        IRefreshFooter refreshFooter;
        String pullToRefreshText;
        String releaseToRefreshText;
        String refreshingText;
        String refreshedSuccessText;
        String refreshedFailedText;
        String loadMoreText;
        String loadNoMoreText;

        public Builder setCanPullDownRefresh(boolean canPullDownRefresh) {
            this.canPullDownRefresh = canPullDownRefresh;
            return this;
        }

        public Builder setCanPullUpRefresh(boolean canPullUpRefresh) {
            this.canPullUpRefresh = canPullUpRefresh;
            return this;
        }

        public Builder setRefreshHeader(IRefreshHeader refreshHeader) {
            this.refreshHeader = refreshHeader;
            return this;
        }

        public Builder setRefreshFooter(IRefreshFooter refreshFooter) {
            this.refreshFooter = refreshFooter;
            return this;
        }

        public Builder setPullToRefreshText(String pullToRefreshText) {
            this.pullToRefreshText = pullToRefreshText;
            return this;
        }

        public Builder setReleaseToRefreshText(String releaseToRefreshText) {
            this.releaseToRefreshText = releaseToRefreshText;
            return this;
        }

        public Builder setRefreshingText(String refreshingText) {
            this.refreshingText = refreshingText;
            return this;
        }

        public Builder setRefreshedSuccessText(String refreshedSuccessText) {
            this.refreshedSuccessText = refreshedSuccessText;
            return this;
        }

        public Builder setRefreshedFailedText(String refreshedFailedText) {
            this.refreshedFailedText = refreshedFailedText;
            return this;
        }

        public Builder setLoadMoreText(String loadMoreText) {
            this.loadMoreText = loadMoreText;
            return this;
        }

        public Builder setLoadNoMoreText(String loadNoMoreText) {
            this.loadNoMoreText = loadNoMoreText;
            return this;
        }

        public void build(ExpandRecyclerView recyclerView) {

            recyclerView.mCanPullDownRefresh = this.canPullDownRefresh;
            recyclerView.mCanPullUpRefresh = this.canPullUpRefresh;
            recyclerView.mRefreshHeaderView = this.refreshHeader;
            recyclerView.mRefreshFooterView = this.refreshFooter;

            if (recyclerView.mCanPullDownRefresh && refreshHeader == null) {
                recyclerView.mRefreshHeaderView = new RefreshHeaderView(recyclerView.getContext());
            }

            if (recyclerView.mCanPullUpRefresh && refreshFooter == null) {
                recyclerView.mRefreshFooterView = new RefreshFooterView(recyclerView.getContext());
            }

            if (recyclerView.mRefreshHeaderView != null) {
                recyclerView.mRefreshHeaderView.setStateDesc(pullToRefreshText,
                    releaseToRefreshText, refreshingText, refreshedSuccessText, refreshedFailedText);
            }

            if (recyclerView.mRefreshFooterView != null) {
                recyclerView.mRefreshFooterView.setStateDesc(loadMoreText, loadNoMoreText);
            }
        }
    }

    /**
     * Listener for pull-to-refresh action
     */
    public interface OnRefreshListener {

        /**
         * when refreshing happened
         */
        void onRefreshing();

        /**
         * when load more happened
         */
        void onLoadMore();
    }

    public interface OnPullDeltaChangeListener {
        /**
         * detect refresh header view height changed
         *
         * @param delta refresh header transition delta
         */
        void onPullDeltaChange(float delta);
    }

    public ExpandRecyclerView(Context context) {
        super(context);
    }

    public ExpandRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = e.getRawY() - mLastY;
                mLastY = e.getRawY();
                if (mCanPullDownRefresh && !mIsLoadingMore && mRefreshHeaderView.getView().getParent() != null) {
                    int pullDownDis = mRefreshHeaderView.onPullDown(deltaX / PULL_REFRESH_RATIO);
                    if (mOnPullDeltaChangeListeners != null && mOnPullDeltaChangeListeners.size() != 0) {
                        for (OnPullDeltaChangeListener listener : mOnPullDeltaChangeListeners) {
                            listener.onPullDeltaChange(pullDownDis);
                        }
                    }

                    //When pull action not reach VIEW_STATE_REFRESHING,we don't care about touch event.let the parent to handle this
                    if (mRefreshHeaderView.getRefreshHeaderHeight() > 0
                        && mRefreshHeaderView.getCurrentState().ordinal() < IRefreshHeader.HeaderState.VIEW_STATE_REFRESHING.ordinal()) {
                        return false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastY = -1;
                if (mCanPullDownRefresh && !mIsLoadingMore) {
                    if (mRefreshHeaderView.onPullRelease()) {
                        mRefreshHeaderView.onRefreshing();
                        if (mOnRefreshListener != null) {
                            mOnRefreshListener.onRefreshing();
                        }
                    }
                }
                break;
        }

        return super.onTouchEvent(e);
    }

    /**
     * When RecyclerView scroll to bottom,
     * And load more data
     */
    private void detectScrollToBottom() {

        LayoutManager layoutManager = getLayoutManager();
        int lastVisibleItemPosition;
        if (layoutManager instanceof GridLayoutManager) {
            lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
            lastVisibleItemPosition = findMax(into);
        } else {
            lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        }

        if (layoutManager.getChildCount() > 0
            && lastVisibleItemPosition >= layoutManager.getItemCount() - 1
            && layoutManager.getItemCount() > layoutManager.getChildCount()) {

            if (mRefreshFooterView != null && !mIsNoMoreData) {
                mRefreshFooterView.updateState(IRefreshFooter.FooterState.VIEW_STATE_LOADING);
                mIsLoadingMore = true;
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onLoadMore();
                }
            }
        }
    }


    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == SCROLL_STATE_IDLE && mCanPullUpRefresh) {
            detectScrollToBottom();
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mAdapterWrapper = new AdapterWrapper(adapter);
        super.setAdapter(mAdapterWrapper);
        //Make sure the wrapper adapter can handle the same observer as adapter
        adapter.registerAdapterDataObserver(mAdapterDataObserver);
    }

    private RecyclerView.AdapterDataObserver mAdapterDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            //TODO handle empty or error page.
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            mAdapterWrapper.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
            mAdapterWrapper.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            mAdapterWrapper.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            mAdapterWrapper.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            mAdapterWrapper.notifyItemMoved(fromPosition, toPosition);
        }
    };

    private class AdapterWrapper extends RecyclerView.Adapter<ViewHolder> {

        private Adapter adapter;//normal item adapter

        public AdapterWrapper(@NonNull Adapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == VIEW_TYPE_HEADER) {
                return new ViewHolderImp(mRefreshHeaderView.getView());
            } else if (viewType == VIEW_TYPE_FOOTER) {
                return new ViewHolderImp(mRefreshFooterView.getView());
            } else if (isHeaderViewType(viewType)) {
                return new ViewHolderImp(getHeaderViewByType(viewType));
            }

            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (isNormalHeader(position) || isRefreshHeader(position) || isFooter(position))
                return;

            //TODO normal header may dynamic change the data
            //here we just handle the normal item.

            adapter.onBindViewHolder(holder, position - (mHeaderViews.size() + getRefreshHeaderCount()));
        }

        @Override
        public int getItemCount() {
            if (mCanPullUpRefresh) {
                return mHeaderViews.size() + adapter.getItemCount() + 1 + getRefreshHeaderCount();
            } else {
                return mHeaderViews.size() + adapter.getItemCount() + getRefreshHeaderCount();
            }
        }

        @Override
        public int getItemViewType(int position) {
            //refresh header
            if (isRefreshHeader(position)) {
                return VIEW_TYPE_HEADER;
            }

            //normal header
            if (isNormalHeader(position)) {
                return mHeaderTypes.get(position - getRefreshHeaderCount());
            }

            //footer view
            if (isFooter(position)) {
                return VIEW_TYPE_FOOTER;
            }

            //normal item
            int normalType = adapter.getItemViewType(getRealItemPosition(position));
            if (!checkItemViewTypeValid(normalType)) {
                return normalType;
            } else {
                throw new IllegalArgumentException("ExpandRecyclerView's itemViewType must less than 10000");
            }
        }

        @Override
        public long getItemId(int position) {
            if (isNormalHeader(position) || isRefreshHeader(position) || isFooter(position)) {
                return NO_ID;
            } else {
                return adapter.getItemId(mHeaderViews.size() + 1);
            }
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            //make sure header/footer's width is full-screen
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (isNormalHeader(position) || isFooter(position) || isRefreshHeader(position))
                            ? gridManager.getSpanCount() : 1;
                    }
                });
            }
            adapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            //make sure header/footer's width is full-screen
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null
                && lp instanceof StaggeredGridLayoutManager.LayoutParams
                && (isNormalHeader(holder.getLayoutPosition()) || isRefreshHeader(holder.getLayoutPosition()) || isFooter(holder.getLayoutPosition()))) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
            adapter.onViewAttachedToWindow(holder);
        }

        @Override
        public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
            adapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            adapter.onViewRecycled(holder);
        }

        @Override
        public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
            return adapter.onFailedToRecycleView(holder);
        }

        @Override
        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            adapter.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            adapter.registerAdapterDataObserver(observer);
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            adapter.onDetachedFromRecyclerView(recyclerView);
        }

        private class ViewHolderImp extends RecyclerView.ViewHolder {

            ViewHolderImp(View itemView) {
                super(itemView);
            }
        }

        private boolean isRefreshHeader(int position) {
            return position == 0 && mCanPullDownRefresh;
        }

        private boolean isNormalHeader(int position) {
            return position >= getRefreshHeaderCount() && position < mHeaderViews.size() + getRefreshHeaderCount();
        }

        private boolean isFooter(int position) {
            if (mCanPullUpRefresh) {
                return position == getItemCount() - 1;
            } else {
                return false;
            }
        }

        private int getRealItemPosition(int position) {
            return position - (mHeaderViews.size() + getRefreshHeaderCount());
        }

        private boolean checkItemViewTypeValid(int itemViewType) {
            if (itemViewType == VIEW_TYPE_HEADER || itemViewType == VIEW_TYPE_FOOTER || mHeaderTypes.contains(itemViewType)) {
                return true;
            } else {
                return false;
            }
        }

        private boolean isHeaderViewType(int type) {
            return mHeaderViews.size() > 0 && mHeaderTypes.contains(type);
        }

        private View getHeaderViewByType(int type) {
            return mHeaderViews.get(type);
        }

        private int getRefreshHeaderCount() {
            return mCanPullDownRefresh ? 1 : 0;
        }

    }

    /**
     * Get header count,besides refresh header(if have)
     * and normal headers
     * @return header counts
     */
    public int getAllHeaderCount() {
        return (mCanPullDownRefresh ? 1 : 0) + mHeaderViews.size();
    }

    /**
     * Update {@link ExpandRecyclerView}'s state to complete({@link IRefreshHeader.HeaderState#VIEW_STATE_REFRESHED})
     * and reset to {@link IRefreshHeader.HeaderState#VIEW_STATE_NORMAL}
     *
     * @param success refresh success or failed
     */
    public void setRefreshComplete(boolean success) {

        if (mRefreshHeaderView != null) {
            mRefreshHeaderView.updateState(IRefreshHeader.HeaderState.VIEW_STATE_REFRESHED);
            mRefreshHeaderView.onRefreshState(success);
        }

        //When pull down to refresh,we need reset IRefreshFooter's state
        if (mRefreshFooterView != null) {
            mIsNoMoreData = false;
            mIsLoadingMore = false;
            mRefreshFooterView.updateState(IRefreshFooter.FooterState.VIEW_STATE_COMPLETE);
        }
    }

    /**
     * Update {@link ExpandRecyclerView}'s state to ({@link IRefreshHeader.HeaderState#VIEW_STATE_REFRESHING})
     * Normally, this is used to auto refresh logic
     */
    public void setRefreshing() {
        if (mRefreshHeaderView != null) {
            mRefreshHeaderView.updateState(IRefreshHeader.HeaderState.VIEW_STATE_REFRESHING);
            if (mOnRefreshListener != null) {
                mOnRefreshListener.onRefreshing();
            }
        }
    }

    /**
     * Update {@link IRefreshFooter.FooterState} to {@link IRefreshFooter.FooterState#VIEW_STATE_COMPLETE}
     * Load More action is done
     */
    public void setLoadMoreComplete() {
        mIsLoadingMore = false;
        if (mRefreshFooterView != null) {
            mRefreshFooterView.updateState(IRefreshFooter.FooterState.VIEW_STATE_COMPLETE);
        }
    }

    /**
     * Update {@link IRefreshFooter.FooterState} to {@link IRefreshFooter.FooterState#VIEW_STATE_NO_MORE}
     * Load More action is done,and no more date exits
     */
    public void setLoadNoMore() {
        mIsNoMoreData = true;
        mIsLoadingMore = false;
        if (mRefreshFooterView != null) {
            mRefreshFooterView.updateState(IRefreshFooter.FooterState.VIEW_STATE_NO_MORE);
        }
    }

    /**
     * Add header view in {@link ExpandRecyclerView} automatically.
     *
     * @param view added header view
     */
    public void addHeaderView(View view) {
        int type = VIEW_TYPE_HEADER_START + mHeaderViews.size();
        mHeaderTypes.add(type);
        mHeaderViews.put(type, view);
        if (mAdapterWrapper != null) {
            mAdapterWrapper.notifyDataSetChanged();
        }
    }

    /**
     * Set listener to observe refresh state
     *
     * @param onRefreshListener OnRefreshListener
     */
    public void setRefreshListener(OnRefreshListener onRefreshListener) {
        this.mOnRefreshListener = onRefreshListener;
    }

    /**
     * Set listener to observe refresh distance change
     *
     * @param onPullDeltaChangeListener OnPullDeltaChangeListener
     */
    public void addPullDeltaListener(OnPullDeltaChangeListener onPullDeltaChangeListener) {
        if (mOnPullDeltaChangeListeners == null) {
            mOnPullDeltaChangeListeners = new ArrayList<>();
        }
        mOnPullDeltaChangeListeners.add(onPullDeltaChangeListener);
    }

    /**
     * Remove all listeners in {@link ExpandRecyclerView}
     */
    public void removeAllListeners() {
        this.mOnRefreshListener = null;
        this.mOnPullDeltaChangeListeners.clear();
    }


}
