package com.brucetoo.expandrecyclerview.intercept;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.brucetoo.expandrecyclerview.R;

/**
 * Created by Bruce Too
 * On 20/06/2017.
 * At 14:10
 */

public class GuideActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_guide);
        findViewById(R.id.txt_guide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
