package com.brucetoo.expandrecyclerview.intercept;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Bruce Too
 * On 22/06/2017.
 * At 20:19
 */

@Deprecated
public class IconFactory {

    private static final String TAG = "IconFactory";
    private final HashMap<String, WeakReference<Bitmap>> ICON_CACHES = new HashMap<>();

    private IconFactory() {
    }

    private static final IconFactory sFactory = new IconFactory();

    public static IconFactory get() {
        return sFactory;
    }

    public interface IconGenerateListener {
        void onGenerated(Bitmap bitmap);
    }

    private final Object mLock = new Object();
    private Worker mWorker;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public void add(Context context, IconGenerateListener listener, NotificationBean notification) {
        synchronized (mLock) {
            boolean alive = isWorkAlive();
            if (!alive) {
                mWorker = new Worker(this, mLock);
                mWorker.setPriority(Thread.MAX_PRIORITY);
                mWorker.start();
            }
            mWorker.add(context, listener, notification);
        }
    }

    public void remove(NotificationBean notification) {
        synchronized (mLock) {
            if (isWorkAlive()) {
                mWorker.remove(notification);
            }
        }
    }

    public Bitmap generateBitmap(final Context context, final NotificationBean notification) {

//        final int iconRes = notification.iconRes;
//        String key = notification.packageName;
//
//        WeakReference<Bitmap> bitmapWeakReference = ICON_CACHES.get(key);
//        Bitmap bitmap;
//        if (bitmapWeakReference != null) {
//            bitmap = bitmapWeakReference.get();
//            if (bitmap != null) {
//                Log.d(TAG, "Got the icon of notification from cache: key=" + key);
//                return bitmap;
//            }
//        }
//        Drawable drawable = NotificationUtils.getDrawable(context, notification, iconRes);
//        final int size = convertDip2Px(context, 24);//notification_icon_size
//        if (drawable != null) {
//            bitmap = createIcon(drawable, size);
//            ICON_CACHES.put(key, new WeakReference<Bitmap>(bitmap));
//            Log.d(TAG, "Put the icon of notification to cache: key=" + key);
//        } else {
//            bitmap = createEmptyIcon(context, size);
//        }
//        return bitmap;

        return null;
    }

    public static int convertDip2Px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static Bitmap createIcon(@NonNull Drawable drawable, int size) {
        Bitmap icon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(icon);

        // Calculate scale ratios
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        float ratioX = Math.min((float) drawableWidth / drawableHeight, 1f);
        float ratioY = Math.min((float) drawableHeight / drawableWidth, 1f);

        // Calculate new width and height
        int width = Math.round(size * ratioX);
        int height = Math.round(size * ratioY);
        int paddingLeft = (size - width) / 2;
        int paddingTop = (size - height) / 2;

        // Apply size and draw
        canvas.translate(paddingLeft, paddingTop);
        drawable = drawable.mutate();
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return icon;
    }

    private static Bitmap createEmptyIcon(@NonNull Context context, int size) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0xDDCCCCCC); // white gray

        final float radius = size / 2f;

        Bitmap icon = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(icon);
        canvas.drawCircle(radius, radius, radius, paint);

        //draw a icon
//        Drawable drawable = NotificationUtils.getDrawable(context, android.R.drawable.presence_away);
//        assert drawable != null;
//        drawable.setBounds(0, 0, size, size);
//        drawable.draw(canvas);

        return icon;
    }


    private final static class Worker extends Thread {

        private final Object lock;
        private final IconFactory iconFactory;
        private final ConcurrentLinkedQueue<Task> mQueue = new ConcurrentLinkedQueue<>();

        public Worker(IconFactory iconFactory, Object lock) {
            this.lock = lock;
            this.iconFactory = iconFactory;
        }

        public void add(Context context, IconGenerateListener listener, NotificationBean notification) {
            Task task = new Task(context, listener, notification);
            mQueue.add(task);
        }

        public void remove(NotificationBean notification) {
            for (Task task : mQueue) {
                if (task.notification.equals(notification)) {
                    mQueue.remove(task);
                }
            }
        }

        private static class Task {
            private Context context;
            private IconGenerateListener listener;
            private NotificationBean notification;

            public Task(Context context, IconGenerateListener listener, NotificationBean notification) {
                this.context = context;
                this.listener = listener;
                this.notification = notification;
            }
        }


        @Override
        public void run() {
            super.run();
            while (true) { // TODO stop this
                final Task task;
                synchronized (lock) {
                    if (mQueue.isEmpty()) {
                        return;
                    }
                    task = mQueue.poll();
                }

                final Bitmap bitmap = iconFactory.generateBitmap(task.context, task.notification);
                iconFactory.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (bitmap != null) {
                            task.listener.onGenerated(bitmap);
                        }
                    }
                });
            }
        }
    }

    private boolean isWorkAlive() {
        return mWorker != null && mWorker.isAlive();
    }

}
