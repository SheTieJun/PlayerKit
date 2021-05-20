package com.tencent.video.superplayer.model.entity

class SuperPlayerVideoIdV2 {
    var fileId // 腾讯云视频fileId
            : String? = null
    var timeout // 【可选】加密链接超时时间戳，转换为16进制小写字符串，腾讯云 CDN 服务器会根据该时间判断该链接是否有效。
            : String? = null
    var us // 【可选】唯一标识请求，增加链接唯一性
            : String? = null
    var sign // 【可选】防盗链签名
            : String? = null
    var exper = -1 // 【V2可选】试看时长，单位：秒。可选
    override fun toString(): String {
        return "SuperPlayerVideoId{" +
                ", fileId='" + fileId + '\'' +
                ", timeout='" + timeout + '\'' +
                ", exper=" + exper +
                ", us='" + us + '\'' +
                ", sign='" + sign + '\'' +
                '}'
    }
}