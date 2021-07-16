package me.shetj.playerkitdemo

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import me.shetj.base.S
import me.shetj.base.network.RxHttp
import me.shetj.base.network.callBack.SimpleNetCallBack
import me.shetj.sdk.video.net.PlayerHttpClient
import me.shetj.sdk.video.net.SuperPlayerHttpClient


class BaseInitialize:Initializer<Unit> {

    override fun create(context: Context) {
        S.init(context.applicationContext as Application, false, "https://xxxx.com")
        SuperPlayerHttpClient.instance.setHttpClient(object : PlayerHttpClient {
            override fun doGet(url: String?, callBack: SuperPlayerHttpClient.OnHttpCallback?) {
                if (url != null) {
                    RxHttp.get(url).executeCus(object : SimpleNetCallBack<String>(context) {
                        override fun onSuccess(data: String) {
                            super.onSuccess(data)
                            callBack?.onSuccess(data)
                        }

                        override fun onError(e: Exception) {
                            super.onError(e)
                            callBack?.onError()
                        }
                    })
                }
            }

            override fun doPost(url: String?, json: String?, callBack: SuperPlayerHttpClient.OnHttpCallback?) {
                url?.let {
                    RxHttp.post(it).apply {
                        upJson(json)
                    }.executeCus(object : SimpleNetCallBack<String>(context) {
                        override fun onSuccess(data: String) {
                            super.onSuccess(data)
                            callBack?.onSuccess(data)
                        }

                        override fun onError(e: Exception) {
                            super.onError(e)
                            callBack?.onError()
                        }
                    })
                }
            }
        })
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return  mutableListOf()
    }
}