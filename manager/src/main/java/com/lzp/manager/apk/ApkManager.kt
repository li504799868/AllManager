package com.lzp.manager.apk

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.content.FileProvider
import java.io.File


/**
 * Created by li.zhipeng on 2018/8/17.
 *
 *      应用相关操作的的管理类
 */
object ApkManager {

    private const val INSTALL_TYPE = "application/vnd.android.package-archive"

    private const val REQUEST_INSTALL_APP = 999

    /**
     * 安装指定路径的apk文件
     *
     * 8.0以上请手动申请权限 Manifest.permission.REQUEST_INSTALL_PACKAGES
     * */
    fun installApk(context: Context, file: File) {
        // 判断权限
        if (!hasInstallApkPermission(context)) {
            return
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // 判断
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            getUriByNougat(context, file)
        } else {
            Uri.fromFile(file)
        }
        intent.setDataAndType(uri, INSTALL_TYPE)
        context.startActivity(intent)
    }

    /**
     * 获取android 7.0以上加密后的Uri
     *
     * */
    @RequiresApi(Build.VERSION_CODES.N)
    fun getUriByNougat(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, context.packageName + ".file.fileProvider", file)
    }

    /**
     * 判断是否安装了指定包名的app
     * */
    @Suppress("DEPRECATION")
    fun hashInstallApk(context: Context, packageName: String): Boolean {
        return try {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.MATCH_UNINSTALLED_PACKAGES)
            } else {
                context.packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.GET_UNINSTALLED_PACKAGES)
            }
            info != null
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * 是否允许安装apk，android 8.0增加了安装apk的权限申请
     *
     * 如果小于android 8.0 直接返回true，否则需要通过系统api进行判断
     * */
    fun hasInstallApkPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O
                || context.packageManager.canRequestPackageInstalls()
    }

    /**
     * 跳转到系统允许安装未知app的页面
     */
    fun goSystemInstallUnknownApk(activity: Activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            activity.startActivityForResult(intent, REQUEST_INSTALL_APP)
        }
    }

    /**
     * 打开已安装的app
     */
    fun launchApp(context: Context, packageName: String, activityName: String) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val cn = ComponentName(packageName, activityName)
        intent.component = cn
        context.startActivity(intent)
    }

    /**
     * 卸载指定的app
     * */
    fun uninstallApk(context: Context, packageName: String) {
        val uri = Uri.parse("package:$packageName")
        val intent = Intent(Intent.ACTION_DELETE, uri)
        context.startActivity(intent)
    }

}