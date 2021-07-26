package me.shetj.sdk.video.tx

import me.shetj.sdk.video.model.VideoPlayerModel
import me.shetj.sdk.video.player.IPlayer


interface ITXPlayer : IPlayer {

    fun play(appId: Int, url: String?)

    fun play(appId: Int, fileId: String?, psign: String?)

    fun play(
        appId: Int,
        superPlayerURLS: List<VideoPlayerModel.PlayerURL>?,
        defaultIndex: Int
    )
}