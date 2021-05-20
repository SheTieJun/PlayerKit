package com.tencent.video.superplayer.model.utils

import com.tencent.liteav.basic.log.TXCLog
import com.tencent.rtmp.TXBitrateItem
import com.tencent.video.superplayer.model.entity.PlayInfoStream
import com.tencent.video.superplayer.model.entity.ResolutionName
import com.tencent.video.superplayer.model.entity.VideoQuality
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by yuejiaoli on 2018/7/6.
 *
 * 清晰度转换工具
 */
object VideoQualityUtils {
    private const val TAG = "TCVideoQualityUtil"

    /**
     * 从比特流信息转换为清晰度信息
     *
     * @param bitrateItem
     * @return
     */
    fun convertToVideoQuality(bitrateItem: TXBitrateItem, index: Int): VideoQuality {
        val quality = VideoQuality()
        quality.bitrate = bitrateItem.bitrate
        quality.index = bitrateItem.index
        when (index) {
            0 -> {
                quality.name = "FLU"
                quality.title = "流畅"
            }
            1 -> {
                quality.name = "SD"
                quality.title = "标清"
            }
            2 -> {
                quality.name = "HD"
                quality.title = "高清"
            }
            3 -> {
                quality.name = "FHD"
                quality.title = "超清"
            }
            4 -> {
                quality.name = "2K"
                quality.title = "2K"
            }
            5 -> {
                quality.name = "4K"
                quality.title = "4K"
            }
            6 -> {
                quality.name = "8K"
                quality.title = "8K"
            }
        }
        return quality
    }

    /**
     * 从源视频信息与视频类别信息转换为清晰度信息
     *
     * @param sourceStream
     * @param classification
     * @return
     */
    fun convertToVideoQuality(sourceStream: PlayInfoStream, classification: String): VideoQuality {
        val quality = VideoQuality()
        quality.bitrate = sourceStream.bitrate
        when (classification) {
            "FLU" -> {
                quality.name = "FLU"
                quality.title = "流畅"
            }
            "SD" -> {
                quality.name = "SD"
                quality.title = "标清"
            }
            "HD" -> {
                quality.name = "HD"
                quality.title = "高清"
            }
            "FHD" -> {
                quality.name = "FHD"
                quality.title = "全高清"
            }
            "2K" -> {
                quality.name = "2K"
                quality.title = "2K"
            }
            "4K" -> {
                quality.name = "4K"
                quality.title = "4K"
            }
        }
        quality.url = sourceStream.url
        quality.index = -1
        return quality
    }

    /**
     * 从[PlayInfoStream]转换为[VideoQuality]
     *
     * @param stream
     * @return
     */
    fun convertToVideoQuality(stream: PlayInfoStream?): VideoQuality {
        val qulity = VideoQuality()
        qulity.bitrate = stream!!.bitrate
        qulity.name = stream.id
        qulity.title = stream.name
        qulity.url = stream.url
        qulity.index = -1
        return qulity
    }

    /**
     * 从转码列表转换为清晰度列表
     *
     * @param transcodeList
     * @return
     */
    fun convertToVideoQualityList(transcodeList: HashMap<String, PlayInfoStream>): ArrayList<VideoQuality> {
        val videoQualities: ArrayList<VideoQuality> = ArrayList()
        for (classification in transcodeList.keys) {
            val videoQuality = convertToVideoQuality(transcodeList[classification])
            videoQualities.add(videoQuality)
        }
        return videoQualities
    }

    /**
     * 根据视频清晰度别名表从码率信息转换为视频清晰度
     *
     * @param bitrateItem     码率
     * @param resolutionNames 清晰度别名表
     * @return
     */
    fun convertToVideoQuality(
        bitrateItem: TXBitrateItem,
        resolutionNames: List<ResolutionName?>?
    ): VideoQuality {
        val quality = VideoQuality()
        quality.bitrate = bitrateItem.bitrate
        quality.index = bitrateItem.index
        var getName = false
        for (resolutionName in resolutionNames!!) {
            if ((resolutionName!!.width == bitrateItem.width && resolutionName.height == bitrateItem.height || resolutionName.width == bitrateItem.height && resolutionName.height == bitrateItem.width)
                && "video".equals(resolutionName.type, ignoreCase = true)
            ) {
                quality.title = resolutionName.name
                getName = true
                break
            }
        }
        if (!getName) {
            TXCLog.i(TAG, "error: could not get quality name!")
        }
        return quality
    }
}