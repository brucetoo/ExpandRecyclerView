package com.brucetoo.expandrecyclerview.intercept;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.brucetoo.expandrecyclerview.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bruce Too
 * On 19/06/2017.
 * At 10:27
 */

public class InterceptActivity extends FragmentActivity {

    public static boolean SWITCH_ON = true;

    private ImageView icon;
    private TextView desc;

    private RecyclerView recyclerView;
    RecyclerAdapter2 adapter2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_intercept);
        ((ToggleButton) findViewById(R.id.btn_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SWITCH_ON = true;
                } else {
                    SWITCH_ON = false;
                }
                rebindNotificationListenerService();
            }
        });

        icon = (ImageView) findViewById(R.id.img_icon);
        desc = (TextView) findViewById(R.id.txt_desc);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            NotificationManager.read(new NotificationManager.FetchAllListener() {
                @Override
                public void onFetchDone(ArrayList<NotificationBean> beans, HashMap<String, List<NotificationBean>> maps) {
                    if (beans != null && beans.size() != 0) {
                        adapter2 = new RecyclerAdapter2(beans, InterceptActivity.this);
                        recyclerView.setAdapter(adapter2);
                    }
                }
            });
        }

        interceptNotification();
    }

    public void onCall(View view) {
//        NotificationUtils.makeCallDirectly(this,"18666009051");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            NotificationManager.save();
        }
    }

    public void onSms(View view) {
//        NotificationUtils.navigateToSms(this,"18666009051");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            NotificationManager.read(new NotificationManager.FetchAllListener() {
                @Override
                public void onFetchDone(ArrayList<NotificationBean> beans, HashMap<String, List<NotificationBean>> maps) {
                    final NotificationBean nb = beans.get(0);
                    icon.setImageDrawable(NotificationUtils.getDrawable(getApplicationContext(), nb.packageName));
                    desc.setText(nb.finalTitle + " -> " + nb.finalDesc);
                    desc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NotificationUtils.startOriginIntent(InterceptActivity.this, nb);
                        }
                    });
                }
            });
        }
    }

    public void onClickGuide(View view) {
        NotificationManager.clearAll();

        //Test permission request
//        if (Build.VERSION.SDK_INT >= 18) {
//            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
//            view.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    startActivity(new Intent(InterceptActivity.this, GuideActivity.class));
//                }
//            }, 500);
//        }
    }

    private void rebindNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationInterceptService.class),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationInterceptService.class),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }


    private void interceptNotification() {
        if (!NotificationUtils.isNotificationListenEnabled(getApplicationContext())) {
            if (Build.VERSION.SDK_INT >= 18) {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }
        } else {
            Intent service = new Intent(this, NotificationInterceptService.class);
            startService(service);
        }
    }
}
