
package com.youku.common.wifi.listener;

import com.youku.common.wifi.model.AccessPoint;
import com.youku.common.wifi.model.ErrorType;

import java.util.List;

/**
 * Created by MoiTempete.
 */
public interface WifiSearchListener {

    /**
     * 扫描网络错误
     *
     * @param errorType
     *            错误类型
     */
    public void onSearchWifiFailed(ErrorType errorType);

    /**
     * 扫描结果
     *
     * @param results
     *            ScanResult列表
     */
    public void onSearchWifiSuccess(List<AccessPoint> results);
}
