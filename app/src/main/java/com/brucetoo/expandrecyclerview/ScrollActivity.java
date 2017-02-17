package com.brucetoo.expandrecyclerview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class ScrollActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private View mTopLayout;
    private View mIcon;
    private TextView mTitle;
    private Button mBtn;
    private ListView mListView;
    private ScrollableLayout mScrollLayout;
    private View mTopTitle;
    private View mTopBtn;
    private View mDesc;
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

        mTopLayout = findViewById(R.id.layout_top_bg);
        mIcon = findViewById(R.id.img_icon);
        mTitle = (TextView) findViewById(R.id.txt_title);
        mBtn = (Button) findViewById(R.id.btn_click);
        mListView = (ListView) findViewById(R.id.list_view);
        mScrollLayout = (ScrollableLayout) findViewById(R.id.scroll_layout);
        mTopTitle = findViewById(R.id.txt_title_top);
        mTopBtn = findViewById(R.id.btn_click_top);
        mDesc = findViewById(R.id.txt_desc);

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("String " + (i + 1));
        }

        mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, list));
        mListView.setOnScrollListener(this);

        mScrollLayout.setScrollView(mListView);
        mScrollLayout.setOnScrollListener(new ScrollableLayout.OnScrollListener() {
            @Override
            public void onScroll(int currentY, int maxY) {
                float percent = 1 - currentY * 1.0f / (maxY - dp2px(50));
                ViewAnimator.putOn(mIcon).alpha(percent)
                    .andPutOn(mDesc).alpha(percent)
                    .andPutOn(mTitle).translationX(-titleDelta[0] * (1 - Math.max(0, percent)));

                if (currentY >= titleDelta[1]) {
                    ViewAnimator.putOn(mTitle).translationY(currentY - titleDelta[1]);
                }
                int[] loc1 = new int[2];
                mTitle.getLocationOnScreen(loc1);
                if(titlePos[1] <= loc1[1]){
                    ViewAnimator.putOn(mTitle).translationY(0);
                }


                if (percent <= 0) {
                    ViewAnimator.putOn(mTopTitle).visible()
                        .andPutOn(mTitle).invisible()
                        .andPutOn(mTopLayout).visible()
                        .andPutOn(mBtn).invisible()
                        .andPutOn(mTopBtn).visible();
                } else {
                    ViewAnimator.putOn(mTopTitle).invisible()
                        .andPutOn(mTitle).visible()
                        .andPutOn(mTopLayout).invisible()
                        .andPutOn(mBtn).visible()
                        .andPutOn(mTopBtn).invisible();
                }

                //textSize 25 -> 25*0.5f
                mTitle.setTextSize(25 * (percent < 0.5f ? 0.5f : percent));


                if (currentY >= btnDelta[1]) {
                    ViewAnimator.putOn(mBtn).translationY(currentY - btnDelta[1]);
                }
                int[] loc2 = new int[2];
                mBtn.getLocationOnScreen(loc2);
                if(btnPos[1] <= loc2[1]){
                    ViewAnimator.putOn(mBtn).translationY(0);
                }

                ViewAnimator.putOn(mBtn).pivotX(0).pivotY(0).translationX(btnDelta[0] * (1 - percent))
                    .scale(percent < 0.75f ? 0.75f : percent);

                Log.e("ScrollActivity", "onScroll: currentY -> " + currentY + " titleDelta_x -> " + titleDelta[0]
                    + " titleDelta_y -> " + titleDelta[1] + " percent -> " + percent + " maxY -> " + maxY);
            }
        });

        mTitle.post(new Runnable() {
            @Override
            public void run() {

                mTopTitle.getLocationOnScreen(topTitlePos);
                mTitle.getLocationOnScreen(titlePos);
                titleDelta[0] = titlePos[0] - topTitlePos[0];
                titleDelta[1] = titlePos[1] - topTitlePos[1];

                mTopBtn.getLocationOnScreen(topBtnPos);
                mBtn.getLocationOnScreen(btnPos);
                btnDelta[0] = topBtnPos[0] - btnPos[0];
                btnDelta[1] = btnPos[1] - topBtnPos[1];

                Log.e("ScrollActivity", "run: titleDelta[0] -> " + titleDelta[0] + "   titleDelta[1] -> " + titleDelta[1]);
                Log.e("ScrollActivity", "run: titlePos[0] -> " + titlePos[0] + "   topTitlePos[0] -> " + topTitlePos[0]);

                Log.e("ScrollActivity", "run: btnDelta[0] -> " + btnDelta[0] + "   btnDelta[1] -> " + btnDelta[1]);
            }
        });

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    private int dp2px(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp);
    }
}
