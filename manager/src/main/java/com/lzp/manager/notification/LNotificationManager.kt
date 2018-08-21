package com.lzp.manager.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import com.lzp.manager.util.PermissionUtils

/**
 * Created by li.zhipeng on 2018/8/17.
 *
 *
 *  发送通知的管理类
 *
 *  如果要适配8.0，最好先通过init方法直接设置好所有的渠道
 */
object LNotificationManager {

    /**
     * 指定Channel，发送通知
     * */
    fun sendNotification(context: Context, notification: Notification, notifyId: Int) {
        // 判断是否有发送通知的全新啊
        if (!PermissionUtils.hasNotificationPermission(context)){
            return
        }

        val mNotificationManager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 判断是否已经创建了对应的渠道
        // 判断是否是8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 如果当前没有创建这个Channel，创建这个Channel
            if (!containsChannel(mNotificationManager, notification.channelId)) {
                createNotificationChannelByDefault(mNotificationManager, notification.channelId)
            }
        }
        // 发送通知
        mNotificationManager.notify(notifyId, notification)
    }

    /**
     * 取消指定id的通知
     * */
    fun cancelNotification(context: Context, notifyId: Int) {
        val mNotificationManager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(notifyId)
    }

    /**
     * 创建Channel列表
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels(context: Context, channels: List<NotificationChannel>) {
        val mNotificationManager = context.applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 把渠道添加到NotificationManager中
        mNotificationManager.createNotificationChannels(channels)
    }

    /**
     * 创建NotificationChannel
     * */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(id: String, name: String, importance: Int): NotificationChannel {
        return NotificationChannel(id, name, importance)
    }

    /**
     * 创建指定的Channel
     * */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelByDefault(mNotificationManager: NotificationManager, channelId: String) {
        mNotificationManager.createNotificationChannel(
                NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH)
        )
    }

    /**
     * 判断是否已经创建指定Channel
     * */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun containsChannel(mNotificationManager: NotificationManager, channelId: String): Boolean {
        if (mNotificationManager.notificationChannels.isEmpty()) {
            return false
        }

        for (item in mNotificationManager.notificationChannels) {
            if (item.id == channelId) {
                return true
            }
        }

        return false
    }

}