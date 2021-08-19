package me.shetj.sdk.video.tx

import android.content.Context
import android.view.View
import com.tencent.rtmp.ui.TXCloudVideoView
import me.shetj.sdk.video.base.IPlayerView


class TXVideoPlayerView(context: Context) : IPlayerView {

    private var playerView: TXCloudVideoView = TXCloudVideoView(context)

    override fun getPlayerView(): TXCloudVideoView {
        return playerView
    }

    fun updatePlayView( playerView: TXCloudVideoView ){
        (this).playerView = playerView
    }
}