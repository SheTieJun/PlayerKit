package com.tencent.video.superplayer.model.protocol

import com.tencent.video.superplayer.model.entity.PlayImageSpriteInfo
import com.tencent.video.superplayer.model.entity.PlayKeyFrameDescInfo
import com.tencent.video.superplayer.model.entity.ResolutionName
import com.tencent.video.superplayer.model.entity.VideoQuality

/**
 * 视频信息协议接口
 */
interface IPlayInfoProtocol {
    /**
     * 发送视频信息协议网络请求
     *
     * @param callback 协议请求回调
     */
    fun sendRequest(callback: IPlayInfoRequestCallback?)

    /**
     * 中途取消请求
     */
    fun cancelRequest()

    /**
     * 获取视频播放url
     *
     * @return 视频播放url字符串
     */
    val url: String?

    /**
     * 获取加密视频播放url
     *
     * @return url字符串
     */
    fun getEncyptedUrl(type: PlayInfoConstant.EncryptedURLType): String?

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
    val videoQualityList: ArrayList<VideoQuality>?

    /**
     * 获取默认画质
     *
     * @return 默认画质信息对象
     */
    val defaultVideoQuality: VideoQuality?

    /**
     * 获取视频画质别名列表
     *
     * @return 画质别名数组
     */
    val resolutionNameList: List<ResolutionName?>?

    /**
     * 透传内容
     *
     * @return 透传内容
     */
    val penetrateContext: String?
}