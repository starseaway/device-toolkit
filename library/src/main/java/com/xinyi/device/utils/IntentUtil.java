package com.xinyi.device.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.xinyi.device.DeviceContext;

import java.io.File;

/**
 * Intent 工具类
 *
 * @author 新一
 * @date 2025/4/3 9:18
 */
public class IntentUtil {

    private IntentUtil() { }

    /**
     * 获取打开App的意图
     *
     * @param packageName 包名
     */
    public static Intent getLaunchIntentForPackage(String packageName) {
        return DeviceContext.getPackageManager().getLaunchIntentForPackage(packageName);
    }

    /**
     * 获取安装Ap
     *
     * @param filePath  文件路径
     * @param authority 7.0及以上安装需要传入清单文件中的{@code <provider>}的authorities属性
     *                  <br><a href="https://developer.android.com/reference/android/support/v4/content/FileProvider.html">文件提供器官方文档的</a></br>
     * @return intent
     */
    public static Intent getInstallAppIntent(String filePath, String authority) {
        return getInstallAppIntent(new File(filePath), authority);
    }

    /**
     * 获取安装App
     *
     * @param file      文件
     * @param authority 7.0及以上安装需要传入清单文件中的{@code <provider>}的authorities属性
     *                  <br><a href="https://developer.android.com/reference/android/support/v4/content/FileProvider.html">文件提供器官方文档的</a></br>
     * @return intent
     */
    public static Intent getInstallAppIntent(File file, String authority) {
        if (file == null) return null;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data;
        String type = "application/vnd.android.package-archive";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            data = Uri.fromFile(file);
        } else {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            data = FileProvider.getUriForFile(DeviceContext.getApplication(), authority, file);
        }
        intent.setDataAndType(data, type);
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 获取卸载App的意图
     *
     * @param packageName 包名
     * @return intent
     */
    public static Intent getUninstallAppIntent(String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}
