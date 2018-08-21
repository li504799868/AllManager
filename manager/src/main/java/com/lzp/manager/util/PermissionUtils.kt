package com.lzp.manager.util

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.AppOpsManagerCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.lzp.manager.R

/**
 * Created by li.zhipeng on 2018/8/21.
 *
 *      权限相关工具类
 */
object PermissionUtils {

    /**
     * 手机型号
     */
    enum class PhoneType(val type: String) {
        /**
         * 魅族
         */
        MEIZU("meizu"),

        HUAWEI("huawei"),

        OPPO("oppo"),

        XIAOMI("xiaomi"),

        VIVO("vivo");
    }

    /**
     * 判断是否有指定的权限
     * */
    fun hasPermission(context: Context, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        for (permission in permissions) {
            var result = ContextCompat.checkSelfPermission(context, permission)
            if (result == PackageManager.PERMISSION_DENIED) return false

            val op = AppOpsManagerCompat.permissionToOp(permission)
            if (TextUtils.isEmpty(op)) continue
            result = AppOpsManagerCompat.noteProxyOp(context, op!!, context.packageName)
            if (result != AppOpsManagerCompat.MODE_ALLOWED) return false

        }
        return true
    }

    /**
     * 判断通知权限是否已经打开
     * */
    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return true
        }
        val checkOpNoThrow = "checkOpNoThrow"
        val opPostNotification = "OP_POST_NOTIFICATION"

        val mAppOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val appInfo = context.applicationInfo
        val pkg = context.applicationContext.packageName
        val uid = appInfo.uid

        val appOpsClass: Class<*>
        try {
            appOpsClass = Class.forName(AppOpsManager::class.java.name)
            val checkOpNoThrowMethod = appOpsClass.getMethod(checkOpNoThrow, Integer.TYPE, Integer.TYPE,
                    String::class.java)
            val opPostNotificationValue = appOpsClass.getDeclaredField(opPostNotification)

            val value = opPostNotificationValue.get(Int::class.java) as Int
            return checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) as Int == AppOpsManager.MODE_ALLOWED

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }


    /**
     * 按照不同机型，打开设置权限页面v1
     *
     * @param permission 权限，如果是通知权限可能需要单独处理
     */
    fun gotoPermissionActivity(context: Context, permission: String) {
        val phoneType = getMobileType().toLowerCase()
        // 魅族测试通过
        if (phoneType.contains(PhoneType.MEIZU.type)) {
            goToCommonAppSetting(context)
        } else if (phoneType.contains(PhoneType.VIVO.type)) {
            // 如果是通知权限，打开系统的设置页面
            if (permission == UIUtils.getString(context, R.string.permission_notification)) {
                goToCommonAppSetting(context)
            } else {
                val intent = Intent()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val comp = ComponentName("com.iqoo.secure", "com.iqoo.secure.MainGuideActivity")
                intent.component = comp
                // 如果有这个activity，继续跳转
                if (findActivity(context, intent)) {
                    context.startActivity(intent)
                } else {
                    goToCommonAppSetting(context)
                }
            }// 跳转到vivo的i管家app
        } else if (phoneType.contains(PhoneType.HUAWEI.type)) {
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val comp: ComponentName
            // 如果是通知权限，打开系统的设置页面
            if (permission == UIUtils.getString(context, R.string.permission_notification)) {
                comp = ComponentName("com.huawei.systemmanager", "com.huawei.notificationmanager.ui.NotificationManagmentActivity")
            } else {
                comp = ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity")
            }// 跳转到vivo的i管家app
            intent.component = comp
            // 如果有这个activity，继续跳转
            if (findActivity(context, intent)) {
                context.startActivity(intent)
            } else {
                goToCommonAppSetting(context)
            }
        } else if (phoneType.contains(PhoneType.OPPO.type)) {
            // 如果是通知权限，打开系统的设置页面
            if (permission == UIUtils.getString(context, R.string.permission_notification)) {
                goToCommonAppSetting(context)
            } else {
                val intent = Intent()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val comp = ComponentName("com.color.safecenter",
                        "com.color.safecenter.permission.PermissionManagerActivity")
                intent.component = comp
                // 如果有这个activity，继续跳转
                if (findActivity(context, intent)) {
                    context.startActivity(intent)
                } else {
                    goToCommonAppSetting(context)
                }
            }// 跳转到权限管理页面
        } else {
            goToCommonAppSetting(context)
        }// 小米需要继续测试
        //        else if (phoneType.contains(UGConstants.PhoneType.XIAOMI)){
        //            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        //            ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        //            intent.setComponent(componentName);
        //            intent.putExtra("extra_pkgname", BuildConfig.APPLICATION_ID);
        //            context.startActivity(intent);
        //        }
        // 其他机型
        // oppo手机测试通过
        // 华为手机测试通过
        // Vivo手机测试通过
    }

    /**
     * 手机型号
     *
     * @return 手机型号
     */
    fun getMobileType(): String {
        return android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
    }

    /**
     * 检查是否这个activity
     */
    private fun findActivity(context: Context, intent: Intent): Boolean {
        val packageManager = context.packageManager
        return packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
    }

    /**
     * 通用跳转到应用设置页面
     */
    fun goToCommonAppSetting(context: Context) {
        val packageURI = Uri.parse("package:" + context.packageName)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
        context.startActivity(intent)
    }


}