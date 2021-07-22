package me.shetj.sdk.video.tx

import android.content.Context
import android.util.AttributeSet
import com.shetj.sdk.video.viedoview.ui.BasePlayerView
import me.shetj.sdk.video.TXVideoFactory
import me.shetj.sdk.video.TXVideoPlayerModel
import me.shetj.sdk.video.model.VideoPlayerModel


class TXPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BasePlayerView(context, attrs, defStyleAttr) {

    private var playerImpl: TXPlayerImpl = TXVideoFactory.getTXPlayer(context)

    init {
        updatePlayer(playerImpl) //设置播放器
        setPlayerView(playerImpl.playView) //设置播放的view
        updateFloatView(TXVideoFactory.getTXFloatView(context)) // 设置悬浮窗
    }

    override fun <T : VideoPlayerModel> playWithModel(model: T) {
        if (model is TXVideoPlayerModel) {
            if (model.videoId != null) {
                playerImpl.play(model.appId, model.videoId!!.fileId, model.videoId!!.pSign)
            } else if (model.multiURLs != null && model.multiURLs!!.isNotEmpty()) {
                playerImpl.play(model.appId, model.multiURLs, model.playDefaultIndex)
            } else {
                playerImpl.play(model.url)
            }
        } else {
            super.playWithModel(model)
        }
    }

}