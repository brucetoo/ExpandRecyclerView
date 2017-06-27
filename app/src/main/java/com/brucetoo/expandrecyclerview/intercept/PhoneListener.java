package com.brucetoo.expandrecyclerview.intercept;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.brucetoo.expandrecyclerview.reflect.Reflecter;

import static com.brucetoo.expandrecyclerview.intercept.InterceptActivity.SWITCH_ON;

/**
 * Created by Bruce Too
 * On 27/06/2017.
 * At 11:56
 */

public class PhoneListener extends BroadcastReceiver {

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Log.i("PhoneListener", "onReceive: action -> " + intent.getAction());
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    PhoneStateListener mListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.e("InterceptActivity", "incomingNumber -> " + incomingNumber);
                    if (needIntercept(incomingNumber)) {
                        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
                        Reflecter.on(tm).call("getITelephony").call("endCall");
                        NotificationManager.addPhone("Missed Call", incomingNumber);
                    }
                    break;
            }
        }
    };


    private boolean needIntercept(String number) {
        return SWITCH_ON;
    }

}
