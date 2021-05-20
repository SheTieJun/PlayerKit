package com.tencent.video.superplayer.model.protocol

import android.os.*
import android.text.TextUtils
import com.tencent.liteav.basic.log.TXCLog
import com.tencent.video.superplayer.model.entity.PlayImageSpriteInfo
import com.tencent.video.superplayer.model.entity.PlayKeyFrameDescInfo
import com.tencent.video.superplayer.model.entity.ResolutionName
import com.tencent.video.superplayer.model.entity.VideoQuality
import com.tencent.video.superplayer.model.net.HttpURLClient
import org.json.JSONException
import org.json.JSONObject

/**
 * V2视频信息协议实现类
 *
 *
 * 负责V2视频信息协议的请求控制与数据获取
 */
class PlayInfoProtocolV2(  // 协议请求输入的参数
    private val mParams: PlayInfoParams
) : IPlayInfoProtocol {
    private val BASE_URLS_V2 = "https://playvideo.qcloud.com/getplayinfo/v2" // V2协议请求地址
    private val mMainHandler: Handler = Handler(Looper.getMainLooper())
    private var mParser: IPlayInfoParser? = null

    /**
     * 发送视频信息协议网络请求
     *
     * @param callback 协议请求回调
     */
    override fun sendRequest(callback: IPlayInfoRequestCallback?) {
        if (mParams.fileId == null) {
            return
        }
        val urlStr = makeUrlString()
        TXCLog.i(TAG, "getVodByFileId: url = $urlStr")
        HttpURLClient.instance.get(urlStr, object : HttpURLClient.OnHttpCallback {
            override fun onSuccess(result: String) {
                TXCLog.i(TAG, "http request success:  result = $result")
                parseJson(result, callback)
                runOnMainThread { callback!!.onSuccess(this@PlayInfoProtocolV2, mParams) }
            }

            override fun onError() {
                runOnMainThread { callback?.onError(-1, "http request error.") }
            }
        })
    }

    /**
     * 拼装协议请求url
     *
     * @return 协议请求url字符串
     */
    private fun makeUrlString(): String {
        var urlStr = String.format("%s/%d/%s", BASE_URLS_V2, mParams.appId, mParams.fileId)
        if (mParams.videoIdV2 != null) {
            val query = makeQueryString(
                mParams.videoIdV2!!.timeout,
                mParams.videoIdV2!!.us,
                mParams.videoIdV2!!.exper,
                mParams.videoIdV2!!.sign
            )
            urlStr = "$urlStr?$query"
        }
        return urlStr
    }

    /**
     * 拼装协议请求url中的query字段
     *
     * @param timeout 加密链接超时时间戳
     * @param us      唯一标识请求
     * @param exper   试看时长，单位：秒，十进制数值
     * @param sign    签名字符串
     * @return query字段字符串
     */
    private fun makeQueryString(timeout: String?, us: String?, exper: Int, sign: String?): String {
        val str = StringBuilder()
        if (timeout != null) {
            str.append("t=$timeout&")
        }
        if (us != null) {
            str.append("us=$us&")
        }
        if (sign != null) {
            str.append("sign=$sign&")
        }
        if (exper >= 0) {
            str.append("exper=$exper&")
        }
        if (str.length > 1) {
            str.deleteCharAt(str.length - 1)
        }
        return str.toString()
    }

    /**
     * 中途取消请求
     */
    override fun cancelRequest() {}

    /**
     * 获取视频播放url
     *
     * @return 视频播放url字符串
     */
    override val url: String?
        get() = if (mParser == null) null else mParser!!.uRL

    override fun getEncyptedUrl(type: PlayInfoConstant.EncryptedURLType): String? {
        return mParser!!.getEncryptedURL(type)
    }

    override val token: String?
        get() = mParser!!.token

    /**
     * 获取视频名称
     *
     * @return 视频名称字符串
     */
    override val name: String?
        get() = if (mParser == null) null else mParser!!.name

    /**
     * 获取雪碧图信息
     *
     * @return 雪碧图信息对象
     */
    override val imageSpriteInfo: PlayImageSpriteInfo?
        get() = if (mParser == null) null else mParser!!.imageSpriteInfo

    /**
     * 获取关键帧信息
     *
     * @return 关键帧信息数组
     */
    override val keyFrameDescInfo: ArrayList<PlayKeyFrameDescInfo>?
        get() = if (mParser == null) null else mParser!!.keyFrameDescInfo

    /**
     * 获取画质信息
     *
     * @return 画质信息数组
     */
    override val videoQualityList: ArrayList<VideoQuality>?
        get() = if (mParser == null) null else mParser!!.videoQualityList

    /**
     * 获取默认画质
     *
     * @return 默认画质信息对象
     */
    override val defaultVideoQuality: VideoQuality?
        get() = if (mParser == null) null else mParser!!.defaultVideoQuality

    /**
     * 解析视频信息协议请求响应的Json数据
     *
     * @param content  响应Json字符串
     * @param callback 协议请求回调
     */
    private fun parseJson(content: String, callback: IPlayInfoRequestCallback?): Boolean {
        if (TextUtils.isEmpty(content)) {
            TXCLog.e(TAG, "parseJsonV2 err, content is empty!")
            runOnMainThread { callback!!.onError(-1, "request return error!") }
            return false
        }
        try {
            val jsonObject = JSONObject(content)
            val code = jsonObject.getInt("code")
            val message = jsonObject.optString("message")
            TXCLog.e(TAG, message)
            mParser = if (code == 0) {
                PlayInfoParserV2(jsonObject)
            } else {
                runOnMainThread { callback!!.onError(code, message) }
                return false
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            TXCLog.e(TAG, "parseJson err")
        }
        return true
    }

    /**
     * 切换到主线程
     *
     *
     * 从视频协议请求回调的子线程切换回主线程
     *
     * @param r 需要在主线程中执行的任务
     */
    private fun runOnMainThread(r: Runnable) {
        if (Looper.myLooper() == mMainHandler.looper) {
            r.run()
        } else {
            mMainHandler.post(r)
        }
    }

    /**
     * 获取视频画质别名列表
     *
     * @return 画质别名数组
     */
    override val resolutionNameList: List<ResolutionName>?
        get() = if (mParser == null) null else mParser!!.resolutionNameList
    override val penetrateContext: String?
        get() = null

    companion object {
        private const val TAG = "TCPlayInfoProtocolV2"
    }

}