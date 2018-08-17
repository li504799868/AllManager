package com.lzp.manager.download

import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap

/**
 * @author li.zhipeng
 * @date 2018/8/16
 *
 * 下载的文件的工具类
 */
object LDownloadManager {

    /**
     * 下载任务的集合
     */
    private val taskSet: ConcurrentHashMap<String, DownloadTask> = ConcurrentHashMap()

    /**
     * okhttpClient
     * */
    private var okHttpClient: OkHttpClient? = null

    fun init(okHttpClient: OkHttpClient) {
        this.okHttpClient = okHttpClient
    }

    /**
     * 是否正在下载
     */
    fun isDownloading(url: String): Boolean {
        return taskSet.containsKey(url)
    }

    /**
     *  创建下载任务，需要读写sd卡权限
     * @param url      下载连接
     * @param savePath 保存的路径
     */
    fun download(url: String, savePath: String): DownloadTask {
        if (okHttpClient == null) {
            throw IllegalStateException("the okhttp is null")
        }
        // 如果这个下载任务正在执行
        if (taskSet.containsKey(url)) {
            // 把参数的listener，绑定到任务中回调
            return taskSet[url]!!
        }
        // 创建下载任务
        val task = DownloadTask(okHttpClient!!, url, savePath).addListener(commonDownloadListener)
        // 添加到运行的任务列表中去
        taskSet[url] = task
        return task
    }

    /**
     * 公共操作的listener
     * */
    private val commonDownloadListener = object : OnDownloadListener {

        override fun onDownloadSuccess(url: String) {
        }

        override fun onDownloading(url: String, progress: Int) {
        }

        override fun onDownloadFailed(url: String, e: Exception) {
            taskSet.remove(url)
        }

    }

    interface OnDownloadListener {
        /**
         * 下载成功
         *
         * @param url 下载地址
         */
        fun onDownloadSuccess(url: String)

        /**
         * 下载的进度
         *
         * @param url      下载地址
         * @param progress 下载进度
         */
        fun onDownloading(url: String, progress: Int)

        /**
         * 下载失败
         *
         * @param url 下载地址
         * @param e   下载出现的异常
         */
        fun onDownloadFailed(url: String, e: Exception)
    }

}

