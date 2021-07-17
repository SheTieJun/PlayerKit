package me.shetj.sdk.video.net

/**
 * 播放器可能需要用的的接口请求，如果你使用fileId 播放，就必须设置PlayerHttpClient
 */
interface TXPlayerHttp {
    fun doGet(url: String?, callBack: TXPlayerHttpClient.OnHttpCallback?)

    fun doPost(url: String?, json: String?, callBack: TXPlayerHttpClient.OnHttpCallback?)
}