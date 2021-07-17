package me.shetj.sdk.video.player

import me.shetj.sdk.video.PlayerDef
import me.shetj.sdk.video.model.PlayImageSpriteInfo
import me.shetj.sdk.video.model.PlayKeyFrameDescInfo
import me.shetj.sdk.video.model.VideoQuality

/**
 * 播放回调
 */
interface SuperPlayerObserver {
    /**
     * 开始播放
     * @param name 当前视频名称
     */
    fun onPlayBegin()

    /**
     * 播放暂停
     */
    fun onPlayPause()

    /**
     * 播放器停止
     */
    fun onPlayStop()

    /**
     * 播放器进入Loading状态
     */
    fun onPlayLoading()

    /**
     * 播放完成
     */
    fun onPlayComplete()

    /**
     * 视频宽高的变化
     */
    fun onVideoSize(width: Int, height: Int)

    /**
     * 播放进度回调
     *
     * @param current
     * @param duration
     */
    fun onPlayProgress(current: Long, duration: Long)


    fun onSeek(position: Int)

    fun onSwitchStreamStart(success: Boolean, playerType: PlayerDef.PlayerType, quality: VideoQuality)

    fun onSwitchStreamEnd(success: Boolean, playerType: PlayerDef.PlayerType, quality: VideoQuality?)

    fun onError(code: Int, message: String?)

    fun onPlayerTypeChange(playType: PlayerDef.PlayerType?)

    fun onPlayTimeShiftLive(player: SuperPlayer?, url: String?)

    fun onVideoQualityListChange(
        videoQualities: ArrayList<VideoQuality>?,
        defaultVideoQuality: VideoQuality?
    )

    fun onVideoImageSpriteAndKeyFrameChanged(
        info: PlayImageSpriteInfo?,
        list: ArrayList<PlayKeyFrameDescInfo>?
    )

}