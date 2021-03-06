package com.brucetoo.expandrecyclerview.intercept;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.brucetoo.expandrecyclerview.DemoApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Bruce Too
 * On 23/06/2017.
 * At 16:05
 */

public class NotificationManager {

    private static final String TAG = "NotificationManager";
    private static final File NOTIFICATION;
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    static {
        NOTIFICATION = new File(getContext().getExternalCacheDir(), "notification.ini");
    }

    private static final ExecutorService sThreadPool = Executors.newCachedThreadPool();
    private static ArrayList<NotificationBean> sNotification = new ArrayList<>();

    private static Context getContext() {
        return DemoApplication.sContext;
    }

    private static File ensureCreated(File folder) {
        if (!folder.exists()) {
            Log.w(TAG, "Unable to create the file:" + folder.getPath());
        }
        return folder;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void add(final NotificationBean notification, StatusBarNotification sbn) {
        Extractor.get().startExtractor(getContext(), notification, sbn.getNotification());
        notification.when = getDateString(System.currentTimeMillis());
        if(!TextUtils.isEmpty(notification.finalTitle) && !TextUtils.isEmpty(notification.finalDesc)) {
            sNotification.add(notification);
        }
    }

    public static void addSms(String from, String body) {
        NotificationBean bean = new NotificationBean(NotificationUtils.getDefaultSmsApp(getContext()),
            NotificationUtils.getNameByPhoneNumber(getContext(),from), body);
        bean.when = getDateString(System.currentTimeMillis());
        sNotification.add(bean);
    }

    public static void addPhone(String number) {
        NotificationBean bean = new NotificationBean(NotificationUtils.getDefaultDialerApp(getContext()), NotificationUtils.getNameByPhoneNumber(getContext(),number), number);
        bean.when = getDateString(System.currentTimeMillis());
        sNotification.add(bean);
    }

    public static void clearAll() {
        sThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintWriter writer = new PrintWriter(NOTIFICATION);
                    writer.print("");
                    writer.close();
                    Log.i(TAG, "run: clear all notification");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void save() {
        //TODO 字段增加可能导致读取为null~NotificationBean 增加版本判断
        long lastModified = NOTIFICATION.lastModified();
        long current = System.currentTimeMillis();
        if (current - lastModified >= 24 * 60 * 60 * 1000) {
            clearAll();
        }
        sThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Parcel p = Parcel.obtain();
                int size = sNotification.size();
                try {
                    ArrayList<NotificationBean> beansInFile = getBeansInFile();
                    size = size + beansInFile.size();
                    Log.e(TAG, "run: write notification size " + size);
                    p.writeInt(size);
                    beansInFile.addAll(sNotification);
                    while (size-- > 0) {
                        p.writeParcelable(beansInFile.get(size), 0);
                    }
                    FileOutputStream out = new FileOutputStream(NOTIFICATION);
                    out.write(p.marshall());
                    out.close();
                } catch (Exception e) {
                    Log.e(TAG, "run: Error -> " + e.toString());
                } finally {
                    p.recycle();
                    sNotification.clear();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void read(final FetchAllListener listener) {
        read(listener, false);
    }

    /**
     * Read history notification with listener and sort options
     *
     * @param listener  call back when fetch success(UI Thread)
     * @param increment sort by increment or not,default is decrement
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void read(final FetchAllListener listener, final boolean increment) {
        sThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<NotificationBean> beans = getBeansInFile();
                handleParamsAndSort(beans, increment);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFetchDone(beans, filterNotificationsByPkg(beans));
                    }
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void readByPackage(final String packageName, final FetchPackageListener listener) {
        sThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<NotificationBean> beans = getBeansInFile();
                handleParamsAndSort(beans, false);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onPackageDone(packageName, (ArrayList<NotificationBean>) filterNotificationsByPkg(beans).get(packageName));
                    }
                });
            }
        });
    }

    private static void handleParamsAndSort(ArrayList<NotificationBean> beans, final boolean increment) {
        for (NotificationBean bean : beans) {
            bean.iconDrawable = NotificationUtils.getDrawable(getContext(), bean.packageName);
        }
        Collections.sort(beans, new Comparator<NotificationBean>() {
            @Override
            public int compare(NotificationBean lhs, NotificationBean rhs) {
                return getDateLong(lhs.when) < getDateLong(rhs.when) && !increment ? 1 : -1;
            }
        });
    }

    @NonNull
    private static ArrayList<NotificationBean> getBeansInFile() {
        Parcel p = Parcel.obtain();
        final ArrayList<NotificationBean> beans = new ArrayList<>();
        try {
            FileInputStream in = new FileInputStream(NOTIFICATION);
            byte[] bytes = new byte[(int) NOTIFICATION.length()];
            int len = in.read(bytes);
            in.close();
            if (len != bytes.length) {
                throw new IOException("Unable to read NOTIFICATION file.");
            }

            p.unmarshall(bytes, 0, bytes.length);
            p.setDataPosition(0);
            int size = p.readInt();
            Log.e(TAG, "run: read notification size " + size);
            while (size-- > 0) {
                beans.add((NotificationBean) p.readParcelable(NotificationBean.class.getClassLoader()));
            }
        } catch (Exception e) {
        } finally {
            p.recycle();
        }
        return beans;
    }

    public static HashMap<String, List<NotificationBean>> filterNotificationsByPkg(ArrayList<NotificationBean> beans) {
        HashMap<String, List<NotificationBean>> maps = new HashMap<>();
        for (NotificationBean bean : beans) {
            String key = bean.packageName;
            if (maps.containsKey(key)) {
                maps.get(key).add(bean);
            } else {
                ArrayList<NotificationBean> list = new ArrayList<>();
                list.add(bean);
                maps.put(key, list);
            }
        }
        return maps;
    }

    private static String getDateString(long when) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(new Date(when));
    }

    private static long getDateLong(String date) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date d = f.parse(date);
            return d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public interface FetchAllListener {
        void onFetchDone(ArrayList<NotificationBean> beans, HashMap<String, List<NotificationBean>> maps);
    }

    public interface FetchPackageListener {
        void onPackageDone(String pkgName, ArrayList<NotificationBean> beans);
    }
}
