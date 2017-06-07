package com.brucetoo.expandrecyclerview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.brucetoo.expandrecyclerview.animator.ViewAnimator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruce Too
 * On 12/01/2017.
 * At 14:12
 */

public class ScrollActivity extends FragmentActivity implements AbsListView.OnScrollListener {

    private View mIcon;
    private TextView mTitle;
    private Button mBtn;
    private ListView mListView;
    private ScrollableLayout mScrollLayout;
    private View mSpaceTitle;
    private View mSpaceBtn;
    private View mBack;
    private View mDesc;
    private View mBg;
    private int[] titleDelta = new int[2];
    private int[] btnDelta = new int[2];

    int[] topTitlePos = new int[2];
    int[] titlePos = new int[2];
    int[] topBtnPos = new int[2];
    int[] btnPos = new int[2];


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_scroll);

        mIcon = findViewById(R.id.img_icon);
        mTitle = (TextView) findViewById(R.id.txt_title);
        mBtn = (Button) findViewById(R.id.btn_click);
        mListView = (ListView) findViewById(R.id.list_view);
        mScrollLayout = (ScrollableLayout) findViewById(R.id.scroll_layout);
        mSpaceTitle = findViewById(R.id.space_title);
        mSpaceBtn = findViewById(R.id.space_btn);
        mBack = findViewById(R.id.img_back);
        mDesc = findViewById(R.id.txt_desc);
        mBg = findViewById(R.id.img_bg);

        final List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("String " + (i + 1));
        }

        mListView.addHeaderView(LayoutInflater.from(this).inflate(R.layout.list_header,null));
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, list);
        mListView.setAdapter(adapter);
        mListView.setOnScrollListener(this);

        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.addAll(list);
            }
        },4000);

        mScrollLayout.setScrollView(mListView);
        mScrollLayout.setOnScrollListener(new ScrollableLayout.OnScrollListener() {
            @Override
            public void onScroll(int currentY, int maxScrollY) {
                float percent = 1 - currentY * 1.0f / maxScrollY;

                ViewAnimator.putOn(mIcon).alpha(percent)
                    .andPutOn(mDesc).alpha(percent)
                    .andPutOn(mTitle).translationX(-titleDelta[0] * (1 - Math.max(0, percent)))
                    .pivotX(0).pivotY(0)
                    .scale(percent < 0.75f ? 0.75f : percent)
                    .andPutOn(mBack).translationY(currentY);

                if (currentY >= titleDelta[1]) {
                    ViewAnimator.putOn(mTitle).translationY(currentY - titleDelta[1]);
                }
//
                if (currentY >= btnDelta[1]) {
                    ViewAnimator.putOn(mBtn).translationY(currentY - btnDelta[1]);
                }
//
                ViewAnimator.putOn(mBtn).pivotX(0).pivotY(0).translationX(btnDelta[0] * (1 - percent))
                    .scale(percent < 0.75f ? 0.75f : percent);

                Log.e("ScrollActivity", "onScroll: currentY -> " + currentY + " titleDelta_x -> " + titleDelta[0]
                    + " titleDelta_y -> " + titleDelta[1] + " percent -> " + percent + " maxScrollY -> " + maxScrollY);
            }

            @Override
            public void onScrollExpand(View headerView,float percent) {
//                headerView.getLayoutParams().height = (int) (headerView.getLayoutParams().height * percent);
//                headerView.requestLayout();
            }
        });

        mTitle.post(new Runnable() {
            @Override
            public void run() {

                mSpaceTitle.getLocationOnScreen(topTitlePos);
                mTitle.getLocationOnScreen(titlePos);
                titleDelta = MoveHelper.moveViewRight2Left(mTitle,mBack,30,0.75f);

                mSpaceBtn.getLocationOnScreen(topBtnPos);
                mBtn.getLocationOnScreen(btnPos);

                btnDelta = MoveHelper.moveViewLeft2Right(mBtn,mSpaceBtn,0,0.75f);

                titleW = mTitle.getMeasuredWidth();
                titleH = mTitle.getMeasuredHeight();

                Log.e("ScrollActivity", "run: titleDelta[0] -> " + titleDelta[0] + "   titleDelta[1] -> " + titleDelta[1]);
                Log.e("ScrollActivity", "run: titlePos[0] -> " + titlePos[0] + "   topTitlePos[0] -> " + topTitlePos[0]);

                Log.e("ScrollActivity", "run: btnDelta[0] -> " + btnDelta[0] + "   btnDelta[1] -> " + btnDelta[1]);
            }
        });

    }

    private int titleW;
    private int titleH;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }
}
