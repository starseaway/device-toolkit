package com.xinyi.device.utils;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.WindowMetrics;

import com.xinyi.device.DeviceContext;

/**
 * Android屏幕尺寸获取 && 大小单位转换工具类
 *
 * @author 新一
 * @date 2022/4/28 19:37
 */
public class DensityUtil {

    private DensityUtil() { }

    /**
     * 获取设备屏幕的密度
     *
     * @return 屏幕密度（dpi）
     */
    public static float getScreenDensity() {
        return DeviceContext.getResources().getDisplayMetrics().density;
    }

    /**
     * 获取屏幕的缩放密度
     */
    public static float getScaledDensity() {
        return DeviceContext.getResources().getDisplayMetrics().scaledDensity;
    }

    /**
     * 获取屏幕的密度DPI
     */
    public static int getScreenDensityDpi() {
        return DeviceContext.getResources().getDisplayMetrics().densityDpi;
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     */
    public static int px2dip(float pxValue) {
     return (int)(pxValue / getScreenDensity() + 0.5F);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */
    public static int dip2px(float dipValue) {
        return (int) (dipValue * getScreenDensity() + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     */
    public static int px2sp(float pxValue) {
        return (int) (pxValue / getScaledDensity() + 0.5F);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public static int sp2px(float spValue) {
        return (int) (spValue * getScaledDensity() + 0.5F);
    }

    /**
     * 获取设备真实屏幕高度（单位：px），包含状态栏、导航栏等系统区域
     */
    public static int getRealScreenHeight() {
        WindowManager wm = DeviceContext.getWindowManager();
        if (wm == null) {
            return -1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics metrics = wm.getCurrentWindowMetrics();
            Rect bounds = metrics.getBounds();
            return bounds.height(); // 包含状态栏/导航栏
        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(metrics);
            return metrics.heightPixels; // 包含状态栏/导航栏
        }
    }

    /**
     * 获取设备真实屏幕宽度（单位：px）
     * <p>
     *
     * <br/>
     * 对于绝大多数设备，导航栏位于屏幕底部，因此宽度通常不会受到影响；
     * 但在极少数横屏设备上，导航栏可能位于屏幕侧边，此方法能正确获取完整宽度。
     * </p>
     */
    public static int getRealScreenWidth() {
        WindowManager wm = DeviceContext.getWindowManager();
        if (wm == null) {
            return -1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics metrics = wm.getCurrentWindowMetrics();
            Rect bounds = metrics.getBounds();
            return bounds.width(); // 包含系统装饰区域
        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(metrics);
            return metrics.widthPixels; // 包含系统装饰区域
        }
    }

    /**
     * 获取设备宽度（px）
     * <p>
     * 此方法返回的是应用可用区域的高度，不包含系统占用区域
     */
    public static int deviceWidth() {
        return DeviceContext.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取设备高度（px）
     * <p>
     * 此方法返回的是应用可用区域的高度，不包含状态栏和导航栏
     */
    public static int deviceHeight() {
        return DeviceContext.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 应用界面可见高度，可能不包含导航和状态栏，看Rom实现
     */
    public static int getAppHeight() {
        WindowManager wm = DeviceContext.getWindowManager();
        if (wm == null) {
            return -1;
        }
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        return point.y;
    }

    /**
     * 应用界面可见宽度，可能不包含系统占用区域，看Rom实现
     */
    public static int getAppWidth() {
        WindowManager wm = DeviceContext.getWindowManager();
        if (wm == null) {
            return -1;
        }
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        return point.x;
    }

    /**
     * 获取状态栏高度
     */
    @SuppressLint("InternalInsetResource") // 内部插图资源
    public static int getStatusBarHeight() {
        Resources resources = Resources.getSystem();
        @SuppressLint("DiscouragedApi")
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * 获取导航栏高度
     */
    @SuppressLint("InternalInsetResource") // 内部插图资源
    public static int getNavBarHeight() {
        Resources res = Resources.getSystem();
        @SuppressLint("DiscouragedApi")
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId != 0) {
            return res.getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }
}