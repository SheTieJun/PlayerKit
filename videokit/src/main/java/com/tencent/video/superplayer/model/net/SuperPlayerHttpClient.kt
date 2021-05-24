package com.tencent.video.superplayer.model.net

import android.util.Log

/**
 * 通过代理,把网络请求代理给外部
 */
class SuperPlayerHttpClient:PlayerHttpClient{
    private var client : PlayerHttpClient ?= null

    companion object   Holder {
        val instance = SuperPlayerHttpClient()
    }

    fun setHttpClient(client: PlayerHttpClient?){
        this.client = client
    }

    override fun doGet(url: String?, callBack: OnHttpCallback?) {
        if (client == null){
            Log.e("SuperPlayerView","client is null,please set httpclient ")
            return
        }
        client!!.doGet(url,callBack)
    }

    override fun doPost(url: String?, json: String?, callBack: OnHttpCallback?) {
        if (client == null){
            Log.e("SuperPlayerView","client is null,please set httpclient ")
            return
        }
        client!!.doPost(url,json,callBack)
    }


    interface OnHttpCallback {
        fun onSuccess(result: String)
        fun onError()
    }
}