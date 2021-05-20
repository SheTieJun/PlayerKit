package me.shetj.playerkitdemo

import android.app.Application
import android.content.Context
import com.tencent.video.superplayer.model.net.HttpClient
import com.tencent.video.superplayer.model.net.HttpURLClient
import me.shetj.base.S
import me.shetj.base.network.RxHttp
import me.shetj.base.network.callBack.SimpleNetCallBack

/**
 *
 * <b>@packageName：</b> com.shetj.diyalbume<br>
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2017/12/4<br>
 * <b>@company：</b><br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b><br>
 */
class APP : Application() {

    override fun onCreate() {
        super.onCreate()
        HttpURLClient.instance.setHttpClient(object : HttpClient {
            override fun doGet(url: String?, callBack: HttpURLClient.OnHttpCallback?) {
                if (url != null) {
                    RxHttp.get(url).executeCus(object : SimpleNetCallBack<String>(this@APP) {
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

            override fun doPost(url: String?, json: String?, callBack: HttpURLClient.OnHttpCallback?) {
                url?.let {
                    RxHttp.post(it).apply {
                        upJson(json)
                    }.executeCus(object : SimpleNetCallBack<String>(this@APP) {
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

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

}