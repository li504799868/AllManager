package com.lzp.manager.net

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.lzp.manager.util.PermissionUtils

/**
 * Created by li.zhipeng on 2018/6/28.
 */
object NetUtils {

    /**
     * 无网络
     * */
    const val NETWORK_NONE = -1

    /**
     *  获得当前的手机联网的类型
     *
     *  @return NetWorkType 当前网络连接的类型
     * */
    fun getNetworkType(context: Context): Int {
        // 检查一下权限
        if (PermissionUtils.hasPermission(context,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.INTERNET)) {
            val connManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            // 得到可用的网络,  如果返回时空对象，直接返回无网络
            // Wifi
            var state: NetworkInfo.State = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    .state
            if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                return ConnectivityManager.TYPE_WIFI
            }

            // 3G//4G
            state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                    .state
            if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                return ConnectivityManager.TYPE_MOBILE
            }
        }
        return NETWORK_NONE
    }
}