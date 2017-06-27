package com.brucetoo.expandrecyclerview.intercept;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by Bruce Too
 * On 20/06/2017.
 * At 16:00
 */

public class NotificationBean implements Parcelable {

    private static final String TAG = "NotificationBean";

    public CharSequence titleBigText;
    public CharSequence titleText;
    public CharSequence messageBigText;
    public CharSequence messageText;
    public CharSequence[] messageTextLines;
    public CharSequence infoText;
    public CharSequence subText;
    public CharSequence summaryText;

    // Notification icon.
//    public Bitmap iconBitmap;
//    @DrawableRes
//    public int iconRes;
    public boolean isResident;
    public Drawable iconDrawable;
    public String packageName;
    public String finalTitle;
    public String finalDesc;
    public Intent originIntent;
    public String when;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public NotificationBean(@NonNull StatusBarNotification sbn) {
//        iconRes = sbn.getNotification().icon;
        packageName = sbn.getPackageName();
        isResident = sbn.isOngoing();
        originIntent = getIntentFromPending(sbn.getNotification().contentIntent);
    }

    public NotificationBean(String pkgName,String title,String desc){
        packageName = pkgName;
        finalTitle = title;
        finalDesc = desc;
    }

    private Intent getIntentFromPending(PendingIntent contentIntent) {
        Intent intent = null;
        try {
            Method getIntent = PendingIntent.class.getDeclaredMethod("getIntent");
            return (Intent) getIntent.invoke(contentIntent);
        } catch (Exception e) {
            Log.e(TAG, "NotificationBean getIntent error " + e.toString());
            return null;
        }
    }

    @Nullable
    private CharSequence ensureNotEmpty(@Nullable CharSequence cs) {
        return TextUtils.isEmpty(cs) ? null : cs;
    }

    @Override
    public String toString() {
        return TAG +
            " finalTitle = " + finalTitle +
            " finalDesc = " + finalDesc +
            " when = " + when +
//            " iconBitmap = " + iconBitmap +
            " messageTextLines = " + Arrays.toString(messageTextLines);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeParcelable(iconBitmap, flags);
//        dest.writeInt(iconRes);
        dest.writeByte((byte) (isResident ? 1 : 0));
        dest.writeString(packageName);
        dest.writeString(finalTitle);
        dest.writeString(finalDesc);
        dest.writeParcelable(originIntent, flags);
        dest.writeString(when);
    }


    protected NotificationBean(Parcel in) {
//        iconBitmap = in.readParcelable(Bitmap.class.getClassLoader());
//        iconRes = in.readInt();
        isResident = in.readByte() != 0;
        packageName = in.readString();
        finalTitle = in.readString();
        finalDesc = in.readString();
        originIntent = in.readParcelable(Intent.class.getClassLoader());
        when = in.readString();
    }

    public static final Creator<NotificationBean> CREATOR = new Creator<NotificationBean>() {
        @Override
        public NotificationBean createFromParcel(Parcel in) {
            return new NotificationBean(in);
        }

        @Override
        public NotificationBean[] newArray(int size) {
            return new NotificationBean[size];
        }
    };
}
