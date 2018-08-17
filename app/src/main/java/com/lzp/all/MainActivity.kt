package com.lzp.all

import android.os.Bundle
import android.os.Environment
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.lzp.manager.apk.ApkManager
import com.lzp.manager.download.LDownloadManager
import com.lzp.manager.notification.LNotificationManager
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import java.io.File

class MainActivity : AppCompatActivity() {

    private val filePath = Environment.getExternalStorageDirectory().absolutePath + "/ugirls.apk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadTest()

        notificationTest()

        installTest()

        uninstallTest()
    }

    private fun downloadTest() {
        LDownloadManager.init(OkHttpClient.Builder().build())

        download.setOnClickListener {
            LDownloadManager.download(
                    "https://down-sns.youguoquan.com/pkg/youguoquan/app-v2.4.9-wapugirls.apk",
                    filePath)
                    .start(object : LDownloadManager.OnDownloadListener {

                        override fun onDownloadSuccess(url: String) {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "下载成功", Toast.LENGTH_SHORT).show()
                            }

                        }

                        override fun onDownloading(url: String, progress: Int) {
                            Log.e("lzp", "$progress")
                        }

                        override fun onDownloadFailed(url: String, e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "下载失败", Toast.LENGTH_SHORT).show()
                            }
                        }

                    })
        }
    }

    private fun notificationTest() {
        notification.setOnClickListener {
            // 第二个参数就是指定channelId
            val notification = NotificationCompat.Builder(
                    this@MainActivity, "test")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("test")
                    .setContentText("test")
                    .setTicker("test")
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()

            LNotificationManager.sendNotification(this@MainActivity, notification, 1)
        }
    }

    private fun installTest() {
        install.setOnClickListener {
            val file = File(filePath)
            if (!file.exists()){
                Toast.makeText(this, "请先下载文件", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ApkManager.installApk(this, file)
        }

    }

    private fun uninstallTest() {
        uninstall.setOnClickListener {
            if (ApkManager.hashInstallApk(this, "com.ugirls.app02")){
                ApkManager.uninstallApk(this, "com.ugirls.app02")
            }
        }
    }
}
