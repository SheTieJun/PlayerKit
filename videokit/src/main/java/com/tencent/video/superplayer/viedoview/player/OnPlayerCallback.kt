package com.tencent.video.superplayer.viedoview.player

import android.graphics.Bitmap

/**
 * SuperPlayerView的回调接口
 */
interface OnPlayerCallback {

    /**
     * 开始全屏播放
     */
    fun onStartFullScreenPlay()

    /**
     * 结束全屏播放
     */
    fun onStopFullScreenPlay()

    /**
     * 点击悬浮窗模式下的x按钮
     */
    fun onClickFloatCloseBtn()

    /**
     * 点击小播放模式的返回按钮
     */
    fun onClickSmallReturnBtn()

    /**
     * 开始悬浮窗播放
     */
    fun onStartFloatWindowPlay()


    /**
     *
     */
    fun onStopFloatWindow()
    /**
     * 点击分享
     */
    fun onClickShare()

    /**
     * 播放进度变化
     */
    fun onPlayProgress(current: Long, duration: Long)

    /**
     * 开始视频大小变化
     */
    fun onVideoSize(width: Int, height: Int)

    /**
     * 截图
     */
    fun onSnapshot(bitmap:Bitmap)

    /**
     * 加载中
     */
    fun onLoading()

    /**
     * 开始播放
     */
    fun onStart()

    /**
     * 播放暂停回调
     */
    fun onPause()

    /**
     * 播放继续回调
     */
    fun onResume()

    /**
     * 停止播放
     */
    fun onStop()
    /**
     * 播放完成
     */
    fun onComplete()

    /**
     * 播放跳转回调
     *
     * @param position 跳转的位置(秒)
     */
    fun onSeekTo(position: Int)

    /**
     * 恢复直播回调
     */
    fun onResumeLive()

    /**
     * 倍数变化
     */
    fun onSpeedChange(speed:Float)

    /**
     * 投屏按钮
     */
    fun onShotScreen()

    /**
     * 错误回调
     */
    fun onError(code: Int, message: String?)
}

