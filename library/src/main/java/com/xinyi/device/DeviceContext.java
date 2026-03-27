package com.xinyi.device;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

/**
 * 全局上下文管理类
 *
 * <p>
 *   由于本工具模块中多个工具类依赖 Context，因此提供一个全局 Application 级上下文，
 *   避免在各个工具类中重复传递 Context，提高代码的简洁性和可维护性
 * </p>
 *
 * @author 新一
 * @date 2025/4/2 19:00
 */
public class DeviceContext {

    /**
     * 全局上下文
     */
    private static Application sApplication = null;

    /**
     * 网络连接管理器
     */
    private static ConnectivityManager mConnectivityManager;

    /**
     * 系统的 Wi-Fi 管理器
     */
    private static WifiManager mWifiManager;

    /**
     * 检查 AppContext 是否已初始化
     *
     * @return true - 已初始化，false - 未初始化
     */
    public static boolean isInitialized() {
        return sApplication != null;
    }

    /**
     * 初始化全局上下文
     *
     * @param mainContext 上下文
     */
    public static void init(Context mainContext) {
        sApplication = (Application) mainContext.getApplicationContext();
    }

    /**
     * 获取全局上下文
     *
     * @return 全局上下文
     */
    public static Application getApplication() {
        if (sApplication == null) {
            throw new NullPointerException("Application 为 null，请先调用 DeviceContext.init() 初始化");
        }
        return sApplication;
    }

    /**
     * 获取包名
     */
    public static String getPackageName() {
        return getApplication().getPackageName();
    }

    /**
     * 获取全局 Resources
     *
     * @return 资源管理器
     */
    public static Resources getResources() {
        return getApplication().getResources();
    }

    /**
     * 获取系统服务
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSystemService(String name) {
        return (T) getApplication().getSystemService(name);
    }

    /**
     * 获取 WindowManager
     *
     * @return WindowManager 实例
     */
    public static WindowManager getWindowManager() {
        return getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * 获取 ActivityManager
     *
     * @return ActivityManager 实例
     */
    public static ActivityManager getActivityManager() {
        return getSystemService(Context.ACTIVITY_SERVICE);
    }

    /**
     * 获取包管理器
     */
    public static PackageManager getPackageManager() {
        return getApplication().getPackageManager();
    }

    /**
     * 获取网络连接管理器
     */
    public static ConnectivityManager getConnectivityManager() {
        if (mConnectivityManager == null) {
            mConnectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return mConnectivityManager;
    }

    /**
     * 获取电话管理器
     */
    public static TelephonyManager getTelephonyManager() {
        return getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * 获取Wi-Fi 管理器
     */
    public static WifiManager getWifiManager() {
        if (mWifiManager == null) {
            mWifiManager = DeviceContext.getSystemService(Context.WIFI_SERVICE);
        }
        return mWifiManager;
    }

    /**
     * 启动指定的 Activity
     *
     * @param intent 要启动的 Intent
     */
    public static void startActivity(Intent intent) {
        getApplication().startActivity(intent);
    }

    /**
     * 获取电池管理器
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static BatteryManager getBatteryManager() {
        return getSystemService(Context.BATTERY_SERVICE);
    }
}