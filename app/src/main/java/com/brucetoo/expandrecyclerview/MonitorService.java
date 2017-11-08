package com.brucetoo.expandrecyclerview;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.brucetoo.expandrecyclerview.reflect.Reflecter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * Created by Bruce Too
 * On 07/06/2017.
 * At 17:43
 */

public class MonitorService extends Service {

    private static final String TAG = MonitorService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        //Hook AMS
        //最终执行startActivity是在 Instrumentation #1507 ActivityManagerNative.getDefault()
//                .startActivity
        //获取IActivityManager服务
        Object singleton;
        Object iAM;
        if(Build.VERSION.SDK_INT > 25){//8.0+
            singleton = Reflecter.on("android.app.ActivityManager").get("IActivityManagerSingleton");
        }else {
            singleton = Reflecter.on("android.app.ActivityManagerNative").get("singleton");
        }
        iAM = Reflecter.on(singleton).get("mInstance");//mInstance就代表 IActivityManager
        Class<?> aClass = null;
        try {
            aClass = Class.forName("android.app.IActivityManager");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //设置动态代理 IActivityManager 的回调方法,特别是startActivity方法
        Object hookIAM = Proxy.newProxyInstance(getClassLoader(), new Class[]{aClass}, new HookIActivityManager(iAM));
        //将hook后的对象塞到原本的mInstance中
        Reflecter.on(singleton).set("mInstance", hookIAM);

        Object activityThread = Reflecter.on("android.app.ActivityThread").call("currentActivityThread").get();
        Handler baseMH = Reflecter.on(activityThread).get("mH");
//        Object newCallback = Proxy.newProxyInstance(getClassLoader(), new Class[]{Handler.Callback.class}, new HookmHHandler(mCallback));
        Reflecter.on(activityThread).field("mH").set("mCallback", new HookCallBack(baseMH));
    }

    private class HookCallBack implements Handler.Callback {

        Handler base;

        public HookCallBack(Handler base) {
            this.base = base;
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 100) {//start_activity
                //将intent中的Component换成替换之前的
                //intent在哪里？ ActivityClientRecord.intent
                // final ActivityThread.ActivityClientRecord r = (ActivityClientRecord) msg.obj;
                Intent now = Reflecter.on(msg.obj).get("intent");
                Intent old = now.getParcelableExtra("old_intent");
                if (old != null) {
                    now.setComponent(old.getComponent());
                }
            }

            base.handleMessage(msg);
            return true;
        }
    }

    private class HookIActivityManager implements InvocationHandler {

        Object base;

        public HookIActivityManager(Object base) {
            this.base = base;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("startActivity".equals(method.getName())) {
                Log.d(TAG, "invoke  startActivity args[0] = " + args[0]);
//                (IApplicationThread caller, String callingPackage, Intent intent,
//                    String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags,
//                ProfilerInfo profilerInfo, Bundle options) throws RemoteException

                Intent old;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        index = i;
                        break;
                    }
                }
                old = (Intent) args[index];
                //伪造新的Intent 带上需要启动的activity
                Intent newIn = new Intent();
                //包名 + 类名
                String pagepackage = "com.brucetoo.expandrecyclerview";
                //ignore system setting options!
                if (old != null && old.getComponent() != null) {
                    Log.i(TAG, "invoke: oldComponent -> " + old.getComponent().toString());
                    //只关心启动这个
                    if (old.getComponent().getClassName().contains("ViewAnimatorActivity")) {
                        newIn.setComponent(new ComponentName(pagepackage, StubActivity.class.getName()));
                        newIn.putExtra("old_intent", old);
                        args[index] = newIn;
                    }
                }
                return method.invoke(base, args);
            }

            if ("activityResumed".equals(method.getName())) {
                Log.d(TAG, "invoke  activityResumed args[0] = " + args[0]);
                fetchInfoByToken(args[0]);
            }

            if ("activityPaused".equals(method.getName())) {
                Log.d(TAG, "invoke  activityPaused args[0] = " + args[0]);
                fetchInfoByToken(args[0]);
            }

            if ("activityStopped".equals(method.getName())) {
                Log.d(TAG, "invoke  activityStopped args[0] = " + args[0]);
            }

            return method.invoke(base, args);
        }
    }

    private void fetchInfoByToken(Object token) {
    }
}
