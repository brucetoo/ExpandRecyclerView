package com.brucetoo.expandrecyclerview.lockscreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.brucetoo.expandrecyclerview.R;

/**
 * Created by Bruce Too
 * On 13/06/2017.
 * At 10:22
 */

public class LockScreenMainActivity extends FragmentActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_lock_screen_main);
    }

    public void onScreenClick(View view){
        startService(new Intent(this, LockScreenService.class));
    }
}
