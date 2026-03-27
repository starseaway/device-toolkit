package com.xinyi.device.info;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import com.xinyi.device.DeviceContext;

import java.io.File;
import java.util.Locale;

/**
 * 设备存储空间的相关信息获取
 *
 * <p> 用于获取设备存储空间信息及判断存储状态，提供内部存储和外部存储的可用空间、总空间等功能 </p>
 *
 * <p>
 *   内部存储（Internal Storage）：设备自带的私有存储空间，通常在 /data/data/ 下。
 *   应用可以自由读写，但数据仅限该应用使用，卸载应用时会被清空。
 * </p>
 *
 * <p>
 *   外部存储（External Storage）：用户可访问的公共存储空间，例如 SD 卡或设备自带的共享存储。
 *   应用可以读写，但在 Android 6.0 及以上需要动态权限。
 *   外部存储可能被卸载或移除，所以在操作前最好先检查是否可用。
 * </p>
 *
 * @author 新一
 * @date 2025/8/19 15:04
 */
public class StorageInfo {

    /**
     * 获取内部存储总空间（字节）
     *
     * @return 内部存储总空间，单位字节
     */
    public static long getInternalTotalSpace() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
    }

    /**
     * 获取内部存储可用空间（字节）
     *
     * @return 内部存储可用空间，单位字节
     */
    public static long getInternalAvailableSpace() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    /**
     * 判断外部存储是否可用
     *
     * @return 是否可用
     */
    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取外部存储总空间（字节）
     *
     * @return 外部存储总空间，单位字节，若不可用则返回 -1
     */
    public static long getExternalTotalSpace() {
        if (isExternalStorageAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return totalBlocks * blockSize;
        } else {
            return -1;
        }
    }

    /**
     * 获取外部存储可用空间（字节）
     *
     * @return 外部存储可用空间，单位字节，若不可用则返回 -1
     */
    public static long getExternalAvailableSpace() {
        if (isExternalStorageAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }

    /**
     * 判断内部存储空间是否足够
     *
     * @param requiredBytes 所需空间，单位字节
     * @return 如果可用空间大于或等于所需空间返回 true，否则返回 false
     */
    public static boolean isInternalSpaceEnough(long requiredBytes) {
        return getInternalAvailableSpace() >= requiredBytes;
    }

    /**
     * 判断外部存储空间是否足够
     *
     * @param requiredBytes 所需空间，单位字节
     * @return 如果可用空间大于或等于所需空间返回 true，否则返回 false
     */
    public static boolean isExternalSpaceEnough(long requiredBytes) {
        long available = getExternalAvailableSpace();
        return available >= 0 && available >= requiredBytes;
    }

    /**
     * 判断内部存储空间是否小于指定阈值
     *
     * @param thresholdBytes 阈值字节数
     * @return 如果可用空间小于阈值返回 true，否则返回 false
     */
    public static boolean isInternalSpaceLow(long thresholdBytes) {
        return getInternalAvailableSpace() < thresholdBytes;
    }

    /**
     * 判断外部存储空间是否小于指定阈值
     *
     * @param thresholdBytes 阈值字节数
     * @return 如果可用空间小于阈值返回 true，否则返回 false
     */
    public static boolean isExternalSpaceLow(long thresholdBytes) {
        long available = getExternalAvailableSpace();
        return available >= 0 && available < thresholdBytes;
    }

    /**
     * 获取内部存储已用空间，单位字节
     */
    public static long getInternalUsedSpace() {
        return getInternalTotalSpace() - getInternalAvailableSpace();
    }

    /**
     * 获取外部存储已用空间，单位字节
     * 若外部存储不可用，返回 -1
     */
    public static long getExternalUsedSpace() {
        long total = getExternalTotalSpace();
        long available = getExternalAvailableSpace();
        if (total >= 0 && available >= 0) {
            return total - available;
        }
        return -1;
    }

    /**
     * 获取内部存储信息（格式化）
     *
     * @return String数组：[总空间, 已用空间, 可用空间]
     */
    public static String[] getInternalStorageInfo() {
        Context context = DeviceContext.getApplication();
        long total = getInternalTotalSpace();
        long used = getInternalUsedSpace();
        long free = getInternalAvailableSpace();
        return new String[]{
                Formatter.formatFileSize(context, total),
                Formatter.formatFileSize(context, used),
                Formatter.formatFileSize(context, free)
        };
    }

    /**
     * 获取外部存储信息（格式化）
     *
     * @return String数组：[总空间, 已用空间, 可用空间]，不可用返回 ["-","-","-"]
     */
    public static String[] getExternalStorageInfo() {
        Context context = DeviceContext.getApplication();
        long total = getExternalTotalSpace();
        long used = getExternalUsedSpace();
        long free = getExternalAvailableSpace();
        if (total < 0 || used < 0 || free < 0) {
            return new String[]{"-", "-", "-"};
        }
        return new String[]{
                Formatter.formatFileSize(context, total),
                Formatter.formatFileSize(context, used),
                Formatter.formatFileSize(context, free)
        };
    }

    /**
     * 将字节转换为更易读的单位
     *
     * @param bytes 字节数
     * @return 转换后的字符串，例如 "1.23 GB"
     */
    public static String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format(Locale.getDefault(), "%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}