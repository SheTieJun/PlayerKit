package me.shetj.video.exoplayer

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.net.toUri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.STATE_ENDED
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.video.VideoSize
import me.shetj.sdk.video.base.IPlayerView
import me.shetj.sdk.video.base.OtherKit
import me.shetj.sdk.video.base.PlayerConfig
import me.shetj.sdk.video.model.PlayImageSpriteInfo
import me.shetj.sdk.video.model.PlayKeyFrameDescInfo
import me.shetj.sdk.video.model.VideoPlayerModel
import me.shetj.sdk.video.model.VideoQuality
import me.shetj.sdk.video.player.IPlayerObserver
import me.shetj.sdk.video.player.ISnapshotListener
import me.shetj.sdk.video.player.PlayerDef
import me.shetj.sdk.video.timer.TimerConfigure


@SuppressLint("RestrictedApi")
class EXOPlayerImpl(private val context: Context) : IEXOPlayer {
    private val update_interval_ms = 1000L
    override var playURL: String? = null
    private var mCurrentModel: VideoPlayerModel? = null// 当前播放的model
    override var playerType = PlayerDef.PlayerType.VOD // 当前播放类型
    override var playerMode = PlayerDef.PlayerMode.WINDOW // 当前播放模式
    override var playerState = PlayerDef.PlayerState.END // 当前播放状态
    private var mRotation: Int = 0
    private var isAutoPlay: Boolean = true
    private var mWidth: Int = -1
    private var mHeight: Int = -1
    private var mObserver: IPlayerObserver? = null
    private var mVideoQuality: VideoQuality? = null
    private var mPlaySeekPos = 0// 开始播放直接到对应位置
    private var currentPosition: Long = 0L
    private var duration: Long = 0L
    private var isLoop = false
    private var playerView: IPlayerView? = null
    private val exoplayer: ExoPlayer by lazy { SimpleExoPlayer.Builder(context).build() }
    private var updateProgressAction = Runnable {
        //更新播放进度
        mObserver?.onPlayProgress(
            exoplayer.currentPosition / 1000,
            exoplayer.duration / 1000
        )
        reUpdateProgressAction()
    }

    private fun reUpdateProgressAction() {
        playerView?.getPlayerView()?.postDelayed(updateProgressAction, update_interval_ms)
    }

