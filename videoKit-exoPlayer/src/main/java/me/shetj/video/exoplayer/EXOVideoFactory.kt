package me.shetj.video.exoplayer

import android.content.Context
import com.google.android.exoplayer2.ui.PlayerView
import com.shetj.sdk.video.viedoview.ui.BaseFloatPlayer

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2021/7/17 0017<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  腾讯的超级播放器<br>
 */
object EXOVideoFactory {


    /**
     * 获取播放view
     */
    fun getPlayerView(context: Context): PlayerView {
        return PlayerView(context).apply {
            useController=false
        }
    }

    /**
     * 获取播放器
     */
    fun getPlayer(context: Context): EXOPlayerImpl {
        return EXOPlayerImpl(context)
    }

    /**
     * 获取悬浮
     */
    fun getFloatView(context: Context): BaseFloatPlayer {
        return EXOFloatPlayer(context)
    }
}