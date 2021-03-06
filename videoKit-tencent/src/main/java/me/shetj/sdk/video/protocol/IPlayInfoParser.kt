package me.shetj.sdk.video.protocol

import me.shetj.sdk.video.model.PlayImageSpriteInfo
import me.shetj.sdk.video.model.PlayKeyFrameDescInfo
import me.shetj.sdk.video.model.ResolutionName
import me.shetj.sdk.video.model.VideoQuality as VideoQuality1


/**
 * 视频信息协议解析接口
 */
interface IPlayInfoParser {
    /**
     * 获取未加密视频播放url,若没有获取sampleaes url
     *
     * @return url字符串
     */
    val uRL: String?

    /**
     * 获取加密视频播放url
     *
     * @return url字符串
     */
    fun getEncryptedURL(type: PlayInfoConstant.EncryptedURLType): String?

    /**
     * 获取加密token
     *
     * @return token字符串
     */
    val token: String?

    /**
     * 获取视频名称
     *
     * @return 视频名称字符串
     */
    val name: String?

    /**
     * 获取雪碧图信息
     *
     * @return 雪碧图信息对象
     */
    val imageSpriteInfo: PlayImageSpriteInfo?

    /**
     * 获取关键帧信息
     *
     * @return 关键帧信息数组
     */
    val keyFrameDescInfo: ArrayList<PlayKeyFrameDescInfo>?

    /**
     * 获取画质信息
     *
     * @return 画质信息数组
     */
    val videoQualityList: ArrayList<VideoQuality1>?

    /**
     * 获取默认画质信息
     *
     * @return 默认画质信息对象
     */
    val defaultVideoQuality: VideoQuality1?

    /**
     * 获取视频画质别名列表
     *
     * @return 画质别名数组
     */
    val resolutionNameList: ArrayList<ResolutionName>?
}