package com.brucetoo.expandrecyclerview.intercept;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.Settings;
import android.provider.Telephony;
import android.service.notification.StatusBarNotification;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Bruce Too
 * On 22/06/2017.
 * At 19:34
 */

public class NotificationUtils {

    private static final String TAG = NotificationUtils.class.getSimpleName();


    public static void startOriginIntent(Context context, NotificationBean bean) {
        Context inner = NotificationUtils.createContext(context, bean.packageName);
        //TODO what if intent is not activity ???
        if (inner != null) {
            if (bean.originIntent != null) {
                context.startActivity(bean.originIntent);
            } else {
                context.startActivity(inner.getPackageManager().getLaunchIntentForPackage(bean.packageName));
            }
        }
    }

    /**
     * Check if the Notification permission is granted
     */
    public static boolean isNotificationListenEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(),
            "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean sendPendingIntent(@Nullable PendingIntent pi) {
        return sendPendingIntent(pi, null, null);
    }

    public static boolean sendPendingIntent(@Nullable PendingIntent pi, Context context, Intent intent) {
        if (pi != null)
            try {
                if (intent == null) {
                    throw new IllegalArgumentException("intent must not be null!");
                }
                pi.send(context, 0, intent);
                return true;
            } catch (PendingIntent.CanceledException e) {
            }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void dismissNotification(StatusBarNotification sbn) {
        if (sbn != null) {
            NotificationInterceptService service = NotificationInterceptService.sService;
            if (service != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        service.cancelNotification(sbn.getKey());
                    } else {
                        service.cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "dismissNotification: exception happened -> " + e.toString());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Create context of a specific package from Notification
     */
    public static Context createContext(@NonNull Context context, @NonNull NotificationBean n) {
        try {
            return context.createPackageContext(n.packageName, Context.CONTEXT_RESTRICTED);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Failed to create notification\'s context");
            return null;
        }
    }

    /**
     * Create context of a specific package
     */
    public static Context createContext(@NonNull Context context, @NonNull String packageName) {
        try {
            return context.createPackageContext(packageName, Context.CONTEXT_RESTRICTED);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Failed to create notification\'s context");
            return null;
        }
    }

    public static String getAppLabelByPackageName(@NonNull Context context, @NonNull String packageName) {
        Context inner = createContext(context, packageName);
        if (inner != null) {
            int labelRes = inner.getApplicationInfo().labelRes;
            return labelRes == 0 ? inner.getApplicationInfo().nonLocalizedLabel.toString() : inner.getString(labelRes);
        }
        return packageName;
    }

    /**
     * get drawable from notification
     */
    public static Drawable getDrawable(Context context, NotificationBean n,
                                       @DrawableRes int iconRes) {
        Context pkgContext = createContext(context, n);
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null)
                packageManager.getPackageInfo(n.packageName, 0).applicationInfo.loadIcon(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (pkgContext != null)
            try {
                return getDrawable(pkgContext, iconRes);
            } catch (Resources.NotFoundException nfe) { /* unused */ }
        return null;
    }

    public static Drawable getDrawable(@NonNull Context context, @DrawableRes int drawableRes) {
        return Build.VERSION.SDK_INT >= 21
            ? context.getResources().getDrawable(drawableRes, context.getTheme())
            : context.getResources().getDrawable(drawableRes);
    }

    /**
     * get drawable from package
     */
    public static Drawable getDrawable(@NonNull Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getPackageInfo(packageName, 0).applicationInfo.loadIcon(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return createEmptyIcon(context);
        }
    }

    public static int convertDip2Px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static Drawable createEmptyIcon(@NonNull Context context) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0xDDCCCCCC); // white gray

        int size = convertDip2Px(context, 24);
        final float radius = size / 2f;

        Bitmap icon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(icon);
        canvas.drawCircle(radius, radius, radius, paint);
        return new BitmapDrawable(icon);
    }

    public static void makeCallDirectly(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }

    public static void navigateToCallLog(Context context) {
        //intent of call log
        Intent callLogIntent = new Intent(Intent.ACTION_VIEW, CallLog.Calls.CONTENT_URI);
        context.startActivity(callLogIntent);
    }

    public static void navigateToSms(Context context, String phoneNumber) {
//        Intent sendIntent = new Intent();
//        sendIntent.setPackage(Telephony.Sms.getDefaultSmsPackage(context));

        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:" + phoneNumber));
        context.startActivity(sendIntent);
    }

    public static boolean isSystemApplication(Context context, String packageName) {
        try {
            return (context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getDefaultSmsApp(Context context) {
        String defaultAppName;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            defaultAppName = Telephony.Sms.getDefaultSmsPackage(context);
        } else {
            String defApp = Settings.Secure.getString(context.getContentResolver(), "sms_default_application");
            PackageManager pm = context.getApplicationContext().getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(defApp);
            ResolveInfo info = pm.resolveActivity(intent, 0);
            defaultAppName = info.activityInfo.packageName;
        }
        Log.i(TAG, "getDefaultSmsApp " + defaultAppName);
        return defaultAppName;
    }

    public static String getDefaultDialerApp(Context context) {
        Intent callLogIntent = new Intent(Intent.ACTION_VIEW, CallLog.Calls.CONTENT_URI);
        PackageManager pm = context.getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(callLogIntent, 0);
        if (resolveInfo != null) {
//            resolveInfo.isDefault
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null) {
                Log.i(TAG, "getDefaultDialerApp: " + activityInfo.packageName);
//                if ("android".equals(activityInfo.packageName)) {
//                    List<ResolveInfo> resolveInfos = pm.queryIntentActivities(callLogIntent, 0);
//
//                } else {
//                    ApplicationInfo appInfo = activityInfo.applicationInfo;
//                    if (appInfo != null) {
//                        appInfo.packageName;
//                    }
//                }
                return activityInfo.packageName;
            }
        }
        return "";
    }
}

