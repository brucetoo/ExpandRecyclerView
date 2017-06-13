package com.brucetoo.expandrecyclerview.lockscreen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.brucetoo.expandrecyclerview.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruce Too
 * On 13/06/2017.
 * At 11:53
 */

public class LockLauncherActivity extends FragmentActivity {

    public static final String TAG = "LockScreen";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_lock_screen_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, LockScreenService.class);
        startService(intent);

        getLauncherPackageName(this);

        if (!LockScreenActivity.isLocked) {
            Log.e(TAG, "onResume LockScreenActivity.isLocked -> " + LockScreenActivity.isLocked );
            Intent systemIntent = new Intent();
            systemIntent.setComponent(new ComponentName(mPackageName, mClassName));
            startActivity(systemIntent);
        }

        finish();
    }


    private String mPackageName;
    private String mClassName;

    private void getLauncherPackageName(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
        if (resInfoList != null) {
            ResolveInfo resInfo;
            for (int i = 0; i < resInfoList.size(); i++) {
                resInfo = resInfoList.get(i);
                if ((resInfo.activityInfo.applicationInfo.flags &
                    ApplicationInfo.FLAG_SYSTEM) > 0) {
                    mPackageName = resInfo.activityInfo.packageName;
                    mClassName = resInfo.activityInfo.name;
                    Log.e(TAG, "getLauncherPackageName mPackageName:" + mPackageName + " mClassName:" + mClassName );
                    break;
                }
            }
        }
    }
}
