package com.xinyi.device.battery;

/**
 * 电量变化监听器接口
 *
 * @author 新一
 * @date 2025/8/19 16:31
 */
public interface BatteryLevelListener {

    /**
     * 当电量发生变化时回调
     *
     * @param level 当前电量值
     * @param scale 电量最大值
     * @param isCharging 是否处于充电状态
     */
    void onBatteryLevelChanged(int level, int scale, boolean isCharging);
}