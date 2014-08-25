
package com.youku.common.wifi.model;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by MoiTempete.
 */
public class AccessPoint implements Parcelable {

    private static final String SECURITY_STRING_EAP = "802.1x EAP";

    private static final String SECURITY_STRING_WEP = "WEP";

    private static final String SECURITY_STRING_WPA = "WPA PSK";

    private static final String SECURITY_STRING_WPA2 = "WPA2 PSK";

    private static final String SECURITY_STRING_WAP_WAP2 = "WAP/WAP2 PSK";

    private static final String SECURITY_STRING_PSK_UNKNOWEN = "UNKNOWN PSK";

    private static final String SECURITY_STRING_OPEN = "OPEN";

    /**
     * SSID
     */
    public String ssid;

    /**
     * 网络加密方式
     */
    public SecurityMode securityMode;

    public String securityString;

    /**
     * 用户名 only need in EAP, set only
     */
    public String userName;

    /**
     * 密码 set only
     */
    public String password;

    /**
     * BSSID
     */
    public String bssid;

    /**
     * 状态
     *
     * @see ApStatus
     */
    public ApStatus status;

    /**
     * ID only available in WifiSettingManager.getConfiguredNetworks()
     */
    public int networkId;

    /**
     * only available in WifiSettingManager.search()
     */
    public int level;

    /**
     * only available in WifiSettingManager.search()
     */
    public int frequency;

    public AccessPoint() {

    }

    public static AccessPoint loadData(WifiConfiguration configuration) {
        if (configuration == null) {
            return null;
        }
        AccessPoint accessPoint = new AccessPoint();
        accessPoint.ssid = configuration.SSID;
        accessPoint.securityMode = getSecurity(configuration);
        accessPoint.securityString = getSecurityString(configuration);
        accessPoint.bssid = configuration.BSSID;
        accessPoint.status = parse2ApStatus(configuration.status);
        accessPoint.networkId = configuration.networkId;

        return accessPoint;
    }

    public static AccessPoint loadData(ScanResult scanResult) {
        if (scanResult == null) {
            return null;
        }
        AccessPoint accessPoint = new AccessPoint();
        accessPoint.ssid = scanResult.SSID;
        accessPoint.securityMode = getSecurity(scanResult);
        accessPoint.securityString = getSecurityString(scanResult);
        accessPoint.bssid = scanResult.BSSID;
        accessPoint.level = scanResult.level;
        accessPoint.frequency = scanResult.frequency;
        return accessPoint;
    }

    /**
     * 通过ScanResult获取Wi-Fi的加密方式
     *
     * @param result
     *            ScanResult
     * @return 枚举类SecurityMode中的一种
     */
    protected static SecurityMode getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SecurityMode.WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SecurityMode.PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SecurityMode.EAP;
        }
        return SecurityMode.OPEN;
    }

    /**
     * 获取preSharedKey的具体类型
     *
     * @param result
     *            ScanResult
     * @return 枚举类PskType中的一种
     */
    protected static PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            return PskType.UNKNOWN;
        }
    }

    /**
     * 通过WifiConfiguration获取Wi-Fi的加密方式
     *
     * @param config
     *            WifiConfiguration
     * @return 枚举类SecurityMode中的一种
     */
    protected static SecurityMode getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SecurityMode.PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP)
                || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return SecurityMode.EAP;
        }
        return (config.wepKeys[0] != null) ? SecurityMode.WEP : SecurityMode.OPEN;
    }

    /**
     * 通过ScanResult获取加密描述字符串
     *
     * @param scanResult
     *            ScanResult
     * @return 加密方式
     */
    protected static String getSecurityString(ScanResult scanResult) {
        switch (getSecurity(scanResult)) {
            case EAP:
                return SECURITY_STRING_EAP;
            case PSK:
                switch (getPskType(scanResult)) {
                    case WPA:
                        return SECURITY_STRING_WPA;
                    case WPA2:
                        return SECURITY_STRING_WPA2;
                    case WPA_WPA2:
                        return SECURITY_STRING_WAP_WAP2;
                    case UNKNOWN:
                    default:
                        return SECURITY_STRING_PSK_UNKNOWEN;
                }
            case WEP:
                return SECURITY_STRING_WEP;
            case OPEN:
            default:
                return SECURITY_STRING_OPEN;
        }
    }

    /**
     * 通过WifiConfiguration获取加密描述字符串
     *
     * @param configuration
     *            WifiConfiguration
     * @return 加密方式
     */
    protected static String getSecurityString(WifiConfiguration configuration) {
        switch (getSecurity(configuration)) {
            case EAP:
                return SECURITY_STRING_EAP;
            case PSK:
                return SECURITY_STRING_PSK_UNKNOWEN;
            case WEP:
                return SECURITY_STRING_WEP;
            case OPEN:
            default:
                return SECURITY_STRING_OPEN;
        }
    }

    protected static ApStatus parse2ApStatus(int i) {
        switch (i) {
            case 0:
                return ApStatus.CURRENT;
            case 1:
                return ApStatus.DISABLED;
            case 2:
                return ApStatus.ENABLED;
        }
        return ApStatus.UNKNOWN;
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ssid);
        dest.writeString(securityString);
        dest.writeString(userName);
        dest.writeString(password);
        dest.writeInt(networkId);
        dest.writeInt(level);
        dest.writeInt(frequency);
        dest.writeValue(securityMode); 
        dest.writeValue(status);
	}
	
    public void readFromParcel(Parcel in) {
    	ssid = in.readString();
    	securityString = in.readString();
    	userName = in.readString();
    	password = in.readString();
    	networkId = in.readInt();
    	level = in.readInt();
    	frequency = in.readInt();
    	securityMode = (SecurityMode) in.readValue(SecurityMode.class.getClassLoader());
    	status = (ApStatus) in.readValue(ApStatus.class.getClassLoader());
    }
    
	AccessPoint(Parcel in) {
        readFromParcel(in);
    }
    
    public static final Creator<AccessPoint> CREATOR = new Creator<AccessPoint>() {
        public AccessPoint createFromParcel(Parcel source) {
            return new AccessPoint(source);
        }
        public AccessPoint[] newArray(int size) {
            return new AccessPoint[size];
        }
    };
}
