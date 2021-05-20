package com.tencent.video.superplayer.model.entity

/**
 * 自适应码流视频画质别名
 */
class ResolutionName {
    var name // 画质名称
            : String? = null
    var type // 类型 可能的取值有 video 和 audio
            : String? = null
    var width = 0
    var height = 0
    override fun toString(): String {
        return "TCResolutionName{" +
                "width='" + width + '\'' +
                "height='" + height + '\'' +
                "type='" + type + '\'' +
                ", name=" + name +
                '}'
    }
}