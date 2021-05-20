package com.tencent.video.superplayer.model.entity

/**
 * 视频关键帧信息
 */
class PlayKeyFrameDescInfo {
    var content // 描述信息
            : String? = null
    var time // 关键帧时间(秒)
            = 0f

    override fun toString(): String {
        return "TCPlayKeyFrameDescInfo{" +
                "content='" + content + '\'' +
                ", time=" + time +
                '}'
    }
}