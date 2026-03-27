package com.xinyi.device.utils;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import com.xinyi.device.DeviceContext;

/**
 * 系统设置跳转工具类
 *
 * <p> 用于快速打开系统中的各类设置界面 </p>
 *
 * @author 新一
 * @date 2025/5/29 13:59
 */
public class SettingsNavigator {

    /**
     * 打开系统的某个设置界面
     */
    public static void openSystemSettings(String action) {
        try {
            Intent intent = new Intent(action);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            DeviceContext.getApplication().startActivity(intent);
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
    }

    /**
     * 打开系统的设置界面。
     */
    public static void openSystemSettings() {
        openSystemSettings(Settings.ACTION_SETTINGS);
    }

    /**
     * 打开系统的 Wi-Fi 设置界面。
     */
    public static void openWifiSettings() {
        openSystemSettings(Settings.ACTION_WIFI_SETTINGS);
    }

    /**
     * 打开系统的网络设置界面（包括飞行模式、数据网络等）。
     */
    public static void openNetworkSettings() {
        openSystemSettings(Settings.ACTION_WIRELESS_SETTINGS);
    }

    /**
     * 打开位置服务（GPS）设置界面。
     */
    public static void openLocationSettings() {
        openSystemSettings(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    }

    /**
     * 打开无线设置界面（通常包含 Wi-Fi、蓝牙、数据流量开关）。
     */
    public static void openWirelessSettings() {
        openSystemSettings(Settings.ACTION_WIRELESS_SETTINGS);
    }

    /**
     * 打开当前应用的系统应用详情页，用于权限管理等操作。
     */
    public static void openAppInfoSettings() {
        openAppInfoSettings(DeviceContext.getApplication().getPackageName());
    }

    /**
     * 打开指定包名应用的系统应用详情页。
     *
     * @param packageName  应用包名
     */
    public static void openAppInfoSettings(String packageName) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            DeviceContext.getApplication().startActivity(intent);
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
    }
}