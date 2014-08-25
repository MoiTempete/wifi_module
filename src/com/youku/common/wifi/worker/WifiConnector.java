
package com.youku.common.wifi.worker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.youku.common.wifi.listener.WifiConnectListener;
import com.youku.common.wifi.model.AccessPoint;
import com.youku.common.wifi.model.SecurityMode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wi-Fi连接类 Created by MoiTempete.
 */
public class WifiConnector {

    private static final String TAG = "WifiConnector";

    private static final int WIFI_CONNECT_TIMEOUT = 10; // 连接WIFI的超时时间

    private Context mContext;

    private WifiManager mWifiManager;

    private Lock mLock;

    private Condition mCondition;

    private WiFiConnectReceiver mWifiConnectReceiver;

    private WifiConnectListener mWifiConnectListener;

    private boolean mIsConnnected = false;

    private int mNetworkID = -1;

    public WifiConnector(Context context, WifiManager wifiManager, WifiConnectListener listener) {
        mContext = context;
        mLock = new ReentrantLock();
        mCondition = mLock.newCondition();
        mWifiManager = wifiManager;
        mWifiConnectReceiver = new WiFiConnectReceiver();
        mWifiConnectListener = listener;
    }

    public void setListener(WifiConnectListener listener) {
        mWifiConnectListener = listener;
    }

    public WifiConnectListener getListener() {
        return mWifiConnectListener;
    }

