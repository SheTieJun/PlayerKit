package com.tencent.video.superplayer.model.entity

/**
 * Created by yuejiaoli on 2018/7/7.
 *
 *
 * 清晰度
 */
class VideoQuality {
    var index = 0
    var bitrate = 0
    var name: String? = null
    var title: String? = null
    var url: String? = null

    constructor() {}
    constructor(index: Int, title: String?, url: String?) {
        this.index = index
        this.title = title
        this.url = url
    }
}