    init {

        exoplayer.addListener(object : Player.Listener {

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                if (mWidth != videoSize.width && mHeight != videoSize.height ) {
                    mWidth = videoSize.width
                    mHeight = videoSize.height
                    mObserver?.onVideoSize(videoSize.width, videoSize.height)
                }
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                mObserver?.onError(error.type, error.message)
            }


            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    updatePlayerState(PlayerDef.PlayerState.PLAYING)
                    if (mWidth == 0){
                        mWidth = exoplayer.videoSize.width
                        mHeight = exoplayer.videoSize.height
                        mObserver?.onVideoSize(mWidth, mHeight)
                    }
                }
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                super.onIsLoadingChanged(isLoading)
                if (isLoading) {
                    updatePlayerState(PlayerDef.PlayerState.LOADING)
                }
            }

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                mObserver?.onPlayProgress(
                    exoplayer.currentPosition / 1000,
                    exoplayer.duration / 1000
                )
            }

            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                if (state == STATE_ENDED) {
                    mObserver?.onPlayComplete()
                    //优先级：定时
                    if (TimerConfigure.instance.isCourseTime()) {
                        TimerConfigure.instance.stateChange(TimerConfigure.STATE_COMPLETE)
                        updatePlayerState(PlayerDef.PlayerState.END)
                        return
                    }
                    if (isLoop()) {
                        reStart()
                        return
                    }
                    if (TimerConfigure.instance.isRepeatOne()) {
                        reStart()
                        return
                    }
                }
            }
        })
    }

    override fun play(url: String?) {
        if (url == null) return
        val model = VideoPlayerModel()
        model.url = url
        playWithModel(model)
    }


    override fun play(superPlayerURLS: List<VideoPlayerModel.PlayerURL>?, defaultIndex: Int) {
        val model = VideoPlayerModel()
        model.multiURLs = superPlayerURLS
        model.playDefaultIndex = defaultIndex
        playWithModel(model)
    }

    private fun playWithModel(model: VideoPlayerModel) {
        if (model.url.isNullOrEmpty()) {
            if (model.multiURLs.isNullOrEmpty()) return
            var videoURL: String? = null
            val videoQualities: ArrayList<VideoQuality> = ArrayList()
            var defaultVideoQuality: VideoQuality? = null
            if (model.multiURLs != null && model.multiURLs!!.isNotEmpty()) {
                for ((i, superPlayerURL) in model.multiURLs!!.withIndex()) {
                    if (i == model.playDefaultIndex) {
                        videoURL = superPlayerURL.url
                    }
                    videoQualities.add(
                        VideoQuality(
                            i,
                            superPlayerURL.qualityName,
                            superPlayerURL.url
                        )
                    )
                }
                defaultVideoQuality = videoQualities[model.playDefaultIndex]
            }
            mVideoQuality = defaultVideoQuality
            mObserver?.onVideoQualityListChange(videoQualities, defaultVideoQuality)
            this.playURL = videoURL
        } else {
            this.playURL = model.url
        }
        val isLivePlay = OtherKit.isRTMPPlay(this.playURL) || OtherKit.isFLVPlay(this.playURL)
        mObserver?.onPlayerTypeChange(if (isLivePlay) PlayerDef.PlayerType.LIVE else PlayerDef.PlayerType.VOD)
        mCurrentModel = model
        exoplayer.setMediaItem(MediaItem.fromUri(playURL!!.toUri()), mPlaySeekPos * 1000L)
        mPlaySeekPos = 0
        exoplayer.prepare()
        if (isAutoPlay) {
            exoplayer.play()
        }
    }

    override fun reStart() {
        exoplayer.setMediaItem(MediaItem.fromUri(playURL!!.toUri()))
        exoplayer.prepare()
        exoplayer.play()
    }

    override fun pause() {
        exoplayer.playWhenReady = false
        updatePlayerState(PlayerDef.PlayerState.PAUSE)
    }

    override fun resume() {
        exoplayer.playWhenReady = true
    }

    override fun resumeLive() {
        exoplayer.playWhenReady = true
    }

    override fun autoPlay(auto: Boolean) {
        this.isAutoPlay = auto
    }

    override fun setLoopPlay(isLoop: Boolean) {
        this.isLoop = isLoop
    }

    override fun isLoop(): Boolean {
        return isLoop
    }

    override fun getVideoWidth(): Int {
        return mWidth
    }

    override fun getVideoHeight(): Int {
        return mHeight
    }

    override fun getVideoRotation(): Int {
        return mRotation
    }

    override fun stop() {
        exoplayer.stop()
        updatePlayerState(PlayerDef.PlayerState.END)
    }

    private fun updatePlayerState(playState: PlayerDef.PlayerState) {
        if (playState == playerState) return
        playerState = playState
        if (mObserver == null) {
            return
        }
        when (playState) {
            PlayerDef.PlayerState.PLAYING -> {
                playerView?.getPlayerView()?.postDelayed(updateProgressAction, update_interval_ms)
                mObserver!!.onPlayBegin()
            }
            PlayerDef.PlayerState.PAUSE -> {
                playerView?.getPlayerView()?.removeCallbacks(updateProgressAction)
                mObserver!!.onPlayPause()
            }
            PlayerDef.PlayerState.LOADING -> mObserver!!.onPlayLoading()
            PlayerDef.PlayerState.END -> {
                playerView?.getPlayerView()?.removeCallbacks(updateProgressAction)
                mObserver!!.onPlayStop()
            }
        }
    }

    override fun destroy() {
        exoplayer.release()
    }

    override fun switchPlayMode(playerMode: PlayerDef.PlayerMode) {
        if (this.playerMode == playerMode) {
            return
        }
        this.playerMode = playerMode
    }

    @Deprecated("EXO默认是硬件和一些软件功能合作完成")
    override fun enableHardwareDecode(enable: Boolean) {

    }

    override fun setPlayerView(videoView: IPlayerView) {
        if (playerView != null){
            if (playerView!!.getPlayerView() is PlayerView) {
                PlayerView.switchTargetView(exoplayer,playerView!!.getPlayerView() as PlayerView,videoView.getPlayerView() as PlayerView)
            }
        }else{
            if (videoView.getPlayerView() is PlayerView) {
                (videoView.getPlayerView() as PlayerView).player = exoplayer
            }
        }
        this.playerView = videoView
    }

    override fun seek(position: Int, isCallback: Boolean) {
        exoplayer.seekTo(position * 1000L)
        if (isCallback) {
            mObserver?.onSeek(position)
        }
    }

    override fun setPlayToSeek(position: Int) {
        mPlaySeekPos = position
    }

    /**
     * 暂不支持
     */
    override fun snapshot(listener: ISnapshotListener) {
        //TODO
    }

    override fun setPlaySpeed(speedLevel: Float) {
        val playbackParameters = PlaybackParameters(speedLevel, 1.0F)
        exoplayer.setPlaybackParameters(playbackParameters)
    }

    override fun setMirror(isMirror: Boolean) {
        if (playerView!!.getPlayerView() is PlayerView) {
            val videoView = playerView!!.getPlayerView() as PlayerView
            if (isMirror) {
            } else {
            }
        }
    }

    override fun switchStream(quality: VideoQuality) {
        mObserver?.onSwitchStreamStart(true, playerType, quality)
        val url = quality.url ?: return
        this.playURL = url
        mVideoQuality = quality
        exoplayer.setMediaItem(MediaItem.fromUri(playURL!!.toUri()))
        mObserver?.onSwitchStreamEnd(true, playerType, quality)
    }

    override fun changeRenderMode(renderMode: Int) {
        if (playerView!!.getPlayerView() is PlayerView) {
            val videoView = playerView!!.getPlayerView() as PlayerView
            if (renderMode == PlayerConfig.RENDER_MODE_FULL_FILL_SCREEN) {
                videoView.resizeMode =
                    AspectRatioFrameLayout.RESIZE_MODE_FILL
            } else {
                videoView.resizeMode =
                    AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }
    }


    override fun setObserver(observer: IPlayerObserver?) {
        mObserver = observer
    }

    override fun getDuration(): Long {
        return duration
    }

    override fun getPosition(): Long {
        return currentPosition
    }

    override fun updateImageSpriteAndKeyFrame(
        info: PlayImageSpriteInfo?,
        list: ArrayList<PlayKeyFrameDescInfo>?
    ) {
        if (mObserver != null) {
            mObserver!!.onVideoImageSpriteAndKeyFrameChanged(info, list)
        }
    }
}