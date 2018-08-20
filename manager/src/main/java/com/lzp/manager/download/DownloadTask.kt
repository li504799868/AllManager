package com.lzp.manager.download

import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Created by li.zhipeng on 2018/8/16.
 *
 * 下载任务
 */
class DownloadTask constructor(private val okHttpClient: OkHttpClient, private val url: String, private val savePath: String) : Callback {

    /**
     * 回调监听
     * */
    private val listeners: ArrayList<LDownloadManager.OnDownloadListener> = ArrayList()

    /**
     * 添加监听函数
     * */
    fun addListener(listener: LDownloadManager.OnDownloadListener): DownloadTask {
        listeners.add(listener)
        return this
    }

    /**
     * 移除监听函数
     * */
    fun removeListener(listener: LDownloadManager.OnDownloadListener): DownloadTask {
        listeners.remove(listener)
        return this
    }

    /**
     * 下载成功
     */
    private fun onSuccess() {
        for (listener in listeners) {
            listener.onDownloadSuccess(url)
        }
    }

    /**
     * 下载失败
     */
    private fun onFailed(e: Exception) {
        // 下载失败
        for (listener in listeners) {
            listener.onDownloadFailed(url, e)
        }
    }

    /**
     * 下载的进度
     *
     * @param progress 下载进度
     */
    private fun onDownloading(progress: Int) {
        for (listener in listeners) {
            listener.onDownloading(url, progress)
        }
    }

    /**
     * @param savePath 保存的地址
     * @throws IOException 判断下载目录是否存在
     */
    private fun isExist(savePath: String): File {
        // 下载位置
        val downloadFile = File(savePath)
        if (!downloadFile.parentFile.exists()) {
            downloadFile.parentFile.mkdirs()
        }
        return downloadFile
    }

    override fun onFailure(call: Call, e: IOException) {
        onFailed(e)
    }

    override fun onResponse(call: Call, response: Response) {
        var `is`: InputStream? = null
        val buf = ByteArray(2048)
        var len: Int
        var fos: FileOutputStream? = null

        // 上一次的下载进度
        var tempProgress = 0
        try {
            val file = isExist(savePath)
            `is` = response.body()!!.byteStream()
            val total = response.body()!!.contentLength()
            fos = FileOutputStream(file)
            var sum: Long = 0
            while (true) {
                len = `is`!!.read(buf)
                if (len != -1) {
                    fos.write(buf, 0, len)
                    sum += len.toLong()
                    val progress = (sum * 1.0f / total * 100).toInt()
                    if (tempProgress != progress){
                        // 下载中
                        onDownloading(progress)
                        tempProgress = progress
                    }
                } else {
                    break
                }
            }
            fos.flush()
            // 下载完成
            onSuccess()
        } catch (e: Exception) {
            onFailed(e)
        } finally {
            try {
                `is`?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 开启任务
     * */
    fun start() {
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(this)
    }

    /**
     * 开启任务
     * */
    fun start(listener: LDownloadManager.OnDownloadListener) {
        addListener(listener)
        start()
    }

}