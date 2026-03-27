package com.xinyi.device.app;

import static com.xinyi.device.utils.SystemUtil.killProcess;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.xinyi.device.DeviceContext;
import com.xinyi.device.utils.IntentUtil;
import com.xinyi.device.utils.ShellUtil;

import java.io.File;
import java.util.List;

/**
 * 应用管理入口，封装常见的 App 级操作能力
 *
 * @author 新一
 * @date 2024/11/7 11:33
 */
public class AppManager {

    private AppManager() { }

    /**
     * 获取应用版本名称
     *
     * @return 版本名称，如 "1.0.0"
     */
    public static String getAppVersionName() {
        try {
            Context context = DeviceContext.getApplication();
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException exception) {
            exception.printStackTrace(System.err);
            return "unknown";
        }
    }

    /**
     * 获取应用版本号（Version Code）
     *
     * @return 版本号，如 100
     */
    public static int getAppVersionCode() {
        try {
            Context context = DeviceContext.getApplication();
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException exception) {
            exception.printStackTrace(System.err);
            return -1;
        }
    }

    /**
     * 判断App是否安装
     *
     * @param packageName 包名
     * @return {@code true}: 已安装<br>{@code false}: 未安装
     */
    public static boolean isInstallApp(@NonNull String packageName) {
        return IntentUtil.getLaunchIntentForPackage(packageName) != null;
    }

    /**
     * 安装App
     *
     * @param filePath  文件路径
     */
    public static void installApp(@NonNull String filePath) {
        installApp(new File(filePath));
    }

    /**
     * 安装App
     *
     * @param file 文件
     */
    public static void installApp(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        String authority = DeviceContext.getPackageName() + ".provider";
        DeviceContext.startActivity(IntentUtil.getInstallAppIntent(file, authority));
    }

    /**
     * 静默安装App
     * <p>非root需添加权限 {@code <uses-permission android:name="android.permission.INSTALL_PACKAGES" />}</p>
     *
     * @param filePath 文件路径
     * @return {@code true}: 安装成功<br>{@code false}: 安装失败
     */
    public static ShellUtil.CommandResult installAppSilent(@NonNull String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ShellUtil.CommandResult(-1, null, "apk File not found");
        }
        String command = "pm install -r -d " + filePath;
        return ShellUtil.execCommand(command, ShellUtil.isCheckRoot());
    }

    /**
     * 卸载App
     */
    public static void uninstallApp() {
        uninstallApp(DeviceContext.getPackageName());
    }

    /**
     * 卸载App
     *
     * @param packageName 包名
     */
    public static void uninstallApp(@NonNull String packageName) {
        DeviceContext.startActivity(IntentUtil.getUninstallAppIntent(packageName));
    }

    /**
     * 卸载App
     *
     * @param activity activity
     */
    public static void uninstallApp(Activity activity, int requestCode) {
        uninstallApp(activity, requestCode, DeviceContext.getPackageName());
    }

    /**
     * 卸载App
     *
     * @param activity activity
     * @param requestCode 请求值
     * @param packageName 包名
     */
    public static void uninstallApp(Activity activity, int requestCode, @NonNull String packageName) {
        activity.startActivityForResult(IntentUtil.getUninstallAppIntent(packageName), requestCode);
    }

    /**
     * 静默卸载App
     * <p>非root需添加权限 {@code <uses-permission android:name="android.permission.DELETE_PACKAGES" />}</p>
     *
     * @param packageName 包名
     * @return {@code true}: 卸载成功<br>{@code false}: 卸载失败
     */
    public static ShellUtil.CommandResult uninstallAppSilent(@NonNull String packageName) {
        String command = "pm uninstall " + packageName;
        return ShellUtil.execCommand(command, ShellUtil.isCheckRoot());
    }

    /**
     * 判断应用是否在前台运行
     *
     * @param context 应用上下文
     * @return 如果应用在前台则返回true，否则返回false
     */
    public static boolean isAppInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();

        if (appProcesses == null) return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // 检查是否为当前应用并判断其是否在前台
            if (appProcess.processName.equals(context.getPackageName()) &&
                    appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }
    /**
     * 启动应用主活动的方法。如果应用在前台则不会启动
     */
    public static void launchApp() {
        // 如果应用不在前台，启动应用
        launchApp(DeviceContext.getApplication());
    }

    /**
     * 启动应用主活动的方法。如果应用在前台则不会启动
     *
     * @param context 应用上下文
     */
    public static void launchApp(Context context) {
        // 如果应用不在前台，启动应用
        if (!isAppInForeground(context)) {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            startAppIntent(launchIntent);
        }
    }

    /**
     * 启动应用主活动的方法
     */
    public static void openApp() {
        String packageName = DeviceContext.getApplication().getPackageName();
        openApp(packageName);
    }

    /**
     * 启动应用主活动的方法
     *
     * @param packageName 应用包名
     */
    public static void openApp(String packageName) {
        Context context = DeviceContext.getApplication();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        startAppIntent(launchIntent);
    }

    /**
     * 启动应用主活动的方法
     *
     * @param launchIntent 应用主活动的 Intent
     */
    public static void startAppIntent(Intent launchIntent) {
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 确保新任务栈启动
            DeviceContext.startActivity(launchIntent); // 启动主活动
        }
    }

    /**
     * 重新启动应用
     * <p>完全退出当前应用，并重新启动它。</p>
     */
    public static void restartApp() {
        openApp();
        killProcess();
    }

    /**
     * 关闭应用
     * <p>完全退出当前应用并结束进程。</p>
     */
    public static void exitApp() {
        killProcess();
    }
}