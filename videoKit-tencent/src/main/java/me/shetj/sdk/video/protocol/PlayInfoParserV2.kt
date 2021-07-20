package me.shetj.sdk.video.protocol

import android.util.Log
import com.tencent.liteav.basic.log.TXCLog
import me.shetj.sdk.video.model.TXVideoQualityUtils
import me.shetj.sdk.video.model.*
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*
import kotlin.collections.ArrayList

/**
 * V2视频信息协议解析实现类
 *
 * 负责解析V2视频信息协议请求响应的Json数据
 */
class PlayInfoParserV2(  // 协议请求返回的Json数据
    private val mResponse: JSONObject
) : IPlayInfoParser {
    //播放器配置信息
    private var mDefaultVideoClassification // 默认视频清晰度名称
            : String? = null
    private var mVideoClassificationList // 视频清晰度信息列表
            : List<VideoClassification>? = null

    /**
     * 获取雪碧图信息
     *
     * @return 雪碧图信息对象
     */
    override var imageSpriteInfo // 雪碧图信息
            : PlayImageSpriteInfo? = null
        private set

    /**
     * 获取关键帧信息
     *
     * @return 关键帧信息数组
     */
    override var keyFrameDescInfo // 关键帧打点信息
            : ArrayList<PlayKeyFrameDescInfo>? = null
        private set

    /**
     * 获取视频名称
     *
     * @return 视频名称字符串
     */
    //视频信息
    override var name // 视频名称
            : String? = null
        private set
    private var mSourceStream // 源视频流信息
            : PlayInfoStream? = null
    private var mMasterPlayList // 主播放视频流信息
            : PlayInfoStream? = null
    private var mTranscodePlayList // 转码视频信息列表
            : LinkedHashMap<String, PlayInfoStream>? = null

    /**
     * 获取视频播放url
     *
     * @return url字符串
     */
    override var uRL // 视频播放url
            : String? = null
        private set
    private var mVideoQualityList // 视频画质信息列表
            : ArrayList<VideoQuality>? = null

    /**
     * 获取默认画质信息
     *
     * @return 默认画质信息对象
     */
    override var defaultVideoQuality // 默认视频画质
            : VideoQuality? = null
        private set

    /**
     * 从视频信息协议请求响应的Json数据中解析出视频信息
     *
     * 解析流程：
     *
     * 1、解析播放器信息(playerInfo)字段，获取视频清晰度列表[.mVideoClassificationList]以及默认清晰度[.mDefaultVideoClassification]
     *
     * 2、解析雪碧图信息(imageSpriteInfo)字段，获取雪碧图信息[.mImageSpriteInfo]
     *
     * 3、解析关键帧信息(keyFrameDescInfo)字段，获取关键帧信息[.mKeyFrameDescInfo]
     *
     * 4、解析视频信息(videoInfo)字段，获取视频名称[.mName]、源视频信息[.mSourceStream]、
     * 主视频列表[.mMasterPlayList]、转码视频列表[.mTranscodePlayList]
     *
     * 5、从主视频列表、转码视频列表、源视频信息中解析出视频播放url[.mURL]、画质信息[.mVideoQualityList]、
     * 默认画质[.mDefaultVideoQuality]
     */
    private fun parsePlayInfo() {
        try {
            val playerInfo = mResponse.optJSONObject("playerInfo")
            if (playerInfo != null) {
                mDefaultVideoClassification = parseDefaultVideoClassification(playerInfo)
                mVideoClassificationList = parseVideoClassificationList(playerInfo)
            }
            val imageSpriteInfo = mResponse.optJSONObject("imageSpriteInfo")
            if (imageSpriteInfo != null) {
                this.imageSpriteInfo = parseImageSpriteInfo(imageSpriteInfo)
            }
            val keyFrameDescInfo = mResponse.optJSONObject("keyFrameDescInfo")
            if (keyFrameDescInfo != null) {
                this.keyFrameDescInfo = parseKeyFrameDescInfo(keyFrameDescInfo)
            }
            val videoInfo = mResponse.optJSONObject("videoInfo")
            if (videoInfo != null) {
                name = parseName(videoInfo)
                mSourceStream = parseSourceStream(videoInfo)
                mMasterPlayList = parseMasterPlayList(videoInfo)
                mTranscodePlayList = parseTranscodePlayList(videoInfo)
            }
            parseVideoInfo()
        } catch (e: JSONException) {
            TXCLog.e(TAG, Log.getStackTraceString(e))
        }
    }

    /**
     * 解析默认视频清晰度信息
     *
     * @param playerInfo 包含默认视频清晰度信息的Json对象
     * @return 默认视频清晰度名称字符串
     */
    @Throws(JSONException::class)
    private fun parseDefaultVideoClassification(playerInfo: JSONObject): String {
        return playerInfo.getString("defaultVideoClassification")
    }

    /**
     * 解析视频清晰度信息
     *
     * @param playerInfo 包含默认视频类别信息的Json对象
     * @return 视频清晰度信息数组
     */
    @Throws(JSONException::class)
    private fun parseVideoClassificationList(playerInfo: JSONObject): List<VideoClassification> {
        val arrayList: MutableList<VideoClassification> = ArrayList()
        val videoClassificationArray = playerInfo.getJSONArray("videoClassification")
        if (videoClassificationArray != null) {
            for (i in 0 until videoClassificationArray.length()) {
                val `object` = videoClassificationArray.getJSONObject(i)
                val classification = VideoClassification()
                classification.id = `object`.getString("id")
                classification.name = `object`.getString("name")
                val definitionList: MutableList<Int> = ArrayList()
                val array = `object`.getJSONArray("definitionList")
                if (array != null) {
                    for (j in 0 until array.length()) {
                        val definition = array.getInt(j)
                        definitionList.add(definition)
                    }
                }
                classification.definitionList = definitionList
                arrayList.add(classification)
            }
        }
        return arrayList
    }

    /**
     * 解析雪碧图信息
     *
     * @param imageSpriteInfo 包含雪碧图信息的Json对象
     * @return 雪碧图信息对象
     */
    @Throws(JSONException::class)
    private fun parseImageSpriteInfo(imageSpriteInfo: JSONObject): PlayImageSpriteInfo? {
        val imageSpriteList = imageSpriteInfo.getJSONArray("imageSpriteList")
        if (imageSpriteList != null) {
            val spriteJSONObject =
                imageSpriteList.getJSONObject(imageSpriteList.length() - 1) //获取最后一个来解析
            val info = PlayImageSpriteInfo()
            info.webVttUrl = spriteJSONObject.getString("webVttUrl")
            val jsonArray = spriteJSONObject.getJSONArray("imageUrls")
            val imageUrls: ArrayList<String> = ArrayList()
            for (i in 0 until jsonArray.length()) {
                val url = jsonArray.getString(i)
                imageUrls.add(url)
            }
            info.imageUrls = imageUrls
            return info
        }
        return null
    }

    /**
     * 解析关键帧打点信息
     *
     * @param keyFrameDescInfo 包含关键帧信息的Json对象
     * @return 关键帧信息数组
     */
    @Throws(JSONException::class)
    private fun parseKeyFrameDescInfo(keyFrameDescInfo: JSONObject): ArrayList<PlayKeyFrameDescInfo>? {
        val jsonArr = keyFrameDescInfo.getJSONArray("keyFrameDescList")
        if (jsonArr != null) {
            val infoList: ArrayList<PlayKeyFrameDescInfo> = ArrayList()
            for (i in 0 until jsonArr.length()) {
                val content = jsonArr.getJSONObject(i).getString("content")
                val time = jsonArr.getJSONObject(i).getLong("timeOffset")
                val timeS = (time / 1000.0).toFloat() //转换为秒
                val info = PlayKeyFrameDescInfo()
                try {
                    info.content = URLDecoder.decode(content, "UTF-8")
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    info.content = ""
                }
                info.time = timeS
                infoList.add(info)
            }
            return infoList
        }
        return null
    }

    /**
     * 解析视频名称
     *
     * @param videoInfo 包含视频名称信息的Json对象
     * @return  视频名称字符串
     * @throws JSONException
     */
    @Throws(JSONException::class)
    private fun parseName(videoInfo: JSONObject): String? {
        val basicInfo = videoInfo.getJSONObject("basicInfo")
        return if (basicInfo != null) {
            basicInfo.getString("name")
        } else null
    }

    /**
     * 解析源视频流信息
     *
     * @param videoInfo 包含源视频流信息的Json对象
     * @return 源视频流信息对象
     */
    @Throws(JSONException::class)
    private fun parseSourceStream(videoInfo: JSONObject): PlayInfoStream? {
        val sourceVideo = videoInfo.getJSONObject("sourceVideo")
        if (sourceVideo != null) {
            val stream = PlayInfoStream()
            stream.url = sourceVideo.getString("url")
            stream.duration = sourceVideo.getInt("duration")
            stream.width = sourceVideo.getInt("width")
            stream.height = sourceVideo.getInt("height")
            stream.size = sourceVideo.getInt("size")
            stream.bitrate = sourceVideo.getInt("bitrate")
            return stream
        }
        return null
    }

    /**
     * 解析主播放视频流信息
     *
     * @param videoInfo 包含主播放视频流信息的Json对象
     * @return 主播放视频流信息对象
     */
    @Throws(JSONException::class)
    private fun parseMasterPlayList(videoInfo: JSONObject): PlayInfoStream? {
        if (!videoInfo.has("masterPlayList")) return null
        val masterPlayList = videoInfo.getJSONObject("masterPlayList")
        if (masterPlayList != null) {
            val stream = PlayInfoStream()
            stream.url = masterPlayList.getString("url")
            return stream
        }
        return null
    }

    /**
     * 解析转码视频流信息
     *
     * 转码视频流信息[.mTranscodePlayList]中不包含清晰度名称，需要与视频清晰度信息[.mVideoClassificationList]做匹配
     *
     * @param videoInfo 包含转码视频流信息的Json对象
     * @return 转码视频信息列表 key: 清晰度名称 value: 视频流信息
     */
    @Throws(JSONException::class)
    private fun parseTranscodePlayList(videoInfo: JSONObject): LinkedHashMap<String, PlayInfoStream>? {
        val transcodeList = parseStreamList(videoInfo)
            ?: return mTranscodePlayList
        for (i in transcodeList.indices) {
            val stream = transcodeList[i]
            stream.id = "YH"
            stream.name = "原画"
            // 匹配清晰度
            if (mVideoClassificationList != null) {
                for (j in mVideoClassificationList!!.indices) {
                    val classification = mVideoClassificationList!![j]
                    val definitionList = classification.definitionList
                    definitionList?.forEach {
                        if (stream.definition.toString().contains(it.toString())) {
                            stream.id = classification.id
                            stream.name = classification.name
                        }
                    }
                }
            }
        }
        //清晰度去重
        val idList = LinkedHashMap<String, PlayInfoStream>()
        for (i in transcodeList.indices) {
            val stream = transcodeList[i]
            if (!idList.containsKey(stream.id)) {
                idList[stream.id!!] = stream
            } else {
                val copy = idList[stream.id]
                if (copy?.url?.endsWith("mp4") == true) {  // 列表中url是mp4，则进行下一步
                    continue
                }
                if (stream.url?.endsWith("mp4") == true) { // 新判断的url是mp4，则替换列表中
                    idList.remove(copy!!.id)
                    idList[stream.id!!] = stream
                }
            }
        }
        //按清晰度排序
        return idList
    }

    /**
     * 解析转码视频信息
     *
     * @param videoInfo 包含转码视频信息的Json对象
     * @return 转码视频是信息数组
     */
    @Throws(JSONException::class)
    private fun parseStreamList(videoInfo: JSONObject): List<PlayInfoStream> {
        val streamList: MutableList<PlayInfoStream> = ArrayList()
        val transcodeList = videoInfo.optJSONArray("transcodeList")
        if (transcodeList != null) {
            for (i in 0 until transcodeList.length()) {
                val transcode = transcodeList.getJSONObject(i)
                val stream = PlayInfoStream()
                stream.url = transcode.getString("url")
                stream.duration = transcode.getInt("duration")
                stream.width = transcode.getInt("width")
                stream.height = transcode.getInt("height")
                stream.size = transcode.getInt("size")
                stream.bitrate = transcode.getInt("bitrate")
                stream.definition = transcode.getInt("definition")
                streamList.add(stream)
            }
        }
        return streamList
    }

    /**
     * 解析视频播放url、画质列表、默认画质
     *
     * V2协议响应Json数据中可能包含多个视频播放信息：主播放视频信息[.mMasterPlayList]、转码视频[.mTranscodePlayList]、
     * 源视频[.mSourceStream], 播放优先级依次递减
     *
     * 从优先级最高的视频信息中解析出播放信息
     */
    private fun parseVideoInfo() {
        //有主播放视频信息时，从中解析出支持多码率播放的url
        if (mMasterPlayList != null) {
            uRL = mMasterPlayList!!.url
            return
        }
        //无主播放信息，从转码视频信息中解析出各码流信息
        if (mTranscodePlayList != null && mTranscodePlayList!!.size != 0) {
            var stream = mTranscodePlayList!![mDefaultVideoClassification]
            var videoURL: String? = null
            if (stream != null) {
                videoURL = stream.url
            } else {
                for (stream1 in mTranscodePlayList!!.values) {
                    if (stream1.url != null) {
                        stream = stream1
                        videoURL = stream1.url
                        break
                    }
                }
            }
            if (videoURL != null) {
                mVideoQualityList =
                    TXVideoQualityUtils.convertToVideoQualityList(mTranscodePlayList!!)
                defaultVideoQuality = TXVideoQualityUtils.convertToVideoQuality(stream)
                uRL = videoURL
                return
            }
        }
        //无主播放信息、转码信息，从源视频信息中解析出播放信息
        if (mSourceStream != null) {
            if (mDefaultVideoClassification != null) {
                defaultVideoQuality = TXVideoQualityUtils.convertToVideoQuality(
                    mSourceStream!!,
                    mDefaultVideoClassification!!
                )
                mVideoQualityList = ArrayList()
                mVideoQualityList!!.add(defaultVideoQuality!!)
            }
            uRL = mSourceStream?.url
        }
    }

    override fun getEncryptedURL(type: PlayInfoConstant.EncryptedURLType): String? {
        return null
    }

    override val token: String?
        get() = null

    /**
     * 获取画质信息
     *
     * @return 画质信息数组
     */
    override val videoQualityList: ArrayList<VideoQuality>?
        get() = mVideoQualityList

    /**
     * 获取视频画质别名列表
     *
     * @return 画质别名数组
     */
    override val resolutionNameList: ArrayList<ResolutionName>?
        get() = null

    companion object {
        private const val TAG = "TCPlayInfoParserV2"
    }

    init {
        parsePlayInfo()
    }
}