package com.tencent.video.superplayer.viedoview.model


/**
 * 超级播放器支持三种方式播放视频:
 * 1. 视频 URL
 * 填写视频 URL, 如需使用直播时移功能，还需填写appId
 * 2. 腾讯云点播 File ID 播放
 * 填写 appId 及 videoId (如果使用旧版本V2, 请填写videoIdV2)
 * 3. 多码率视频播放
 * 是URL播放方式扩展，可同时传入多条URL，用于进行码率切换
 */
class VideoPlayerModel {
    var appId  = 0

    /**
     * ------------------------------------------------------------------
     * 直接使用URL播放
     *
     *
     * 支持 RTMP、FLV、MP4、HLS 封装格式
     * 使用腾讯云直播时移功能则需要填写appId
     * ------------------------------------------------------------------
     */
    var url = "" // 视频URL

    /**
     * ------------------------------------------------------------------
     * 多码率视频 URL
     *
     *
     * 用于拥有多个播放地址的多清晰度视频播放
     * ------------------------------------------------------------------
     */
    var multiURLs: List<SuperPlayerURL?>? = null
    var playDefaultIndex    = 0

    /**
     * ------------------------------------------------------------------
     * 腾讯云点播 File ID 播放参数
     * ------------------------------------------------------------------
     */
    var videoId: SuperPlayerVideoId? = null
    var title = ""

    class SuperPlayerURL {
        constructor(url: String, qualityName: String) {
            this.qualityName = qualityName
            this.url = url
        }

        constructor() {}

        var qualityName = "原画" // 清晰度名称（用于显示在UI层）
        var url = "" // 该清晰度对应的地址
    }
}