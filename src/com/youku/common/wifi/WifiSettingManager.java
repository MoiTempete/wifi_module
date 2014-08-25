
package com.youku.common.wifi;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import com.youku.common.wifi.listener.WifiConnectListener;
import com.youku.common.wifi.listener.WifiSearchListener;
import com.youku.common.wifi.model.AccessPoint;
import com.youku.common.wifi.worker.WifiConnector;
import com.youku.common.wifi.worker.WifiSearcher;

import java.util.ArrayList;
import java.util.List;

/**
 * WifiManager 封装类 Created by MoiTempete.
 */
public class WifiSettingManager {

    /**
     * LINK_SPEED_UNITS
     */
    public static final String LINK_SPEED_UNITS = "Mbps";

    private Context mContext;

    private WifiManager mWifiManager;

    private WifiInfo mWifiInfo;

    private DhcpInfo mDhcpInfo;

    private WifiConnector mWifiConnector;

    private WifiSearcher mWifiSearcher;

    public WifiSettingManager(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
        mDhcpInfo = mWifiManager.getDhcpInfo();
    }

    /**
     * get WifiConnector
     *
     * @param listener
     *            WifiConnectListener
     * @return WifiConnector
     */
    private WifiConnector getWifiConnector(WifiConnectListener listener) {
        if (mWifiConnector == null) {
            mWifiConnector = new WifiConnector(mContext, mWifiManager, listener);
        }
        mWifiConnector.setListener(listener);
        return mWifiConnector;
    }

    /**
     * connect Wi-Fi
     *
     * @param accessPoint
     *            AP实例中需要包含如下信息: ssid SSID securityMode 加密方式 password 密码
     *            userName 用户名(only in EAP)
     * @param saveConfig
     *            disable | enable to save config
     * @param listener
     *            连接状态监听
     */
    public void connect(AccessPoint accessPoint, boolean saveConfig, WifiConnectListener listener) {
        // TODO check data
        getWifiConnector(listener).connect(accessPoint, saveConfig);
    }

    /**
     * get WifiSearcher
     *
     * @param listener
     *            WifiSearchListener
     * @return WifiSearcher
     */
    private WifiSearcher getWifiSearcher(WifiSearchListener listener) {
        if (mWifiSearcher == null) {
            mWifiSearcher = new WifiSearcher(mContext, mWifiManager, listener);
        }
        mWifiSearcher.setListener(listener);
        return mWifiSearcher;
    }

    /**
     * search Wi-Fi
     *
     * @param listener
     *            WifiSearchListener
     */
    public void search(WifiSearchListener listener) {
        getWifiSearcher(listener).search();
    }

    /**
     * update Wi-Fi & DHCP info after disconnect & connect
     */
    public void updateWifiStat() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        mDhcpInfo = mWifiManager.getDhcpInfo();
    }

    /**
     * Return whether Wi-Fi is enabled or disabled.
     *
     * @return result
     */
    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    /**
     * Enable or disable Wi-Fi.
     *
     * @param wifiEnable
     *            {@code true} to enable, {@code false} to disable
     * @return result
     */
    public boolean setWifiEnable(boolean wifiEnable) {
        return mWifiManager.setWifiEnabled(wifiEnable);
    }

    /**
     * 计算信号的等级
     *
     * @param rssi
     *            rssi
     * @param numLevels
     *            numLevels
     * @return 新号等级
     */
    public static int calculateSignalLevel(int rssi, int numLevels) {
        return WifiManager.calculateSignalLevel(rssi, numLevels);
    }

    /**
     * 让一个网络连接失效
     *
     * @param netId
     *            net ID
     * @return result
     */
    public boolean disableNetwork(int netId) {
        return mWifiManager.disableNetwork(netId);
    }

    /**
     * 断开当前的Wi-Fi连接
     */
    public void disconnectWifi() {
        int netId = getNetworkId();
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
        mWifiInfo = null;
    }

    /**
     * 获取当前SSID
     *
     * @return SSID
     */
    public String getSSID() {
        return (mWifiInfo == null) ? "" : mWifiInfo.getSSID();
    }

    /**
     * 获取当前netId
     *
     * @return Net Id
     */
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    /**
     * 获取已连接Wi-Fi的IP地址
     *
     * @return result
     */
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    /**
     * 获取已连接Wi-Fi的Mac地址
     *
     * @return MAC-Address
     */
    public String getMacAddress() {
        return (mWifiInfo == null) ? "" : mWifiInfo.getMacAddress();
    }

    /**
     * 获取已连接Wi-Fi的BSSID
     *
     * @return BSSID
     */
    public String getBSSID() {
        return (mWifiInfo == null) ? "" : mWifiInfo.getBSSID();
    }

    /**
     * 获取已连接Wi-Fi的连接速度
     *
     * @return the link speed.
     * @see #LINK_SPEED_UNITS
     */
    public int getLinkSpeed() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getLinkSpeed();
    }

    /**
     * 获取已连接Wi-Fi的信号强度
     *
     * @return the RSSI, in the range ??? to ???
     */
    public int getRssi() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getRssi();
    }

    /**
     * 获取已连接Wi-Fi的首选DNS地址
     *
     * @return 首选DNS地址
     */
    public String getDns1() {
        return (mDhcpInfo == null) ? "" : FormatIP(mDhcpInfo.dns1);
    }

    /**
     * 获取已连接Wi-Fi的备选DNS地址
     *
     * @return 备选DNS地址
     */
    public String getDns2() {
        return (mDhcpInfo == null) ? "" : FormatIP(mDhcpInfo.dns2);
    }

    /**
     * 获取已连接Wi-Fi的网关地址
     *
     * @return 网关地址
     */
    public String getGateway() {
        return (mDhcpInfo == null) ? "" : FormatIP(mDhcpInfo.gateway);
    }

    /**
     * 获取已经记住的网络
     *
     * @return List<AccessPoint>
     */
    public List<AccessPoint> getConfiguredNetworks() {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        List<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        for (WifiConfiguration config : configs) {
            accessPoints.add(AccessPoint.loadData(config));
        }
        return accessPoints;
    }

    /**
     * 重新连接Wi-Fi网络，即使该网络是已经被连接上的
     *
     * @return result
     */
    public boolean reassociate() {
        return mWifiManager.reassociate();
    }

    /**
     * 重新连接一个未连接上的WIFI网络
     *
     * @return result
     */
    public boolean reconnect() {
        return mWifiManager.reconnect();
    }

    /**
     * 移除某一个网络
     *
     * @param netId
     *            网络ID
     * @return result
     */
    public boolean removeNetwork(int netId) {
        return mWifiManager.removeNetwork(netId);
    }

    /**
     * 查看以前是否也配置过这个网络
     *
     * @param SSID
     *            SSID
     * @return 如果已经配置过则返回配置config，若未配置过，则返回null
     */
    public WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    // IP地址转化为字符串格式
    public static String FormatIP(int IpAddress) {
        return Formatter.formatIpAddress(IpAddress);
    }

}
