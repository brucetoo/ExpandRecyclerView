package com.brucetoo.expandrecyclerview.lockscreen;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.brucetoo.expandrecyclerview.R;

/**
 * Created by Bruce Too
 * On 13/06/2017.
 * At 11:51
 */

public class LockScreenActivity extends FragmentActivity {


    public static boolean isLocked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        setContentView(R.layout.layout_lock_screen);

        handleHideNavigateBar();

        isLocked = true;

        findViewById(R.id.dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLocked = false;
                Toast.makeText(LockScreenActivity.this, "Screen is unlocked", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        SwipeableLayout swipeableLayout = (SwipeableLayout) findViewById(R.id.swipe_view);
        swipeableLayout.setDragOptions(true,true,true,true);
        swipeableLayout.addDragListener(new SwipeableLayout.DragListener() {
            @Override
            public void onDragRatioChange(float ratio, View dragView, float leftDelta, float topDelta, boolean horizontal) {

            }

            @Override
            public void onRelease2EdgeEnd(boolean horizontal) {
                Log.e("lock_screen", "onRelease2EdgeEnd");
                LockScreenActivity.this.finish();
            }

            @Override
            public void onDragStateChanged(int state) {

            }
        });

        try {
            startService(new Intent(this, LockScreenService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Only system app can be granted android.permission.STATUS_BAR
//        int STATUS_BAR_DISABLE_HOME = Reflecter.on(View.class).get("STATUS_BAR_DISABLE_HOME");
//        int STATUS_BAR_DISABLE_RECENT = Reflecter.on(View.class).get("STATUS_BAR_DISABLE_RECENT");
//        Reflecter.on("android.app.StatusBarManager").create(this).call("disable", STATUS_BAR_DISABLE_HOME | STATUS_BAR_DISABLE_RECENT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        public static final int DISABLE_NONE = 0x00000000;
//        Reflecter.on("android.app.StatusBarManager").create(this).call("disable", 0x00000000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int key = event.getKeyCode();
        switch (key) {
            case KeyEvent.KEYCODE_BACK: { //back
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void handleHideNavigateBar() {

        // This work only for android 4.4+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//            | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            getWindow().getDecorView().setSystemUiVisibility(flags);

            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });
        }
    }


}
