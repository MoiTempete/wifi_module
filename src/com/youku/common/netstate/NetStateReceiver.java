
package com.youku.common.netstate;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * Created by MoiTempete.
 */
public class NetStateReceiver extends BroadcastReceiver {

    private NetStateListener mListener;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ethernetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!ethernetInfo.isConnected() && !wifiNetInfo.isConnected()) {
            mListener.onConnect();
        } else {
            mListener.onDisConnect();
        }
    }

    public void setListener(NetStateListener listener){
        mListener = listener;
    }
}