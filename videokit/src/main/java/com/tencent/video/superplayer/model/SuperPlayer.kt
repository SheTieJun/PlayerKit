package com.tencent.video.superplayer.model

import com.tencent.rtmp.TXLivePlayer
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.video.superplayer.SuperPlayerDef.*
import com.tencent.video.superplayer.SuperPlayerModel
import com.tencent.video.superplayer.base.PlayerConfig
import com.tencent.video.superplayer.model.entity.VideoQuality

interface SuperPlayer {
    /**
     * 开始播放
     *
     * @param url 视频地址
     */
    fun play(url: String?)

    /**
     * 开始播放
     *
     * @param appId 腾讯云视频appId
     * @param url   直播播放地址
     */
    fun play(appId: Int, url: String?)

    /**
     * 开始播放
     *
     * @param appId  腾讯云视频appId
     * @param fileId 腾讯云视频fileId
     * @param psign  防盗链签名，开启防盗链的视频必填，非防盗链视频可不填
     */
    fun play(appId: Int, fileId: String?, psign: String?)

    /**
     * 多分辨率播放
     * @param appId             腾讯云视频appId
     * @param superPlayerURLS   不同分辨率数据
     * @param defaultIndex      默认播放Index
     */
    fun play(appId: Int, superPlayerURLS: List<SuperPlayerModel.SuperPlayerURL?>?, defaultIndex: Int)

    /**
     * 重播
     */
    fun reStart()

    /**
     * 暂停播放
     */
    fun pause()

    /**
     * 暂停点播视频
     */
    fun pauseVod()

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

    fun getWidth():Int

    fun getHeight():Int

    fun getRotation() :Int
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
     * @param playerMode [SuperPlayerDef.PlayerMode.WINDOW]          窗口模式
     * [SuperPlayerDef.PlayerMode.FULLSCREEN]      全屏模式
     * [SuperPlayerDef.PlayerMode.FLOAT]           悬浮窗模式
     */
    fun switchPlayMode(playerMode: PlayerMode)
    fun enableHardwareDecode(enable: Boolean)
    fun setPlayerView(videoView: TXCloudVideoView?)
    fun seek(position: Int,isCallback:Boolean = true)
    fun setPlayToSeek(position: Int)
    fun snapshot(listener: TXLivePlayer.ITXSnapshotListener)
    fun setRate(speedLevel: Float)
    fun setMirror(isMirror: Boolean)
    fun switchStream(quality: VideoQuality)
    fun changeRenderMode(renderMode : Int)
    var playURL: String?

    /**
     * 获取当前播放器模式
     *
     * @return [SuperPlayerDef.PlayerMode.WINDOW]          窗口模式
     * [SuperPlayerDef.PlayerMode.FULLSCREEN]              全屏模式
     * [SuperPlayerDef.PlayerMode.FLOAT]                   悬浮窗模式
     */
    val playerMode: PlayerMode

    /**
     * 获取当前播放器状态
     *
     * @return [SuperPlayerDef.PlayerState.PLAYING]     播放中
     * [SuperPlayerDef.PlayerState.PAUSE]               暂停中
     * [SuperPlayerDef.PlayerState.LOADING]             缓冲中
     * [SuperPlayerDef.PlayerState.END]                 结束播放
     */
    val playerState: PlayerState

    /**
     * 获取当前播放器类型
     *
     * @return [SuperPlayerDef.PlayerType.LIVE]     直播
     * [SuperPlayerDef.PlayerType.LIVE_SHIFT]       直播时移
     * [SuperPlayerDef.PlayerType.VOD]              点播
     */
    val playerType: PlayerType

    /**
     * 设置播放器状态回调
     *
     * @param observer [SuperPlayerObserver]
     */
    fun setObserver(observer: SuperPlayerObserver?)


    fun getPlayConfig():PlayerConfig

    fun setPlayConfig(playerConfig: PlayerConfig)
}