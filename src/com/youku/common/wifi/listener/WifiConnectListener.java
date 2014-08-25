
package com.youku.common.wifi.listener;

/**
 * 通知连接结果的监听接口 Created by MoiTempete.
 */
public interface WifiConnectListener {

    /**
     * 连接成功
     */
    public void OnWifiConnectCompleted();

    /**
     * 连接失败
     */
    public void OnWifiConnectFailed();
}
