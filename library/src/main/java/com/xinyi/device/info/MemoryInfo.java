package com.xinyi.device.info;

import android.app.ActivityManager;
import android.text.format.Formatter;

import com.xinyi.device.DeviceContext;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 内存信息获取
 *
 * <p>
 *   提供获取设备总内存、可用内存、已用内存、内存使用百分比等方法，
 *   并提供内存低于阈值的判断方法，便于系统监控和优化。
 * </p>
 *
 * @author 新一
 * @date 2025/8/19 15:19
 */
public class MemoryInfo {

    /**
     * 获取当前可用内存，单位字节
     *
     * @return 可用内存，单位Byte
     */
    public static long getAvailableMemorySize() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = DeviceContext.getActivityManager();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    /**
     * 获取当前可用内存（格式化为可读单位）
     *
     * @return 可用内存字符串，例如 "123 MB"
     */
    public static String getAvailableMemory() {
        return Formatter.formatFileSize(DeviceContext.getApplication(), getAvailableMemorySize());
    }

    /**
     * 获取设备总内存，单位字节
     *
     * @return 总内存，单位Byte
     */
    public static long getTotalMemorySize() {
        String path = "/proc/meminfo";
        String memTotal = "0";
        try (RandomAccessFile reader = new RandomAccessFile(path, "r")) {
            String line = reader.readLine();
            if (line != null) {
                memTotal = line.split("\\s+")[1];
            }
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }
        return Long.parseLong(memTotal) * 1024;
    }

    /**
     * 获取设备总内存（格式化为可读单位）
     *
     * @return 总内存字符串，例如 "2 GB"
     */
    public static String getTotalMemory() {
        return Formatter.formatFileSize(DeviceContext.getApplication(), getTotalMemorySize());
    }

    /**
     * 获取已用内存，单位字节
     *
     * @return 已用内存，单位Byte
     */
    public static long getUsedMemorySize() {
        return getTotalMemorySize() - getAvailableMemorySize();
    }

    /**
     * 获取已用内存（格式化为可读单位）
     *
     * @return 已用内存字符串，例如 "512 MB"
     */
    public static String getUsedMemory() {
        return Formatter.formatFileSize(DeviceContext.getApplication(), getUsedMemorySize());
    }

    /**
     * 获取当前内存使用百分比
     *
     * @return 已用内存占总内存百分比，0~100
     */
    public static float getMemoryUsagePercentage() {
        long total = getTotalMemorySize();
        long used = getUsedMemorySize();
        return (float) used / total * 100;
    }

    /**
     * 判断可用内存是否低于指定字节阈值
     *
     * @param thresholdBytes 阈值字节数
     * @return true 表示可用内存低于阈值
     */
    public static boolean isMemoryLow(long thresholdBytes) {
        return getAvailableMemorySize() < thresholdBytes;
    }

    /**
     * 判断可用内存是否低于指定百分比
     *
     * @param thresholdPercent 阈值百分比，例如 10 表示 10%
     * @return true 表示可用内存低于阈值
     */
    public static boolean isMemoryLowByPercent(float thresholdPercent) {
        float usagePercent = getMemoryUsagePercentage();
        return (100 - usagePercent) < thresholdPercent;
    }

    /**
     * 获取应用占用内存，单位字节
     *
     * @return 当前应用占用的内存
     */
    public static long getAppMemoryUsage() {
        ActivityManager activityManager = DeviceContext.getActivityManager();
        int pid = android.os.Process.myPid();
        android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(new int[]{pid});
        return memoryInfoArray[0].getTotalPss() * 1024L; // 转为字节
    }

    /**
     * 获取应用占用内存（格式化为可读单位）
     *
     * @return 当前应用占用内存字符串
     */
    public static String getAppMemoryUsageFormatted() {
        return Formatter.formatFileSize(DeviceContext.getApplication(), getAppMemoryUsage());
    }
}