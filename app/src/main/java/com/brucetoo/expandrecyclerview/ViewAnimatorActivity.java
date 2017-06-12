package com.brucetoo.expandrecyclerview;

import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.brucetoo.expandrecyclerview.animator.AnimatorListener;
import com.brucetoo.expandrecyclerview.animator.ViewAnimator;
import com.brucetoo.expandrecyclerview.reflect.Reflecter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

import static com.brucetoo.expandrecyclerview.reflect.Reflecter.on;
import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * Created by Bruce Too
 * On 12/01/2017.
 * At 14:12
 */

public class ViewAnimatorActivity extends AppCompatActivity {

    @Override
    public ComponentName getComponentName() {
        ComponentName componentName = super.getComponentName();
        //绕过系统检查的方法
        return  new ComponentName(componentName.getPackageName(), StubActivity.class.getName());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_viewanimator);

        View image = findViewById(R.id.image);
        View text = findViewById(R.id.text);

//        ViewAnimator
//            .putOn(image)
//            .alpha(0.2f)
//            .animate()
//            .translationY(-1000, 0)
//            .alpha(0,1)
//            .andAnimate(text)
//            .dp().translationX(-20, 0)
//            .decelerate()
//            .duration(2000)
//            .thenAnimate(image)
//            .scale(1f, 0.5f, 1f)
//            .accelerate()
//            .duration(1000)
//            .start();

        final String TAG = "ViewAnimator";
        ViewAnimator
            .putOn(image)
            .alpha(0.2f)
            .translation(100, 100)
            .animate()
            .waitForSize()
            .dp().width(100, 400)
            .dp().height(50, 200)
            .translationX(0)
            .translationY(0)
            .alpha(1)
            .andAnimate(text)
            .textColor(Color.BLACK, Color.GREEN)
            .backgroundColor(Color.WHITE, Color.BLACK)
            .accelerate()
            .duration(2000)
            .thenAnimate(image)
            .rotationY(360)
            .repeatCount(3)
            .repeatMode(ValueAnimator.RESTART)
            .duration(2000)
            .onStart(new AnimatorListener.Start() {
                @Override
                public void onStart() {
                    Log.e(TAG, "onStart: ");
                }
            })
            .onEnd(new AnimatorListener.End() {
                @Override
                public void onEnd() {
                    Log.e(TAG, "onEnd: ");
                }
            })
            .thenAnimate(text)
            .custom(new AnimatorListener.Update<TextView>() {
                @Override
                public void onUpdate(TextView view, float value) {
                    view.setText("This is ViewAnimator " + value);
                }
            }, 1, 100)
            .duration(2000)
            .start();


        bindService(new Intent(this, BindService.class), serviceConnection, BIND_AUTO_CREATE);

        //找到系统的clipboard binder
        IBinder clipboard = on("android.os.ServiceManager").call("getService", "clipboard").get();
        //hook asInterface -> queryLocalInterface 方法(是接口所以可以动态代理)
        //原因 所有的service 都会通过IBinder.asInterface(baseBinder)构建一个新
        //当构建的时候 先在本地缓存查(我们就在此做替换) 如果没有才会重新构建一个代理Binder I***$Stub$Proxy
        IBinder hookedBinder = (IBinder) newProxyInstance(getClassLoader(), new Class[]{IBinder.class}, new HookBinderHandler(clipboard));
        //利用伪造的binder去替换掉系统服务中的剪切板binder
        HashMap<String, IBinder> cache = Reflecter.on("android.os.ServiceManager").field("sCache").get();
        cache.put("clipboard", hookedBinder);
    }

    private class HookBinderHandler implements InvocationHandler {

        //原本的 clipboard binder
        IBinder mBase;

        public HookBinderHandler(IBinder base) {
            this.mBase = base;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //hook掉mBase的queryLocalInterface方法 用伪造的代替
            if(method.getName().equals("queryLocalInterface")){
                //hook android.os.IInterface
                //满足返回值不为null，且是IClipboard类型 -- 重新new一个从而伪造一个假的
                //怎么伪造 ?? IClipboard.Stub.asInterface(b)
                Object fakeIInterface = Reflecter.on("android.content.IClipboard$Stub").call("asInterface",mBase).get();
                //返回一个 IInterface接口
                return newProxyInstance(getClassLoader(), new Class[]{Class.forName("android.content.IClipboard")}, new HookIClipboardHandler(fakeIInterface));
            }
            return method.invoke(mBase,args);
        }
    }

    private class HookIClipboardHandler implements InvocationHandler{

        Object mBase;//(IInterface)

        public HookIClipboardHandler(Object base) {
            this.mBase = base;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //开始hook掉剪切板的方法
            if("hasClipboardText".equals(method.getName())){
                return true;// 总是返回true
            }
            if ("getPrimaryClip".equals(method.getName())) {
                return ClipData.newPlainText(null, "老子被改了~~~~");
            }
            return method.invoke(mBase,args);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventHappen(TestEvent ev) {
        Log.e("Test", "onEventHappen: " + ev.test);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

    }

    private ICompute compute;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            compute = ICompute.Stub.asInterface(service);
            try {
                Log.e("Test", "onServiceConnected -> " + compute.add(1, 2).name + " " + compute.add(1, 2).time);
                EventBus.getDefault().post(new TestEvent("onServiceConnected"));
            } catch (RemoteException e) {
                Log.e("Test", "onServiceConnected exception ");
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("Test", "onServiceDisconnected ");
            compute = null;
        }
    };
}
