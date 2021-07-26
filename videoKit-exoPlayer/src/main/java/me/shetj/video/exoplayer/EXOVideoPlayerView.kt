package me.shetj.video.exoplayer

import android.content.Context
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import me.shetj.sdk.video.base.IPlayerView


class EXOVideoPlayerView(context: Context) : IPlayerView {

    private var playerView: PlayerView = EXOVideoFactory.getPlayerView(context)

    override fun getPlayerView(): PlayerView {
        return playerView
    }

    fun updatePlayView( playerView: PlayerView ){
        (this).playerView = playerView
    }
}