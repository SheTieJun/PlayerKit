package me.shetj.sdk.video.player

import me.shetj.sdk.video.base.IPlayerView
import me.shetj.sdk.video.model.PlayImageSpriteInfo
import me.shetj.sdk.video.model.PlayKeyFrameDescInfo
import me.shetj.sdk.video.model.VideoPlayerModel
import me.shetj.sdk.video.model.VideoQuality

interface IPlayer{
    /**
     * 开始播放
     *
     * @param url 视频地址
     */
    fun play(url: String?)

    /**
     * 多分辨率播放
     * @param appId             腾讯云视频appId
     * @param superPlayerURLS   不同分辨率数据
     * @param defaultIndex      默认播放Index
     */
    fun play(superPlayerURLS: List<VideoPlayerModel.PlayerURL>?, defaultIndex: Int)

    /**
     * 重播
     */
    fun reStart()

    /**
     * 暂停播放
     */
    fun pause()

    /**
     * 恢复播放
     */
    fun resume()

    /**
     * 恢复直播播放，从直播时移播放中，恢复到直播播放。
     */
    fun resumeLive()

    /**
     * 是否自定开始播放
     */
    fun autoPlay(auto:Boolean)

    /**
     * 是否循环
     */
    fun setLoopPlay(isLoop:Boolean)

    fun isLoop():Boolean

    fun getVideoWidth():Int

    fun getVideoHeight():Int

    fun getVideoRotation() :Int
    /**
     * 停止播放
     */
    fun stop()

    /**
     * 销毁播放器
     */
    fun destroy()

    /**
     * 切换播放器模式
     *
     * @param playerMode [PlayerMode.WINDOW]          窗口模式
     * [PlayerMode.FULLSCREEN]      全屏模式
     * [PlayerMode.FLOAT]           悬浮窗模式
     */
    fun switchPlayMode(playerMode: PlayerDef.PlayerMode)
    fun enableHardwareDecode(enable: Boolean)
    fun setPlayerView(videoView: IPlayerView)
    fun seek(position: Int,isCallback:Boolean = true)
    fun setPlayToSeek(position: Int)
    fun snapshot(listener: ISnapshotListener)
    fun setPlaySpeed(speedLevel: Float)
    fun setMirror(isMirror: Boolean)
    fun switchStream(quality: VideoQuality)
    fun changeRenderMode(renderMode : Int)
    var playURL: String?

    /**
     * 获取当前播放器模式
     *
     * @return [PlayerMode.WINDOW]          窗口模式
     * [PlayerMode.FULLSCREEN]              全屏模式
     * [PlayerMode.FLOAT]                   悬浮窗模式
     */
    val playerMode: PlayerDef.PlayerMode

    /**
     * 获取当前播放器状态
     *
     * @return [PlayerState.PLAYING]     播放中
     * [PlayerState.PAUSE]               暂停中
     * [PlayerState.LOADING]             缓冲中
     * [PlayerState.END]                 结束播放
     */
    val playerState: PlayerDef.PlayerState

    /**
     * 获取当前播放器类型
     *
     * @return [PlayerType.LIVE]     直播
     * [PlayerType.LIVE_SHIFT]       直播时移
     * [PlayerType.VOD]              点播
     */
    val playerType: PlayerDef.PlayerType

    /**
     * 设置播放器状态回调
     *
     * @param observer [IPlayerObserver]
     */
    fun setObserver(observer: IPlayerObserver?)

    fun getDuration():Long

    fun getPosition():Long

    fun updateImageSpriteAndKeyFrame(
        info: PlayImageSpriteInfo?,
        list: ArrayList<PlayKeyFrameDescInfo>?
    )


}