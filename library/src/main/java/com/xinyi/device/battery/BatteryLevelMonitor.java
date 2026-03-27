package com.xinyi.device.battery;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import com.xinyi.device.DeviceContext;

/**
 * 电量监听器类
 *
 * <p> 用于监听 Android 系统电量变化，并通过回调接口将最新电量信息传递给外部调用者 </p>
 *
 * <p> 内部采用广播实现，请注意在合适的时机解绑注册，避免发生其他意外 </p>
 *
 * @author 新一
 * @date 2025/8/19 15:29
 */
public class BatteryLevelMonitor {

    /**
     * 电量变化监听器回调
     */
    private final BatteryLevelListener mListener;

    /**
     * 电量变化广播
     */
    private BroadcastReceiver mBatteryReceiver;

    /**
     * 构造方法
     *
     * @param listener 电量变化监听器回调
     */
    public BatteryLevelMonitor(BatteryLevelListener listener) {
        this.mListener = listener;
    }

    /**
     * 注册电量变化广播
     */
    public void register() {
        if (mBatteryReceiver != null) {
            return; // 已注册
        }
        mBatteryReceiver = new BatteryReceiver(mListener);
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        DeviceContext.getApplication().registerReceiver(mBatteryReceiver, filter);
    }

    /**
     * 注销电量变化广播
     */
    public void unregister() {
        if (mBatteryReceiver != null) {
            DeviceContext.getApplication().unregisterReceiver(mBatteryReceiver);
            mBatteryReceiver = null;
        }
    }
}