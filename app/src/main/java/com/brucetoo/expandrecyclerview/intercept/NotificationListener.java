package com.brucetoo.expandrecyclerview.intercept;

import android.service.notification.StatusBarNotification;

/**
 * Created by Bruce Too
 * On 20/06/2017.
 * At 18:02
 */

public interface NotificationListener {
    void onNotificationPosted(NotificationInterceptService service,StatusBarNotification sbn);
    void onListenerConnected();
    void onNotificationRemoved(NotificationInterceptService service,StatusBarNotification sbn);
}
