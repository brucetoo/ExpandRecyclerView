package com.brucetoo.expandrecyclerview.intercept;

import android.service.notification.StatusBarNotification;

import java.util.ArrayList;

/**
 * Created by Bruce Too
 * On 20/06/2017.
 * At 17:23
 */

public class NotificationListenerDispatcher implements NotificationListener {

    private static NotificationListenerDispatcher dispatcher;

    private NotificationListenerDispatcher() {
    }

    public static NotificationListenerDispatcher newInstance() {
        if (dispatcher == null) {
            return dispatcher = new NotificationListenerDispatcher();
        }
        return dispatcher;
    }

    private static ArrayList<NotificationListener> listeners = new ArrayList<>();

    public static void addNotificaitonListener(NotificationListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeNotificaitonListener(NotificationListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public static void removeAllNotificaitonListener() {
        listeners.clear();
    }


    @Override
    public void onNotificationPosted(NotificationInterceptService service, StatusBarNotification sbn) {
        for (NotificationListener listener : listeners) {
            listener.onNotificationPosted(service,sbn);
        }
    }

    @Override
    public void onListenerConnected() {
        for (NotificationListener listener : listeners) {
            listener.onListenerConnected();
        }
    }

    @Override
    public void onNotificationRemoved(NotificationInterceptService service, StatusBarNotification sbn) {
        for (NotificationListener listener : listeners) {
            listener.onNotificationRemoved(service,sbn);
        }
    }
}
