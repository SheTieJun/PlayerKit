package com.tencent.video.superplayer.base

import android.graphics.Bitmap
import com.tencent.video.superplayer.viedoview.player.OnPlayerCallback


typealias OnSeekComplete = (position: Int) -> Unit

typealias OnVideoSize = (width: Int, height: Int) -> Unit

typealias OnStartFullScreen = () -> Unit

typealias OnStartFloatWindow = () -> Unit

typealias OnStopFullScreen = () -> Unit

typealias OnSpeedUpdate = (speed: Float) -> Unit

typealias OnLoading = () -> Unit

typealias OnStart = () -> Unit

typealias OnPause = () -> Unit

typealias OnResume = () -> Unit

typealias OnPlayProgress = (current: Long, duration: Long) -> Unit

typealias OnComplete = () -> Unit

typealias OnShare = () -> Unit

typealias OnStopFloatWindow = () -> Unit

typealias OnSnapshot = (bitmap: Bitmap) -> Unit

typealias OnResumeLive = () -> Unit

typealias OnClickSmallReturn = () -> Unit

typealias OnSpeedChange = (speed: Float) -> Unit

typealias OnShotScreen = () -> Unit

typealias OnStop = () ->Unit

typealias OnError = (code: Int, message: String?)  ->Unit

class VideoViewCallbackBuilder {

    var onSeekComplete: OnSeekComplete? = null

    var onVideoSize: OnVideoSize? = null

    var onStartFullScreen: OnStartFullScreen? = null

    var onStartFloatWindow: OnStartFloatWindow? = null

    var onClickSmallReturn: OnClickSmallReturn? = null

    var onStopFullScreen: OnStopFullScreen? = null

    var onSpeedUpdate: OnSpeedUpdate? = null

    var onLoading: OnLoading? = null

    var onStart: OnStart? = null

    var onPause: OnPause? = null

    var onResume: OnResume? = null

    var onStop: OnStop? = null

    var onPlayProgress: OnPlayProgress? = null

    var onComplete: OnComplete? = null

    var onShare: OnShare? = null

    var onStopFloatWindow: OnStopFloatWindow? = null

    var onSnapshot: OnSnapshot? = null

    var onResumeLive: OnResumeLive? = null

    var onSpeedChange: OnSpeedChange? = null

    var onShotScreen: OnShotScreen? = null

    var onError:OnError?= null

    companion object {

        inline fun build(block: VideoViewCallbackBuilder.() -> Unit) =
            VideoViewCallbackBuilder().apply(block).build()
    }

    fun build(): OnPlayerCallback {
        return object : OnPlayerCallback {
            override fun onStartFullScreenPlay() {
                onStartFullScreen?.invoke()
            }

            override fun onStopFullScreenPlay() {
                onStopFullScreen?.invoke()
            }

            override fun onClickFloatCloseBtn() {
                onStartFloatWindow?.invoke()
            }

            override fun onClickSmallReturnBtn() {
                onClickSmallReturn?.invoke()
            }

            override fun onStartFloatWindowPlay() {
                onStartFloatWindow?.invoke()
            }

            override fun onStopFloatWindow() {
                onStopFloatWindow?.invoke()
            }

            override fun onClickShare() {
                onShare?.invoke()
            }

            override fun onPlayProgress(current: Long, duration: Long) {
                onPlayProgress?.invoke(current, duration)
            }

            override fun onVideoSize(width: Int, height: Int) {
                onVideoSize?.invoke(width, height)
            }

            override fun onSnapshot(bitmap: Bitmap) {
                onSnapshot?.invoke(bitmap)
            }

            override fun onLoading() {
                onLoading?.invoke()
            }

            override fun onStart() {
                onStart?.invoke()
            }

            override fun onPause() {
                onPause?.invoke()
            }

            override fun onResume() {
                onResume?.invoke()
            }

            override fun onStop() {
                onStop?.invoke()
            }

            override fun onComplete() {
                onComplete?.invoke()
            }

            override fun onSeekTo(position: Int) {
                onSeekComplete?.invoke(position)
            }

            override fun onResumeLive() {
                onResumeLive?.invoke()
            }

            override fun onSpeedChange(speed: Float) {
                onSpeedChange?.invoke(speed)
            }

            override fun onShotScreen() {
                onShotScreen?.invoke()
            }

            override fun onError(code: Int, message: String?) {
                onError?.invoke(code, message)
            }

        }
    }
}

