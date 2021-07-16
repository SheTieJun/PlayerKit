package me.shetj.sdk.video.model

class SuperPlayerVideoId {
    var fileId: String? = null    // 腾讯云视频fileId
    var pSign: String? = null   // v4 开启防盗链必填
    override fun toString(): String {
        return "SuperPlayerVideoId{" +
                ", fileId='" + fileId + '\'' +
                ", pSign='" + pSign + '\'' +
                '}'
    }
}