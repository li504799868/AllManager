package com.lzp.manager.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.StringRes
import android.widget.EditText

/**
 * Created by li.zhipeng on 2018/8/21.
 */
object UIUtils {

    /**
     * 根据资源名称获取对应的资源ID
     */
    private fun getResourcesFromName(context: Context, drawableName: String): Int {
        return context.resources.getIdentifier(drawableName, "drawable",
                context.packageName)
    }

    /**
     * 资源名称转Drawable
     */
    fun getDrawableFromName(context: Context, drawableName: String): Drawable {
        return UIUtils.getDrawable(context, UIUtils.getResourcesFromName(context, drawableName))
    }


    /**
     * 根据资源ID转Drawable
     */
    @Suppress("DEPRECATION")
    fun getDrawable(context: Context, id: Int): Drawable {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getDrawable(id, context.theme)
        } else {
            context.resources.getDrawable(id)
        }
    }

    /**
     * 获取字符串
     */
    fun getString(context: Context, @StringRes id: Int): String {
        return context.resources.getString(id)
    }

    /**
     * 获取字符串
     */
    fun getString(context: Context, id: Int, vararg arg: Any): String {
        return context.resources.getString(id, arg)
    }


    /**
     * 获取颜色
     */
    @Suppress("DEPRECATION")
    fun getColor(context: Context, id: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getColor(id, context.theme)
        } else {
            context.resources.getColor(id)
        }
    }

    /**
     * drawable 转 bitmap
     * 注意！此方法转换的出的 Bitmap 为 565 格式，没有透明度。
     *
     * @param drawableId 资源 id
     * @return bitmap
     */
    fun drawableToBitmap(context: Context, drawableId: Int): Bitmap {
        val drawable = getDrawable(context, drawableId)
        return drawableToBitmap(drawable)
    }

    /**
     * drawable 转 bitmap
     * 注意！此方法转换的出的 Bitmap 为 565 格式，没有透明度。
     *
     * @param drawable Drawable
     * @return bitmap
     */
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                if (drawable.opacity != PixelFormat.OPAQUE)
                    Bitmap.Config.RGB_565
                else
                    Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 获得输入框的文字
     */
    fun getEditText(editText: EditText): String {
        return editText.text.toString().trim { it <= ' ' }
    }

}