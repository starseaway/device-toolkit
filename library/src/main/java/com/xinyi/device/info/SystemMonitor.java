package com.xinyi.device.info;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;
import com.xinyi.device.DeviceContext;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控器
 *
 * <p> 提供 CPU、系统负载、电池等信息的获取 </p>
 *
 * @author 新一
 * @date 2025/4/3 11:27
 */
public class SystemMonitor {

    /**
     * 获取当前 CPU 使用率，范围 0~100
     */
    public static float getCpuUsage() {
        try {
            String[] cpuStats = new String[3];
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String line = reader.readLine();
            reader.close();
            if (line != null) {
                cpuStats = line.split(" ");
            }

            long idleTime = Long.parseLong(cpuStats[4]);
            long totalTime = 0;
            for (int i = 2; i < cpuStats.length; i++) {
                totalTime += Long.parseLong(cpuStats[i]);
            }

            return (float) (totalTime - idleTime) / totalTime * 100;
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
            return 0;
        }
    }

    /**
     * 获取 CPU 核心数
     */
    public static int getCpuCoreCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * 获取 CPU 架构
     */
    public static String getCpuAbi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "未知架构";
        } else {
            // SDK 19-20 使用 Build.CPU_ABI
            return Build.CPU_ABI != null ? Build.CPU_ABI : "未知架构";
        }
    }

    /**
     * 获取 CPU 型号
     */
    public static String getCpuModel() {
        try {
            String line;
            StringBuilder cpuInfo = new StringBuilder();
            Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) {
                cpuInfo.append(line).append(" ");
            }
            reader.close();
            return cpuInfo.toString();
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
            return "无法检索 CPU 信息";
        }
    }

    /**
     * 获取 CPU 最大频率（kHz），若失败返回 -1
     */
    public static long getCpuMaxFrequency() {
        return readCpuFreq("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
    }

    /**
     * 获取 CPU 最小频率（kHz），若失败返回 -1
     */
    public static long getCpuMinFrequency() {
        return readCpuFreq("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq");
    }

    /**
     * 获取 CPU 当前频率（kHz），若失败返回 -1
     */
    public static long getCpuCurrentFrequency() {
        return readCpuFreq("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
    }

    /**
     * 读取 CPU 频率（kHz）
     *
     * @param path 频率文件路径
     */
    private static long readCpuFreq(String path) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)))) {
            String line = reader.readLine();
            if (line != null) {
                return Long.parseLong(line.trim());
            }
        } catch (Exception exception) {
            Log.e(SystemMonitor.class.getSimpleName(), "读取 CPU 频率失败", exception);
        }
        return -1;
    }

    /**
     * 获取电池百分比
     */
    public static int getBatteryLevel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = DeviceContext.getBatteryManager();
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            return -1;
        }
    }

    /**
     * 是否在充电
     */
    public static boolean isCharging() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = DeviceContext.getApplication().registerReceiver(null, filter);
        if (batteryStatus == null) {
            return false;
        }
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
    }

    /**
     * 电池电压，单位 mV
     */
    public static int getBatteryVoltage() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = DeviceContext.getApplication().registerReceiver(null, filter);
        if (batteryStatus == null) {
            return -1;
        }
        return batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
    }

    /**
     * 电池温度，单位 °C
     */
    public static int getBatteryTemperature() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = DeviceContext.getApplication().registerReceiver(null, filter);
        if (batteryStatus == null) {
            return -1;
        }
        return batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
    }

    /**
     * 获取系统负载，返回 1/5/15 分钟 load average
     */
    public static float[] getSystemLoadAverage() {
        float[] load = new float[3];
        try (RandomAccessFile reader = new RandomAccessFile("/proc/loadavg", "r")) {
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split(" ");
                load[0] = Float.parseFloat(parts[0]);
                load[1] = Float.parseFloat(parts[1]);
                load[2] = Float.parseFloat(parts[2]);
            }
        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }
        return load;
    }

    /**
     * 获取系统概览信息
     *
     * @return Map<String,Object> 包含 CPU、电池、系统负载、设备信息
     */
    public static Map<String, Object> getSystemSummary() {
        Map<String, Object> summary = new HashMap<>();

        // CPU
        summary.put("cpuUsage", getCpuUsage());
        summary.put("cpuCoreCount", getCpuCoreCount());
        summary.put("cpuAbi", getCpuAbi());
        summary.put("cpuModel", getCpuModel());
        summary.put("cpuMaxFreq", getCpuMaxFrequency());
        summary.put("cpuMinFreq", getCpuMinFrequency());
        summary.put("cpuCurrentFreq", getCpuCurrentFrequency());

        // 电池
        summary.put("batteryLevel", getBatteryLevel());
        summary.put("isCharging", isCharging());
        summary.put("batteryVoltage", getBatteryVoltage());
        summary.put("batteryTemperature", getBatteryTemperature());

        // 系统负载
        summary.put("systemLoadAverage", getSystemLoadAverage());

        return summary;
    }
}