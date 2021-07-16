package me.shetj.sdk.video.protocol

import android.text.TextUtils
import android.util.Log
import me.shetj.sdk.video.model.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * V4视频协议解析实现类
 *
 * 负责解析V4视频信息协议请求响应的Json数据
 */
class PlayInfoParserV4(  // 协议请求返回的Json数据
    private val mResponse: JSONObject
) : IPlayInfoParser {
    /**
     * 获取视频名称
     *
     * @return 视频名称字符串
     */
    override var name // 视频名称
            : String? = null
        private set
    private var mURL // 未加密视频播放url
            : String? = null
    private var mToken // DRM token
            : String? = null
    private var mEncryptedStreamingInfoList // 加密视频播放url 数组
            : ArrayList<EncryptedStreamingInfo>? = null

    /**
     * 获取雪碧图信息
     *
     * @return 雪碧图信息对象
     */
    override var imageSpriteInfo // 雪碧图信息
            : PlayImageSpriteInfo? = null
        private set
    private var mKeyFrameDescInfo // 关键帧信息
            : ArrayList<PlayKeyFrameDescInfo>? = null
    private var mResolutionNameList // 自适应码流画质名称匹配信息
            : ArrayList<ResolutionName>? = null

    @Throws(JSONException::class)
    private fun parseSubStreams(substreams: JSONArray?) {
        if (substreams != null && substreams.length() > 0) {
            mResolutionNameList = ArrayList()
            for (i in 0 until substreams.length()) {
                val jsonObject = substreams.getJSONObject(i)
                val resolutionName = ResolutionName()
                val width = jsonObject.optInt("width")
                val height = jsonObject.optInt("height")
                resolutionName.width = width
                resolutionName.height = height
                resolutionName.name = jsonObject.optString("resolutionName")
                resolutionName.type = jsonObject.optString("type")
                mResolutionNameList!!.add(resolutionName)
            }
        }
    }

    /**
     * 从视频信息协议请求响应的Json数据中解析出视频信息
     */
    private fun parsePlayInfo() {
        try {
            val media = mResponse.getJSONObject("media")
            if (media != null) {
                //解析视频名称
                val basicInfo = media.optJSONObject("basicInfo")
                if (basicInfo != null) {
                    name = basicInfo.optString("name")
                }
                //解析视频播放url
                val streamingInfo = media.getJSONObject("streamingInfo")
                if (streamingInfo != null) {
                    val plainoutObj = streamingInfo.optJSONObject("plainOutput") //未加密的输出
                    if (plainoutObj != null) {
                        mURL = plainoutObj.optString("url") //未加密直接解析出视频url
                        parseSubStreams(plainoutObj.optJSONArray("subStreams"))
                    }
                    val drmoutputobj = streamingInfo.optJSONArray("drmOutput") //加密输出
                    if (drmoutputobj != null && drmoutputobj.length() > 0) {
                        mEncryptedStreamingInfoList = ArrayList()
                        for (i in 0 until drmoutputobj.length()) {
                            val jsonObject = drmoutputobj.optJSONObject(i)
                            val drmType = jsonObject.optString("type")
                            val url = jsonObject.optString("url")
                            val info = EncryptedStreamingInfo()
                            info.drmType = drmType
                            info.url = url
                            mEncryptedStreamingInfoList!!.add(info)
                            parseSubStreams(jsonObject.optJSONArray("subStreams"))
                        }
                    }
                    mToken = streamingInfo.optString("drmToken")
                }
                //解析雪碧图信息
                val imageSpriteInfo = media.optJSONObject("imageSpriteInfo")
                if (imageSpriteInfo != null) {
                    this.imageSpriteInfo = PlayImageSpriteInfo()

                    this.imageSpriteInfo!!.webVttUrl = imageSpriteInfo.getString("webVttUrl")
                    val jsonArray = imageSpriteInfo.optJSONArray("imageUrls")
                    if (jsonArray != null && jsonArray.length() > 0) {
                        val imageUrls: ArrayList<String> = ArrayList()
                        for (i in 0 until jsonArray.length()) {
                            val url = jsonArray.getString(i)
                            imageUrls.add(url)
                        }
                        this.imageSpriteInfo!!.imageUrls = imageUrls
                    }
                }
                //解析关键帧信息
                val keyFrameDescInfo = media.optJSONObject("keyFrameDescInfo")
                if (keyFrameDescInfo != null) {
                    mKeyFrameDescInfo = ArrayList()
                    val keyFrameDescList = keyFrameDescInfo.optJSONArray("keyFrameDescList")
                    if (keyFrameDescList != null && keyFrameDescList.length() > 0) {
                        for (i in 0 until keyFrameDescList.length()) {
                            val jsonObject = keyFrameDescList.getJSONObject(i)
                            val info = PlayKeyFrameDescInfo()
                            info.time = jsonObject.optLong("timeOffset").toFloat()
                            info.content = jsonObject.optString("content")
                            mKeyFrameDescInfo!!.add(info)
                        }
                    }
                }
            }
        } catch (e: JSONException) {
            Log.e(TAG, Log.getStackTraceString(e))
        }
    }

    /**
     * 获取视频播放url
     *
     * @return url字符串
     */
    override val uRL: String?
        get() {
            var url = mURL
            if (!TextUtils.isEmpty(mToken)) {
                url = getEncryptedURL(PlayInfoConstant.EncryptedURLType.SIMPLEAES)
            }
            return url
        }

    override fun getEncryptedURL(type: PlayInfoConstant.EncryptedURLType): String? {
        for (info in mEncryptedStreamingInfoList!!) {
            if (info.drmType != null && info.drmType.equals(type.value, ignoreCase = true)) {
                return info.url
            }
        }
        return null
    }

    override val token: String?
        get() = if (TextUtils.isEmpty(mToken)) null else mToken

    /**
     * 获取关键帧信息
     *
     * @return 关键帧信息数组
     */
    override val keyFrameDescInfo: ArrayList<PlayKeyFrameDescInfo>?
        get() = mKeyFrameDescInfo

    /**
     * 获取画质信息
     *
     * @return 画质信息数组
     */
    override val videoQualityList: ArrayList<VideoQuality>?
        get() = null

    /**
     * 获取默认画质信息
     *
     * @return 默认画质信息对象
     */
    override val defaultVideoQuality: VideoQuality?
        get() = null

    /**
     * 获取视频画质别名列表
     *
     * @return 画质别名数组
     */
    override val resolutionNameList: ArrayList<ResolutionName>?
        get() = mResolutionNameList

    companion object {
        private const val TAG = "TCPlayInfoParserV4"
    }

    init {
        parsePlayInfo()
    }
}