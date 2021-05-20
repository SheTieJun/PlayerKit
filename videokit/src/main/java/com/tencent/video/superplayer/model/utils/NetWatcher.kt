package com.tencent.video.superplayer.model.utils

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import com.tencent.rtmp.TXLivePlayer
import com.tencent.rtmp.TXLog
import java.lang.ref.WeakReference

/**
 * 网络质量监视工具
 *
 * 当loading次数大于等于3次时，提示用户切换到低清晰度
 */
class NetWatcher(context: Context?) {
    private val mContext: WeakReference<Context?> = WeakReference(context)
    private var mLivePlayer // 直播播放器
            : WeakReference<TXLivePlayer?>? = null
    private var mPlayURL: String? = "" // 播放的url
    private var mLoadingCount = 0 // 记录loading次数
    private var mLoadingTime: Long = 0 // 记录单次loading的时长
    private var mLoadingStartTime: Long = 0 // loading开始的时间
    private var mWatching // 是否正在监控
            = false

    /**
     * 开始监控网络
     *
     * @param playUrl 播放的url
     * @param player  播放器
     */
    fun start(playUrl: String?, player: TXLivePlayer?) {
        mWatching = true
        mLivePlayer = WeakReference(player)
        mPlayURL = playUrl
        mLoadingCount = 0
        mLoadingTime = 0
        mLoadingStartTime = 0
        TXLog.w("NetWatcher", "net check start watch ")
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.postDelayed({
            TXLog.w(
                "NetWatcher",
                "net check loading count = $mLoadingCount loading time = $mLoadingTime"
            )
            if (mLoadingCount >= MAX_LOADING_COUNT || mLoadingTime >= MAX_LOADING_TIME) {
                showSwitchStreamDialog()
            }
            mLoadingCount = 0
            mLoadingTime = 0
        }, WATCH_TIME.toLong())
    }

    /**
     * 停止监控
     */
    fun stop() {
        mWatching = false
        mLoadingCount = 0
        mLoadingTime = 0
        mLoadingStartTime = 0
        mPlayURL = ""
        mLivePlayer = null
        TXLog.w("NetWatcher", "net check stop watch")
    }

    /**
     * 开始loading计时
     */
    fun enterLoading() {
        if (mWatching) {
            mLoadingCount++
            mLoadingStartTime = System.currentTimeMillis()
        }
    }

    /**
     * 结束loading计时
     */
    fun exitLoading() {
        if (mWatching) {
            if (mLoadingStartTime != 0L) {
                mLoadingTime += System.currentTimeMillis() - mLoadingStartTime
                mLoadingStartTime = 0
            }
        }
    }

    /**
     * 弹出切换清晰度的提示框
     */
    private fun showSwitchStreamDialog() {
        val context = mContext.get() ?: return
        val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setMessage("检测到您的网络较差，建议切换清晰度")
        alertDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, which ->
            val player = if (mLivePlayer != null) mLivePlayer!!.get() else null
            val videoUrl = mPlayURL!!.replace(".flv", "_900.flv")
            if (player != null && !TextUtils.isEmpty(videoUrl)) {
                val result = player.switchStream(videoUrl)
                if (result < 0) {
                    Toast.makeText(context, "切换高清清晰度失败，请稍候重试", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "正在为您切换为高清清晰度，请稍候...", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        alertDialog.show()
    }

    companion object {
        private const val WATCH_TIME = 30000 // 监控总时长ms
        private const val MAX_LOADING_TIME = 10000 // 一次loading的判定时长ms
        private const val MAX_LOADING_COUNT = 3 // 弹出切换清晰度提示框的loading总次数
    }

}