    /**
     * 链接指定Wi-Fi
     *
     * @param accessPoint
     *            AP实例中需要包含如下信息: ssid SSID securityMode 加密方式 password 密码
     *            userName 用户名(only in EAP)
     * @param saveConfig
     *            disable | enable to save config
     */
    public void connect(final AccessPoint accessPoint, final boolean saveConfig) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 如果WIFI没有打开，则打开WIFI
                if (!mWifiManager.isWifiEnabled()) {
                    mWifiManager.setWifiEnabled(true);
                }
                // 注册连接结果监听对象
                mContext.registerReceiver(mWifiConnectReceiver, new IntentFilter(
                        WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
                Log.e(TAG, "registerReceiver @ connect @ 86");
                // 连接指定SSID
                if (!onConnect(accessPoint.ssid, accessPoint.securityMode, accessPoint.password, accessPoint.userName)) {
                    mWifiConnectListener.OnWifiConnectFailed();
                    Log.e(TAG, "OnWifiConnectFailed @ connect @ 90");
                    if (saveConfig) {
                        mWifiManager.saveConfiguration();
                    }
                } else {
                    mWifiConnectListener.OnWifiConnectCompleted();
                    Log.e(TAG, "OnWifiConnectFailed @ connect @ 96");
                }
                // 删除注册的监听类对象
                mContext.unregisterReceiver(mWifiConnectReceiver);
                Log.e(TAG, "unregisterReceiver @ connect @ 100");
            }
        }).start();
    }

    protected boolean onConnect(String ssid, SecurityMode mode, String password, String userName) {
        // 添加新的网络配置
        // WifiConfiguration cfg = new WifiConfiguration();
        // cfg.SSID = "\"" + ssid + "\"";
        // if (password != null && !"".equals(password)) {
        // //如果是WEP加密方式的网络，密码需要放到cfg.wepKeys[0]里面
        // if (mode == SecurityMode.WEP) {
        // cfg.wepKeys[0] = "\"" + password + "\"";
        // cfg.wepTxKeyIndex = 0;
        // } else {
        // cfg.preSharedKey = "\"" + password + "\"";
        // }
        // }

        WifiConfiguration cfg = getConfig(ssid, mode, password, userName);
        cfg.status = WifiConfiguration.Status.ENABLED;

        // 添加网络配置
        mNetworkID = mWifiManager.addNetwork(cfg);
        mLock.lock();
        mIsConnnected = false;
        // 连接该网络
        if (!mWifiManager.enableNetwork(mNetworkID, true)) {
            mLock.unlock();
            mContext.unregisterReceiver(mWifiConnectReceiver);
            Log.e(TAG, "unregisterReceiver @ onConnect @ 130");
            return false;
        } else {
            mLock.unlock();
            mContext.unregisterReceiver(mWifiConnectReceiver);
            Log.e(TAG, "unregisterReceiver @ onConnect @ 135");
            return true;
        }
//        try {
//            // 等待连接结果
//            mCondition.await(WIFI_CONNECT_TIMEOUT, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            mContext.unregisterReceiver(mWifiConnectReceiver);
//            Log.e(TAG, "unregisterReceiver @ onConnect @ 140");
//        }
//        mLock.unlock();
//        mContext.unregisterReceiver(mWifiConnectReceiver);
//        Log.e(TAG, "unregisterReceiver @ onConnect @ 143");
//        return mIsConnnected;
    }

    protected WifiConfiguration getConfig(String ssid, SecurityMode mode, String password, String userName) {
        WifiConfiguration config = new WifiConfiguration();

        config.SSID = "\"" + ssid + "\"";

        switch (mode) {
            case OPEN:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;

            case WEP:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                if (password.length() != 0) {
                    int length = password.length();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58) && password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;

            case PSK:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                if (password.length() != 0) {
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;

            case EAP:
                // TODO need check
                config = getEapConfig(ssid, password, userName);
                break;

            default:
                return null;
        }

        return config;
    }

    // 监听系统的WIFI连接消息
    protected class WiFiConnectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (!WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                return;
            }
            mLock.lock();

            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info.getNetworkId() == mNetworkID && info.getSupplicantState() == SupplicantState.COMPLETED) {
                mIsConnnected = true;
                mCondition.signalAll();
            }
            mLock.unlock();
        }
    }

    protected void readEap(WifiConfiguration config) {
        Log.d("WifiPreference", config.toString());
        Log.d("WifiPreference", "SSID" + config.SSID);
        Log.d("WifiPreference", "PASSWORD" + config.preSharedKey);
        Log.d("WifiPreference", "---------ALLOWED ALGORITHMS------------");
        Log.d("WifiPreference", "LEAP=" + config.allowedAuthAlgorithms.get(WifiConfiguration.AuthAlgorithm.LEAP));
        Log.d("WifiPreference", "OPEN=" + config.allowedAuthAlgorithms.get(WifiConfiguration.AuthAlgorithm.OPEN));
        Log.d("WifiPreference", "SHARED=" + config.allowedAuthAlgorithms.get(WifiConfiguration.AuthAlgorithm.SHARED));
        Log.d("WifiPreference", "GROUP CIPHERS=");
        Log.d("WifiPreference", "CCMP=" + config.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.CCMP));
        Log.d("WifiPreference", "TKIP=" + config.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.TKIP));
        Log.d("WifiPreference", "WEP104=" + config.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.WEP104));
        Log.d("WifiPreference", "WEP40=" + config.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.WEP40));
        Log.d("WifiPreference", "----------KEYMGMT------------------------");
        Log.d("WifiPreference", "IEEE8021X=" + config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X));
        Log.d("WifiPreference", "NONE" + config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE));
        Log.d("WifiPreference", "WPA_EAP" + config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP));
        Log.d("WifiPreference", "WPA_PSK" + config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK));
        Log.d("WifiPreference", "---------PairWiseCipher-------------------");
        Log.d("WifiPreference", "CCMP" + config.allowedPairwiseCiphers.get(WifiConfiguration.PairwiseCipher.CCMP));
        Log.d("WifiPreference", "NONE" + config.allowedPairwiseCiphers.get(WifiConfiguration.PairwiseCipher.NONE));
        Log.d("WifiPreference", "TKIP" + config.allowedPairwiseCiphers.get(WifiConfiguration.PairwiseCipher.TKIP));
        Log.d("WifiPreference", "-----------Protocols-----------------------");
        Log.d("WifiPreference", "RSN" + config.allowedProtocols.get(WifiConfiguration.Protocol.RSN));
        Log.d("WifiPreference", "WPA" + config.allowedProtocols.get(WifiConfiguration.Protocol.WPA));
        Log.d("WifiPreference", "---------------WEP Key Strings-----------");
        String[] wepKeys = config.wepKeys;
        Log.d("WifiPreference", "WEP KEY 0" + wepKeys[0]);
        Log.d("WifiPreference", "WEP KEY 1" + wepKeys[1]);
        Log.d("WifiPreference", "WEP KEY 2" + wepKeys[2]);
        Log.d("WifiPreference", "WEP KEY 3" + wepKeys[3]);

        Log.d("WifiPreference", "-----wcEnterpriseField-----");

        Class[] wcClasses = WifiConfiguration.class.getClasses();

        Class wcEnterpriseField = null;

        for (Class wcClass : wcClasses) {

            if (wcClass.getName().contains("EnterpriseField")) {
                wcEnterpriseField = wcClass;
                break;
            }
        }

        Method wcefSetValue = null;

        if (wcEnterpriseField != null) {
            for (Method m : wcEnterpriseField.getMethods()) {
                // System.out.println("getName" + m.getName());
                if ("value".equals(m.getName())) {
                    wcefSetValue = m;
                    break;
                }

            }
        }

        // Dispatching Field vars
        Field[] wcefFields = WifiConfiguration.class.getFields();
        for (Field wcefField : wcefFields) {
            try {
                assert wcefSetValue != null;
                Log.d("WifiPreference", wcefField.getName() + "=" + wcefSetValue.invoke(wcefField.get(config)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected WifiConfiguration getEapConfig(String ssid, String passString, String userName) {

        final String ENTERPRISE_EAP = "PEAP";

        /* Create a WifiConfig */
        WifiConfiguration selectedConfig = new WifiConfiguration();

        /* AP Name */
        selectedConfig.SSID = "\"" + ssid + "\"";
        /* Key Mgmnt */
        selectedConfig.allowedKeyManagement.clear();
        selectedConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        selectedConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);

        /* Group Ciphers */
        selectedConfig.allowedGroupCiphers.clear();
        selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

        /* Pairwise ciphers */
        selectedConfig.allowedPairwiseCiphers.clear();
        selectedConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        selectedConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

        /* Protocols */
        selectedConfig.allowedProtocols.clear();

        selectedConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        selectedConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        Class[] wcClasses = WifiConfiguration.class.getClasses();

        Class wcEnterpriseField = null;

        for (Class wcClass : wcClasses) {
            if (wcClass.getName().contains("EnterpriseField")) {
                wcEnterpriseField = wcClass;
                break;
            }
        }

        Field wcefEap = null, wcefIdentity = null, wcefPassword = null;
        Field[] wcefFields = WifiConfiguration.class.getFields();
        // Dispatching Field vars
        for (Field wcefField : wcefFields) {
            if (wcefField.getName().equals("eap")) {
                wcefEap = wcefField;
            } else if (wcefField.getName().equals("identity")) {
                wcefIdentity = wcefField;
            } else if (wcefField.getName().equals("password")) {
                wcefPassword = wcefField;
            }

        }

        Method wcefSetValue = null;

        if (wcEnterpriseField != null) {
            for (Method m : wcEnterpriseField.getMethods()) {
                System.out.println("methodName--->" + m.getName());
                if (m.getName().trim().equals("setValue")) {
                    wcefSetValue = m;
                    break;
                }

            }
        }
        try {
            /* EAP Method */
            if (wcEnterpriseField != null) {
                assert wcefEap != null;
                assert wcefSetValue != null;
                wcefSetValue.invoke(wcefEap.get(selectedConfig), ENTERPRISE_EAP);

                assert wcefIdentity != null;
                wcefSetValue.invoke(wcefIdentity.get(selectedConfig), userName);

                assert wcefPassword != null;
                wcefSetValue.invoke(wcefPassword.get(selectedConfig), passString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // readEap(selectedConfig);
        return selectedConfig;
    }
}
