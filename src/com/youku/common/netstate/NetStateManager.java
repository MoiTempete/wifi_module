
package com.youku.common.netstate;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by MoiTempete.
 */
public class NetStateManager {

    private Context mContext;
    private NetStateReceiver mReceiver;

    public NetStateManager(Context context) {
        mContext = context;
    }

    /**
     * 判断是否有网络连接
     * 
     * @return
     */
    public boolean isNetworkConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        return mNetworkInfo != null && mNetworkInfo.isAvailable();
    }

    /**
     * 判断是否有Wi-Fi连接
     *
     * @return
     */
    public boolean isWifiConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWiFiNetworkInfo != null && mWiFiNetworkInfo.isAvailable();
    }

    /**
     * 判断ETHERNET网络是否可用
     * 
     * @return
     */
    public boolean isEthernetConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        return mMobileNetworkInfo != null && mMobileNetworkInfo.isAvailable();
    }

    /**
     * 
     * @param context
     * 
     * @return
     */

    public static NetType getAPNType(Context context) {

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            return NetType.NONE;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            return NetType.WIFI;
        } else if (nType == ConnectivityManager.TYPE_BLUETOOTH) {
            return NetType.BLUETOOTH;
        } else if (nType == ConnectivityManager.TYPE_ETHERNET) {
            return NetType.ETHERNET;
        }
        return NetType.UNKNOWN;
    }

    /**
     * 注册网络状态监听
     * @param listener
     */
    public void registerReceiver(NetStateListener listener) {
        if (mReceiver == null) {
            mReceiver = new NetStateReceiver();
        }
        mReceiver.setListener(listener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mReceiver, filter);
    }

    /**
     * 注销网络状态监听
     */
    public void unRegisterReceiver() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
        }
    }
}
