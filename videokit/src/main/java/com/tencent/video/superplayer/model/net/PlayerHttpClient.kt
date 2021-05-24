package com.tencent.video.superplayer.model.net

/**
 * 播放器可能需要用的的接口请求，如果你使用fileId 播放，就必须设置PlayerHttpClient
 */
interface PlayerHttpClient {
    fun doGet(url: String?, callBack: SuperPlayerHttpClient.OnHttpCallback?)

    fun doPost(url: String?, json: String?, callBack: SuperPlayerHttpClient.OnHttpCallback?)
}