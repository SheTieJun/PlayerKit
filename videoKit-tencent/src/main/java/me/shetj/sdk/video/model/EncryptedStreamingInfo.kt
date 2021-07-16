package me.shetj.sdk.video.model

/**
 * Created by hans on 2019/3/25.
 *
 *
 * 自适应码流信息
 */
class EncryptedStreamingInfo {
    var drmType: String? = null
    var url: String? = null
    override fun toString(): String {
        return "TCEncryptedStreamingInfo{" +
                ", drmType='" + drmType + '\'' +
                ", url='" + url + '\'' +
                '}'
    }
}