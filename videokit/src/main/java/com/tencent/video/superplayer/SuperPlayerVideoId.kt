package com.tencent.video.superplayer

/**
 * Created by hans on 2019/3/25.
 * 使用腾讯云fileId播放
 */
class SuperPlayerVideoId {
    // 腾讯云视频fileId
    var fileId: String? = null
    // v4 开启防盗链必填
    var pSign: String? = null

    override fun toString(): String {
        return "SuperPlayerVideoId{" +
                ", fileId='" + fileId + '\'' +
                ", pSign='" + pSign + '\'' +
                '}'
    }
}