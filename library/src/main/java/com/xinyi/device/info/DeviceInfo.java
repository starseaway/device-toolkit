package com.xinyi.device.info;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import com.xinyi.device.DeviceContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * 设备信息获取，用于获取设备的各种基本信息
 *
 * @author 新一
 * @date 2025/4/2 10:00
 */
public class DeviceInfo {

    private DeviceInfo() { }

    /**
     * 获取设备型号
     *
     * @return 设备型号
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * 获取设备制造商
     *
     * @return 设备制造商
     */
    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取 Android 系统版本
     *
     * @return Android 系统版本
     */
    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 系统源代码控制值，一个数字或者git hash值
     */
    public static String getBuildVersionINCREMENTAL() {
        return Build.VERSION.INCREMENTAL;
    }

    /**
     * 设备当前的系统开发代号，一般使用REL代替
     */
    public static String getBuildVersionCODENAME() {
        return Build.VERSION.CODENAME;
    }

    /**
     * 获取设备的构建版本号（Build ID）
     * <p>
     * 这是设备系统版本的标识符，通常表示ROM的内部版本号，
     * 不同设备或同型号的设备可能相同。
     * </p>
     *
     * @return 设备系统构建版本号
     */
    public static String getDeviceID() {
        return Build.ID;
    }

    /**
     * 获取设备品牌
     *
     * @return 设备品牌
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * 获取设备引导程序版本号
     */
    public static String getAndroidBOOTLOADER() {
        return Build.BOOTLOADER;
    }

    /**
     * 获取设备的构建指纹
     * <p>
     * 构建指纹是完整的系统版本标识符，
     * 用于唯一标识设备的ROM版本，
     * 不同设备即使是同一型号但刷了不同ROM，指纹也会不同。
     * 但它不是设备唯一标识，
     * 多台相同机型的设备可能具有相同的构建指纹。
     * </p>
     *
     * @return 设备构建指纹
     */
    public static String getAndroidFINGERPRINT() {
        return Build.FINGERPRINT;
    }

    /**
     * 获取设备的硬件信息
     *
     * @return 设备硬件信息
     */
    public static String getHardware() {
        return Build.HARDWARE;
    }

