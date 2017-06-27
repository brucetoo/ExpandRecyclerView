package com.brucetoo.expandrecyclerview.intercept;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.brucetoo.expandrecyclerview.intercept.InterceptActivity.SWITCH_ON;

/**
 * Created by Bruce Too
 * On 19/06/2017.
 * At 10:05
 */

@SuppressLint("OverrideAbstract")
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationInterceptService extends NotificationListenerService {

    public static final String TAG = NotificationInterceptService.class.getSimpleName();

    public static NotificationInterceptService sService;
    private NotificationListenerDispatcher dispatcher = NotificationListenerDispatcher.newInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        //foreground keep alive
        try {
            startForeground(250, new Notification());
        } catch (Exception e) {
        }
        Log.i(TAG, "onCreate: NotificationInterceptService on");
        sService = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sService = null;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.i(TAG, "onListenerConnected");
        dispatcher.onListenerConnected();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        dispatcher.onNotificationPosted(this, sbn);
        String notificationPkg = sbn.getPackageName();

        if (sbn.isOngoing()) return;

        if (SWITCH_ON) {
            NotificationUtils.dismissNotification(sbn);
            NotificationBean sb = new NotificationBean(sbn);
            //ignore system
            if (!NotificationUtils.isSystemApplication(this, notificationPkg)) {
                NotificationManager.add(sb, sbn);
                Log.i(TAG, "onNotificationPosted: pkg -> " + notificationPkg + " is system ?" + NotificationUtils.isSystemApplication(this, notificationPkg) + " --- " + sb.toString()
                    + " when:" + getDateString(System.currentTimeMillis()));
            }
        }
    }

    //for test
    public static String getDateString(long when) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(new Date(when));
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        dispatcher.onNotificationRemoved(this, sbn);
        Log.i(TAG, "onNotificationRemoved: pkg -> " + sbn.getPackageName());
    }

}
