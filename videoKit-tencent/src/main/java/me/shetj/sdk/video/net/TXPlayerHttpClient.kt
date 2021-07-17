package me.shetj.sdk.video.net

import android.util.Log

/**
 * 通过代理,把网络请求代理给外部
 */
class TXPlayerHttpClient: TXPlayerHttp {
    private var client : TXPlayerHttp?= null

    companion object   Holder {

        val instance by lazy { TXPlayerHttpClient()  }
    }

    fun setHttpClient(client: TXPlayerHttp?){
        this.client = client
    }

    override fun doGet(url: String?, callBack: OnHttpCallback?) {
        if (client == null){
            Log.e("SuperPlayerView","client is null,please set SuperPlayerHttpClient：SuperPlayerHttpClient.instance.setHttpClient ")
            return
        }
        client!!.doGet(url,callBack)
    }

    override fun doPost(url: String?, json: String?, callBack: OnHttpCallback?) {
        if (client == null){
            Log.e("SuperPlayerView","client is null,please set SuperPlayerHttpClient :SuperPlayerHttpClient.instance.setHttpClient")
            return
        }
        client!!.doPost(url,json,callBack)
    }


    interface OnHttpCallback {
        fun onSuccess(result: String)
        fun onError()
    }
}