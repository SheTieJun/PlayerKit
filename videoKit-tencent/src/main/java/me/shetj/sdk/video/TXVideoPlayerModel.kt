package me.shetj.sdk.video

import me.shetj.sdk.video.model.VideoPlayerModel
import me.shetj.sdk.video.tx.TXPlayerVideoId


/**
 * 超级播放器支持三种方式播放视频:
 * 1. 视频 URL
 * 填写视频 URL, 如需使用直播时移功能，还需填写appId
 * 2. 腾讯云点播 File ID 播放
 * 填写 appId 及 videoId (如果使用旧版本V2, 请填写videoIdV2)
 * 3. 多码率视频播放
 * 是URL播放方式扩展，可同时传入多条URL，用于进行码率切换
 */
class TXVideoPlayerModel : VideoPlayerModel() {
    var appId  = 0

    /**
     * ------------------------------------------------------------------
     * 腾讯云点播 File ID 播放参数
     * ------------------------------------------------------------------
     */
    var videoId: TXPlayerVideoId? = null
}