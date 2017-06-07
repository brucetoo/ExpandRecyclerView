package com.brucetoo.expandrecyclerview;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Window;

import com.brucetoo.expandrecyclerview.reflect.Reflecter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Bruce Too
 * On 02/06/2017.
 * At 10:24
 */

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //android.app.ActivityThread

        //currentActivityThread Method

        //mInstrumentation

        //通过获取隐藏的ActivityThread类来获取当前的 ActivityThread
        Object currentActivityThread = Reflecter.on("android.app.ActivityThread").call("currentActivityThread").get();
        //获取当前的 Instrumentation
        Instrumentation originInstrumentation = Reflecter.on(currentActivityThread).get("mInstrumentation");
        //DemoInstrumentation代理实现Instrumentation
        Reflecter.on(currentActivityThread).set("mInstrumentation", new DemoInstrumentation(originInstrumentation));

    }

    private class DemoInstrumentation extends Instrumentation {

        //如果需要全权代理 originInstrumentation 则所有方法的 super全部删除,且动态由originInstrumentation执行一次
        private Instrumentation originInstrumentation;

        public DemoInstrumentation(Instrumentation originInstrumentation) {
            this.originInstrumentation = originInstrumentation;
        }

        //该api为隐藏的,所有在此Copy一份,用originInstrumentation重新调用
        public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {

            Log.e("DemoInstrumentation", "execStartActivity = " + target + "}: we add a log here....");
            //用原始的Instrumentation 调用执行系统逻辑
            return Reflecter.on(originInstrumentation).call("execStartActivity", who, contextThread, token, target,
                intent, requestCode, options).get();
        }

        @Override
        public void callActivityOnCreate(Activity activity, Bundle icicle) {
//            super.callActivityOnCreate(activity, icicle);
            //或者写成让originInstrumentation全权代理~~ 这样就需要重写所有 Instrumentation的方法
            originInstrumentation.callActivityOnCreate(activity,icicle);

            Log.i("DemoInstrumentation", "callActivityOnCreate: " + activity + "");

            //动态代理 activity的接口 Window.Callback
            Object callBack = Proxy.newProxyInstance(getClassLoader(), new Class[]{Window.Callback.class}, new DemoInvocationHandler(activity));
            Reflecter.on(activity.getWindow()).set("mCallback",callBack);
        }
    }

    private class DemoInvocationHandler implements InvocationHandler {

        private Activity mBase;

        public DemoInvocationHandler(Activity activity) {
            this.mBase = activity;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.i("DemoInstrumentation", "invoke method :" + method.getName());
            if(method.getName().equals("onWindowFocusChanged")){

            }
            //调用该activity中的回调
            return method.invoke(mBase,args);
        }
    }
}
