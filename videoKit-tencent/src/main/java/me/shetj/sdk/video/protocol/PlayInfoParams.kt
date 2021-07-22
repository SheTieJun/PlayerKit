package me.shetj.sdk.video.protocol

import me.shetj.sdk.video.tx.TXPlayerVideoId

/**
 * 视频信息协议解析需要传入的参数
 */
class PlayInfoParams {
    //必选
    var appId // 腾讯云视频appId
            = 0
    var fileId // 腾讯云视频fileId
            : String? = null
    var videoId //v4 协议参数
            : TXPlayerVideoId? = null

    override fun toString(): String {
        return "TCPlayInfoParams{" +
                ", appId='" + appId + '\'' +
                ", fileId='" + fileId + '\'' +
                ", v4='" + (if (videoId != null) videoId.toString() else "") + '\'' +
                '}'
    }
}