package com.tencent.video.superplayer.model.entity

/**
 * 视频雪碧图信息
 */
class PlayImageSpriteInfo {
    var imageUrls: ArrayList<String>? = null // 图片链接URL
    var webVttUrl: String? = null

    override fun toString(): String {
        return "TCPlayImageSpriteInfo{" +
                "imageUrls=" + imageUrls +
                ", webVttUrl='" + webVttUrl + '\'' +
                '}'
    }
}