package com.brucetoo.expandrecyclerview.vpn;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.brucetoo.expandrecyclerview.R;

/**
 * Created by Bruce Too
 * On 14/06/2017.
 * At 11:18
 */

public class VpnActivity extends FragmentActivity{

    private static final String TAG = "VpnActivity";

    // VPN服务器地址、端口号、用户名、密码
    private final String VPN_SERVER = "118.184.28.173";
    private final int VPN_PORT = 8388;
    private final String VPN_USERNAME = "";
    private final String VPN_PASSWORD = "allen123453#";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_vpn);

    }

}
