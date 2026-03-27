package com.xinyi.device.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Process;

import com.xinyi.device.DeviceContext;

import java.util.Locale;

/**
 * 系统工具类
 *
 * <p> 提供各种系统相关的工具方法，如返回主界面、获取应用版本、判断是否是模拟器等 </p>
 * 
 * @author 新一
 * @date 2025/4/2 15:21
 */
public class SystemUtil {

    private SystemUtil() { }

    /**
     * 返回主屏幕
     *
     * <p> 让应用进入后台，类似于按下 Home 键 </p>
     */
    public static void goToHomeScreen() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        DeviceContext.getApplication().startActivity(intent);
    }

    /**
     * 判断是否处于深色模式
     *
     * @return true 表示当前是深色模式
     */
    public static boolean isDarkMode() {
        int nightModeFlags = DeviceContext.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * 获取设备当前语言
     *
     * @return 设备当前语言，如 "zh-CN"（简体中文）
     */
    public static String getCurrentLanguage() {
        return Locale.getDefault().toString();
    }

    /**
     * 判断当前设备是否是模拟器
     *
     * @return true 如果是模拟器，false 不是
     */
    public static boolean isEmulator() {
        return Build.FINGERPRINT.contains("generic")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"));
    }

    /**
     * 跳转到系统设置界面
     *
     * @param context 上下文
     */
    public static void openSystemSettings(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 杀死当前进程
     */
    public static void killProcess() {
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
}