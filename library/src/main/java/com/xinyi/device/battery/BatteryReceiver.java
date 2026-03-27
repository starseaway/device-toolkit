package com.xinyi.device.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

/**
 * 电量广播接收器
 *
 * @author 新一
 * @date 2025/8/19 16:39
 */
public class BatteryReceiver extends BroadcastReceiver {

    /**
     * 电量变化监听器
     */
    private final BatteryLevelListener mListener;

    public BatteryReceiver(BatteryLevelListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent == null || !Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            return;
        }

        // 当前电量
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        // 电量最大值
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        // 是否充电
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;

        if (mListener != null) {
            mListener.onBatteryLevelChanged(level, scale, isCharging);
        }
    }
}