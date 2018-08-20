package com.lzp.manager.net;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.yanzhenjie.permission.AndPermission;

import java.util.ArrayList;
import java.util.List;

/**
 * 网络状态切换
 *
 * @author li.zhipeng
 */
public class NetworkBroadCast extends BroadcastReceiver {

    @SuppressLint("StaticFieldLeak")
    private static Context applicationContext;

    /**
     * 是否已经完成了网络监听的注册
     */
    private static volatile boolean hasRegisterNetwork;

    @SuppressLint("StaticFieldLeak")
    private static NetworkBroadCast instance;

    private static synchronized NetworkBroadCast getInstance() {
        instance = new NetworkBroadCast();
        return instance;
    }

    private static synchronized void clearInstance() {
        instance = null;
    }

    /**
     * 保留所有需要监听监听网络的回调函数
     */
    private static final ArrayList<NetEventHandler> mListeners = new ArrayList<>();

    /**
     * 添加网络监听函数
     */
    private static synchronized void addListener(NetEventHandler netEventHandler) {
        if (!mListeners.contains(netEventHandler)) {
            mListeners.add(netEventHandler);
        }
    }

    /**
     * 移除网络监听函数
     */
    private static synchronized void removeListener(NetEventHandler netEventHandler) {
        mListeners.remove(netEventHandler);
    }

    /**
     * 注册广播
     *
     * @param context         context上下文
     * @param netEventHandler 网络状态的回调函数
     */
    public static void register(final Context context, final NetEventHandler netEventHandler) {
        // 判断是否已经注册了网络监听
        // 如果已经注册了，直接添加监听函数即可
        Log.e("lzp", "添加网络监听");
        if (hasRegisterNetwork) {
            mListeners.add(netEventHandler);
            return;
        }

        Log.e("lzp", "开始注册广播");
        // 需要注册广播
        applicationContext = context.getApplicationContext();
        // 判断是否拥有权限
        if (AndPermission.hasPermission(context,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET)) {
            //安卓5.0以上使用新的api进行监听
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 据说在个别机型上有问题，我们做一个异常处理
                try {
                    registerAboveLOLLIPOP();
                } catch (Exception e) {
                    registerNormal();
                }
            } else {
                registerNormal();
            }
            // 已经完成网络监听的广播
            hasRegisterNetwork = true;
            // 添加回调函数
            mListeners.add(netEventHandler);
        }
    }

    /**
     * 解除网络监听
     */
    public static void unregister(NetEventHandler netEventHandler) {
        if (hasRegisterNetwork) {
            Log.e("lzp", "移除网络监听");
            synchronized (mListeners) {
                removeListener(netEventHandler);
                // 如果注册的是普通的广播，当没有监听正在使用的时候，解绑广播
                if (mListeners.size() == 0) {
                    Log.e("lzp", "解除广播");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        try {
                            unregisterAboveLOLLIPOP();
                        } catch (Exception ignore) {
                            unregisterNormal();
                        }
                    } else {
                        unregisterNormal();
                    }
                    hasRegisterNetwork = false;
                }
            }
        }
    }

    /**
     * 安卓5.0注册网络监听
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void registerAboveLOLLIPOP() {
        ConnectivityManager
                connectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            connectivityManager.requestNetwork(new NetworkRequest.Builder().build(), callback);
        }
    }

    /**
     * 安卓5.0以上解除网络监听
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void unregisterAboveLOLLIPOP() {
        ConnectivityManager
                connectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(callback);
        }
    }

    /**
     * 普通网络监听注册
     */
    private static void registerNormal() {
        IntentFilter netFilter = new IntentFilter();
        netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        applicationContext.registerReceiver(getInstance(), netFilter);
    }

    /**
     * 普通网络监听解绑
     */
    private static void unregisterNormal() {
        applicationContext.unregisterReceiver(instance);
        // 清除单例
        clearInstance();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            setNetWordState();
        }
    }

    /**
     * 为所有的监听函数分发网络变化的状态
     */
    private static void setNetWordState() {
        // 获得当前的网络类型
        int netWorkType = NetUtils.INSTANCE.getNetworkType(applicationContext);
        // 通知接口完成加载
        if (mListeners.size() > 0) {
            // 对目前注册的监听做一个备份，防止并发操作list出现的异常
            List<NetEventHandler> arrLocal = (List<NetEventHandler>) mListeners.clone();
            for (int i = 0; i < arrLocal.size(); i++) {
                (arrLocal.get(i)).onNetChange(netWorkType);
            }
        }
    }

    /**
     *
     */
    private static ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            setNetWordState();
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            setNetWordState();
        }
    };

    public interface NetEventHandler {

        void onNetChange(int netWorkType);
    }
}