package me.shetj.playerkitdemo

import android.app.Application
import android.content.Context
import com.tencent.video.superplayer.model.net.PlayerHttpClient
import com.tencent.video.superplayer.model.net.SuperPlayerHttpClient
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
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

}