package com.brucetoo.expandrecyclerview;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brucetoo.expandrecyclerview.reyclerview.DividerItemDecoration;
import com.brucetoo.expandrecyclerview.reyclerview.ExpandRecyclerView;
import com.brucetoo.expandrecyclerview.reyclerview.RecyclerAdapter;

import java.util.ArrayList;

public class RecyclerViewActivity extends AppCompatActivity {

    private ExpandRecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private ArrayList<String> mListDatas;
    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerview);

        mRecyclerView = (ExpandRecyclerView) this.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        View header1 = LayoutInflater.from(this).inflate(R.layout.recyclerview_header, (ViewGroup) findViewById(android.R.id.content), false);
        mRecyclerView.addHeaderView(header1);

        View header2 = LayoutInflater.from(this).inflate(R.layout.recyclerview_header1, (ViewGroup) findViewById(android.R.id.content), false);

        mRecyclerView.addHeaderView(header2);

        mRecyclerView.addPullDeltaListener(new ExpandRecyclerView.OnPullDeltaChangeListener() {
            @Override
            public void onPullDeltaChange(float delta) {
//                Log.e("RefreshHeaderView", "onPullDeltaChange: " + delta);
            }
        });

        new ExpandRecyclerView.Builder()
            .setCanPullDownRefresh(true)
            .setCanPullUpRefresh(true)
            .setPullToRefreshText("Pull To Refresh 1")
            .setReleaseToRefreshText("Release To Refresh 1")
            .setRefreshingText("Refreshing 1")
            .setRefreshedSuccessText("Refresh Success 1")
            .setRefreshedFailedText("Refresh Failed 1")
            .setLoadMoreText("Loading...1")
            .setLoadNoMoreText("No More Data 1")
            .build(mRecyclerView);

        mRecyclerView.setRefreshListener(new ExpandRecyclerView.OnRefreshListener() {

            @Override
            public void onRefreshing() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListDatas.clear();
                        for (int i = 0; i < 15; i++) {
                            mListDatas.add("newItem" + i);
                        }
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.setRefreshComplete(false);
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int preIndex = mListDatas.size();
                        //No more date test
                        if (mListDatas.size() >= 20) {
                            mRecyclerView.setLoadNoMore();
                            return;
                        }

                        for (int i = 0; i < 5; i++) {
                            mListDatas.add("LoadMore item" + i);
                        }

                        //load more test
                        mAdapter.notifyItemRangeInserted(preIndex, 5);
                        mRecyclerView.setLoadMoreComplete();

                    }
                }, 2000);
            }
        });

        mListDatas = new ArrayList<String>();
        for (int i = 0; i < 15; i++) {
            mListDatas.add("Item " + i);
        }

        mAdapter = new RecyclerAdapter(mListDatas);

        Drawable divider = ContextCompat.getDrawable(this, R.drawable.shape_divider);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(divider,mRecyclerView.getAllHeaderCount()));

        //TODO add item animator
        //https://github.com/wasabeef/recyclerview-animators

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setRefreshing();
    }
}
