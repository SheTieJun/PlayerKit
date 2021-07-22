package me.shetj.sdk.video

import android.content.Context
import com.shetj.sdk.video.viedoview.ui.BaseFloatPlayer
import me.shetj.sdk.video.tx.TXFloatPlayer
import me.shetj.sdk.video.tx.TXPlayerImpl
import me.shetj.sdk.video.tx.TXVideoPlayerView

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2021/7/17 0017<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  腾讯的超级播放器<br>
 */
object TXVideoFactory {

    var playShiftDomain = "liteavapp.timeshift.qcloud.com"

    /**
     * 获取播放view
     */
    fun getTXPlayerView(context: Context): TXVideoPlayerView {
        return TXVideoPlayerView(context)
    }

    /**
     * 获取播放器
     */
    fun getTXPlayer(context: Context): TXPlayerImpl {
        return TXPlayerImpl(context)
    }

    /**
     * 获取悬浮
     */
    fun getTXFloatView(context: Context): BaseFloatPlayer {
        return TXFloatPlayer(context)
    }
}