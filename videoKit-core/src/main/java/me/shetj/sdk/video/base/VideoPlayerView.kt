package me.shetj.sdk.video.base

import android.view.View


open class VideoPlayerView<T :View> {

    private var playerView: T? = null

    fun getPlayerView() = playerView!!

    fun setPlayerView(view: T) {
        playerView = view
    }
}