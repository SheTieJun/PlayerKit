package me.shetj.sdk.video.base

import android.text.TextUtils


object OtherKit {

    /**
     * 是否是RTMP协议
     *
     * @param videoURL
     * @return
     */
    fun isRTMPPlay(videoURL: String?): Boolean {
        return !TextUtils.isEmpty(videoURL) && videoURL!!.startsWith("rtmp")
    }

    /**
     * 是否是HTTP-FLV协议
     *
     * @param videoURL
     * @return
     */
    fun isFLVPlay(videoURL: String?): Boolean {
        return (!TextUtils.isEmpty(videoURL) && videoURL!!.startsWith("http://")
                || videoURL!!.startsWith("https://")) && videoURL.contains(".flv")
    }

}