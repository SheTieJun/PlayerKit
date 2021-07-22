package me.shetj.sdk.video.model


open class VideoPlayerModel {
    /**
     * ------------------------------------------------------------------
     * 直接使用URL播放
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
    var multiURLs: List<PlayerURL?>? = null
    var playDefaultIndex    = 0
    var title = ""

    class PlayerURL {
        constructor(url: String, qualityName: String) {
            this.qualityName = qualityName
            this.url = url
        }

        constructor() {}

        var qualityName = "原画" // 清晰度名称（用于显示在UI层）
        var url = "" // 该清晰度对应的地址
    }
}