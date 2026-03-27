package com.xinyi.device.net;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.xinyi.device.DeviceContext;

/**
 * 网络状态监视器
 *
 * @author 新一
 * @date 2023/11/3 13:55
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class NetworkStatusMonitor {

    /**
     * 静态内部类的网络回调实例
     */
    private final NetworkCallbackImpl mNetworkCallback;

    public NetworkStatusMonitor(@NonNull OnNetworkStatusMonitor onNetworkStatusMonitor) {
        this.mNetworkCallback = new NetworkCallbackImpl(onNetworkStatusMonitor);
    }

    /**
     * 注册网络回调监听器
     *
     * <p> {@code registerNetworkCallback} 仅支持 Android 5.0 以上的版本</p>
     *
     * <p> 注意：5.0以下建议注册广播接收器实现，静态广播/动态广播皆可 </p>
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void register() {
        // 创建一个 NetworkRequest.Builder 对象，并添加网络连接能力
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        // 注册网络回调监听器
        DeviceContext.getConnectivityManager().registerNetworkCallback(request, mNetworkCallback);
    }

    /**
     * 取消注册网络回调监听器
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void unregister() {
        // 取消注册网络回调监听器
        DeviceContext.getConnectivityManager().unregisterNetworkCallback(mNetworkCallback);
    }

    /**
     * 网络状态回调的静态内部类实现
     * 避免持有外部类隐式引用，防止内存泄漏
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private static class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {

        /**
         * 网络状态回调
         */
        private final OnNetworkStatusMonitor mMonitor;

        public NetworkCallbackImpl(@NonNull OnNetworkStatusMonitor monitor) {
            this.mMonitor = monitor;
        }

        /**
         * 网络连接可用时调用
         */
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            mMonitor.networkIsConnected(network);
        }

        /**
         * 网络连接断开时调用
         */
        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            mMonitor.networkHasBeenDisconnected(network);
        }
    }

    /**
     * 网络状态回调
     */
    public interface OnNetworkStatusMonitor {

        /**
         * 网络已连接
         */
        void networkIsConnected(@NonNull Network network);

        /**
         * 网络已断开
         */
        void networkHasBeenDisconnected(@NonNull Network network);
    }
}