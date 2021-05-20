package com.tencent.video.superplayer.model.net

class HttpURLClient {
    private var client : HttpClient ?= null

    companion object   Holder {
        val instance = HttpURLClient()
    }

    fun setHttpClient(client: HttpClient?){
        this.client = client
    }

    /**
     * get请求
     *
     * @param urlStr
     * @param callback
     */
    fun get(urlStr: String?, callback: OnHttpCallback?) {
        client?.doGet(urlStr,callback)
    }

    /**
     * post json数据请求
     *
     * @param urlStr
     * @param callback
     */
    fun postJson(urlStr: String?, json: String, callback: OnHttpCallback?) {
        client?.doPost(urlStr,json,callback)
    }

    interface OnHttpCallback {
        fun onSuccess(result: String)
        fun onError()
    }

}