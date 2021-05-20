package com.tencent.video.superplayer

import com.tencent.video.superplayer.model.entity.VideoQuality
import com.tencent.video.superplayer.ui.player.Player

/**
 * SuperPlayerView的回调接口
 */
interface OnSuperPlayerViewCallback {

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

}

