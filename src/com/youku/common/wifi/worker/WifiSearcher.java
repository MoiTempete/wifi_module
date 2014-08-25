
package com.youku.common.wifi.worker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import com.youku.common.wifi.listener.WifiSearchListener;
import com.youku.common.wifi.model.AccessPoint;
import com.youku.common.wifi.model.ApStatus;
import com.youku.common.wifi.model.ErrorType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wi-Fi扫描类 Created by MoiTempete.
 */
public class WifiSearcher {
    private static final int WIFI_SEARCH_TIMEOUT = 20; // 扫描WIFI的超时时间

    private Context mContext;

    private WifiManager mWifiManager;

    private WiFiScanReceiver mWifiReceiver;

    private Lock mLock;

    private Condition mCondition;

    private WifiSearchListener mWifiSearchListener;

    private boolean mIsWifiScanCompleted = false;

    public WifiSearcher(Context context, WifiManager wifiManager, WifiSearchListener listener) {
        mContext = context;
        mWifiSearchListener = listener;
        mLock = new ReentrantLock();
        mCondition = mLock.newCondition();
        mWifiManager = wifiManager;
        mWifiReceiver = new WiFiScanReceiver();
    }

    public void setListener(WifiSearchListener listener) {
        mWifiSearchListener = listener;
    }

    public WifiSearchListener getListener() {
        return mWifiSearchListener;
    }

    public void search() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 如果WIFI没有打开，则打开WIFI
                if (!mWifiManager.isWifiEnabled()) {
                    mWifiManager.setWifiEnabled(true);
                }
                // 注册接收WIFI扫描结果的监听类对象
                mContext.registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                // 开始扫描
                mWifiManager.startScan();

                mLock.lock();
                // 阻塞等待扫描结果
                try {
                    mIsWifiScanCompleted = false;
                    mCondition.await(WIFI_SEARCH_TIMEOUT, TimeUnit.SECONDS);
                    if (!mIsWifiScanCompleted) {
                        mWifiSearchListener.onSearchWifiFailed(ErrorType.SEARCH_WIFI_TIMEOUT);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mLock.unlock();
                // 删除注册的监听类对象
                mContext.unregisterReceiver(mWifiReceiver);
            }
        }).start();
    }

    // 系统WIFI扫描结果消息的接收者
    protected class WiFiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            // 提取扫描结果
            List<ScanResult> scanResults = mWifiManager.getScanResults();
            List<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
            for (ScanResult scanResult : scanResults) {
                AccessPoint ap = AccessPoint.loadData(scanResult);
                if (isCurrent(scanResult)) {
                    ap.status = ApStatus.CURRENT;
                    accessPoints.add(0, ap);
                } else {
                    ap.status = ApStatus.ENABLED;
                    accessPoints.add(ap);
                }
            }
            // 检测扫描结果
            if (scanResults.isEmpty()) {
                mWifiSearchListener.onSearchWifiFailed(ErrorType.NO_WIFI_FOUND);
            } else {
                mWifiSearchListener.onSearchWifiSuccess(accessPoints);
            }
            mLock.lock();
            mIsWifiScanCompleted = true;
            mCondition.signalAll();
            mLock.unlock();
        }
    }

    protected boolean isCurrent(ScanResult scanResult) {
        return scanResult.BSSID.equals(mWifiManager.getConnectionInfo().getBSSID());
    }
}
