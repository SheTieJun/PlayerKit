package me.shetj.video.exoplayer

import android.content.Context
import android.util.AttributeSet
import com.shetj.sdk.video.viedoview.ui.BasePlayerView


class EXOPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BasePlayerView(context, attrs, defStyleAttr) {

    private var playerImpl: EXOPlayerImpl = EXOVideoFactory.getPlayer(context)
    private var playerView: EXOVideoPlayerView = EXOVideoPlayerView(context)

    init {
        updatePlayer(playerImpl) //设置播放器
        setPlayerView(playerView) //设置播放的view
        updateFloatView(EXOVideoFactory.getFloatView(context)) // 设置悬浮窗
    }

}