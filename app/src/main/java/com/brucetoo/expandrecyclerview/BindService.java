package com.brucetoo.expandrecyclerview;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * Created by Bruce Too
 * On 09/02/2017.
 * At 11:50
 */

public class BindService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final ICompute.Stub mBinder = new ICompute.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public TestParcel add(int a, int b) throws RemoteException {
            TestParcel parcel = new TestParcel();
            parcel.name = "What the fucking girl.";
            parcel.time = 1000;
            return parcel;
        }
    };
}
