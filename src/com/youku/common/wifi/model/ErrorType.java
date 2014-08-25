
package com.youku.common.wifi.model;

/**
 * 扫描网络错误类型 Created by MoiTempete.
 */
public enum ErrorType {
    SEARCH_WIFI_TIMEOUT, // 扫描WIFI超时（一直搜不到结果）
    NO_WIFI_FOUND, // 扫描WIFI结束，没有找到任何WIFI信号
}