    /**
     * 获取手机Android API等级（22、23 ...）
     * SDK_INT 系统的API级别 数字表示
     */
    public static int getSDK_INT() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取基带版本
     */
    public static String getBaseBandVer() {
        Object result = null;
        try {
            @SuppressLint("PrivateApi")
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");

            Object invoker = systemProperties.newInstance();
            Method method = systemProperties.getMethod("get", String.class, String.class);
            result = method.invoke(invoker, "gsm.version.baseband", "no message");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (result != null) {
            return result.toString();
        } else {
            return "";
        }
    }

    /**
     * 获取android内核版本
     */
    public static String getCoreVersion() {
        return "Linux version " + System.getProperty("os.version");
    }

    /**
     * 获取设备的序列号 (需权限 <uses-permission android:name="android.permission.READ_PRIVILEGED_PHONE_STATE" />)
     *
     * @return 设备序列号
     */
    @SuppressLint("HardwareIds")
    public static String getSerialNumber() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Build.getSerial();
        } else {
            return Build.SERIAL;
        }
    }

    /**
     * 际移动用户识别码MD5; 国际移动用户身份（需权限 <uses-permission android:name="android.permission.READ_PRIVILEGED_PHONE_STATE" />）
     */
    @SuppressLint("HardwareIds")
    public static String getIMSI() {
        TelephonyManager telephonyManager = DeviceContext.getTelephonyManager();
        String imsi = telephonyManager.getSubscriberId();
        return imsi == null ? "" : imsi;
    }

    /**
     * 获取设备的 IMEI (需权限 <uses-permission android:name="android.permission.READ_PHONE_STATE" />)
     *
     * @return IMEI
     */
    @SuppressLint("HardwareIds")
    public static String getIMEI() {
        TelephonyManager telephonyManager = DeviceContext.getTelephonyManager();
        if (ActivityCompat.checkSelfPermission(DeviceContext.getApplication(),
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return telephonyManager.getDeviceId();
        }
        return null;
    }

    /**
     * 通过网络接口获取设备的 MAC 地址
     * 依次检查以太网（ETH）、Wi-Fi、wlan0 接口的 MAC 地址
     *
     * @return 返回设备的 MAC 地址，若无 MAC 地址则返回空字符串
     */
    public static String getDeviceMac() {
        String ethMAC = getEthMAC();
        if (!ethMAC.isEmpty()) {
            return ethMAC;
        } else {
            String ethMacByInterface = getMacByInterface("eth");
            if (ethMacByInterface != null && !ethMacByInterface.isEmpty()) {
                return ethMacByInterface;
            } else {
                String wifiMac = getWifiMac();
                if (!wifiMac.isEmpty()) {
                    return wifiMac;
                } else {
                    String wlanMac = getWlanMac();
                    if (!wlanMac.isEmpty()) {
                        return wlanMac;
                    } else {
                        String wifiMacByInterface = getMacByInterface("wlan0");
                        if (wifiMacByInterface == null) {
                            wifiMacByInterface = "";
                        }
                        return !wifiMacByInterface.isEmpty() ? wifiMacByInterface : "";
                    }
                }
            }
        }
    }

    /**
     * 获取以太网（ETH）接口的 MAC 地址
     *
     * @return 以太网的 MAC 地址，若无法获取则返回空字符串
     */
    public static String getEthMAC() {
        String macSerial = null;
        String str = "";
        try {
            // 通过命令获取以太网接口的 MAC 地址
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/eth0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            while (null != str) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();
                    break;
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return macSerial != null && macSerial.length() == 17 ? macSerial.toUpperCase() : "";
    }

    /**
     * 获取指定网络接口的 MAC 地址
     *
     * @param netIKey 网络接口标识符（如 "eth" 或 "wlan0"）
     * @return 指定接口的 MAC 地址，若无法获取则返回 null
     */
    public static String getMacByInterface(String netIKey) {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            List<NetworkInterface> all = Collections.list(enumeration);

            // 遍历网络接口，查找符合条件的 MAC 地址
            for (NetworkInterface networkInterface : all) {
                if (networkInterface.getName().toLowerCase().contains(netIKey)) {
                    byte[] macBytes = networkInterface.getHardwareAddress();
                    if (macBytes != null) {
                        StringBuilder sb = new StringBuilder();
                        for (byte b : macBytes) {
                            sb.append(String.format("%02X:", b));
                        }
                        if (sb.length() > 0) {
                            sb.deleteCharAt(sb.length() - 1); // 去掉最后的冒号
                        }
                        return sb.toString();
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 Wi-Fi 接口的 MAC 地址
     *
     * @return Wi-Fi 的 MAC 地址，若无法获取或 MAC 地址无效则返回空字符串
     */
    @SuppressLint("HardwareIds")
    public static String getWifiMac() {
        WifiInfo wifiInfo = null;
        try {
            WifiManager wifiManager = DeviceContext.getWifiManager();
            wifiInfo = wifiManager.getConnectionInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (wifiInfo == null) {
            return "";
        } else {
            String mac = wifiInfo.getMacAddress();
            // 如果 MAC 地址为默认值（02:00:00:00:00:00），则返回空字符串
            return mac != null && !"02:00:00:00:00:00".equals(mac) ? mac : "";
        }
    }

    /**
     * 获取 wlan0 接口的 MAC 地址
     *
     * @return wlan0 接口的 MAC 地址，若无法获取则返回空字符串
     */
    public static String getWlanMac() {
        String macSerial = null;
        String str = "";
        try {
            // 通过命令获取 wlan0 接口的 MAC 地址
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            while (null != str) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();
                    break;
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return macSerial != null && macSerial.length() == 17 ? macSerial.toUpperCase() : "";
    }


    /**
     * 获取设备的 CPU 架构
     *
     * @return CPU 架构
     */
    public static String getCpuArch() {
        return Build.CPU_ABI;
    }


    /**
     * 获取设备的 Android ID
     * <p>
     * Android ID 是设备在首次启动时会生成的一个64位十六进制字符串，
     * 对同一设备通常是唯一且稳定的，
     * 但在恢复出厂设置或刷机后可能会变化。
     * </p>
     * 注意：部分厂商定制系统可能会影响其唯一性。
     *
     * @return Android ID
     */
    public static String getAndroidID() {
        return Settings.System.getString(DeviceContext.getApplication().getContentResolver(), Settings.System.ANDROID_ID);
    }
}