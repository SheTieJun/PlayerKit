package me.shetj.sdk.video.tx

import android.content.*
import android.os.Bundle
import android.os.Environment.DIRECTORY_MOVIES
import android.text.TextUtils
import com.tencent.liteav.basic.log.TXCLog
import com.tencent.rtmp.*
import com.tencent.rtmp.TXLiveConstants.*
import com.tencent.rtmp.ui.TXCloudVideoView
import me.shetj.sdk.video.TXVideoFactory
import me.shetj.sdk.video.TXVideoPlayerModel
import me.shetj.sdk.video.base.GlobalConfig
import me.shetj.sdk.video.base.IPlayerView
import me.shetj.sdk.video.base.PlayerConfig
import me.shetj.sdk.video.model.*
import me.shetj.sdk.video.player.IPlayerObserver
import me.shetj.sdk.video.player.ISnapshotListener
import me.shetj.sdk.video.player.PlayerDef.*
import me.shetj.sdk.video.protocol.*
import me.shetj.sdk.video.timer.TimerConfigure
import java.util.*
import kotlin.collections.ArrayList

/**
 * 腾讯播放器的实现
 */
class TXPlayerImpl @JvmOverloads constructor(
    context: Context,
    var playView: TXVideoPlayerView = TXVideoFactory.getTXPlayerView(context),
    private val playerConfig: PlayerConfig = PlayerConfig.playerConfig,
) : ITXPlayer,
    ITXVodPlayListener, ITXLivePlayListener {
    private var mRotation: Int = 0
    private var isAutoPlay: Boolean = true
    private var mWidth: Int = -1
    private var mHeight: Int = -1
    private var mContext: Context? = null
    private var mVideoView: TXCloudVideoView? = null// 腾讯云视频播放view
    private var mCurrentProtocol: IPlayInfoProtocol? = null // 当前视频信息协议类
    private var mVodPlayer: TXVodPlayer? = null// 点播播放器
    private var mVodPlayConfig: TXVodPlayConfig? = null// 点播播放器配置
    private var mLivePlayer: TXLivePlayer? = null // 直播播放器
    private var mLivePlayConfig: TXLivePlayConfig? = null// 直播播放器配置
    private var mCurrentModel: TXVideoPlayerModel? = null// 当前播放的model
    private var mObserver: IPlayerObserver? = null
    private var mVideoQuality: VideoQuality? = null
    override var playerType = PlayerType.VOD // 当前播放类型
    override var playerMode = PlayerMode.WINDOW // 当前播放模式
    override var playerState = PlayerState.END // 当前播放状态
    override var playURL: String? = null// 当前播放的URL
    private var mSeekPos = 0 // 记录切换硬解时的播放时间
    private var mPlaySeekPos = 0// 开始播放直接到对应位置
    private var mReportLiveStartTime: Long = -1 // 直播开始时间，用于上报使用时长
    private var mReportVodStartTime: Long = -1 // 点播开始时间，用于上报使用时长
    private var mMaxLiveProgressTime: Long = 0// 观看直播的最大时长
    private var mIsMultiBitrateStream = false// 是否是多码流url播放
    private var mIsPlayWithFileId = false // 是否是腾讯云fileId播放
    private var mDefaultQualitySet = false // 标记播放多码流url时是否设置过默认画质
    private var mChangeHWAcceleration = false// 切换硬解后接收到第一个关键帧前的标记位
    private var currentPosition: Long = 0L
    private var duration: Long = 0L

    //循环播放，必须在播放之前设置
    private var isLoop = false

    /**
     * 直播播放器事件回调
     *
     * @param event
     * @param param
     */
    override fun onPlayEvent(event: Int, param: Bundle) {
        if (event != PLAY_EVT_PLAY_PROGRESS) {
            val playEventLog =
                "TXLivePlayer onPlayEvent event: " + event + ", " + param.getString(EVT_DESCRIPTION)
            TXCLog.d(TAG, playEventLog)
        }
        when (event) {
            PLAY_EVT_VOD_PLAY_PREPARED, PLAY_EVT_PLAY_BEGIN -> updatePlayerState(
                PlayerState.PLAYING
            )
            PLAY_ERR_NET_DISCONNECT, PLAY_EVT_PLAY_END -> if (playerType == PlayerType.LIVE_SHIFT) {  // 直播时移失败，返回直播
                mLivePlayer!!.resumeLive()
                updatePlayerType(PlayerType.LIVE)
                onError(TXPlayerCode.LIVE_SHIFT_FAIL, "时移失败,返回直播")
                updatePlayerState(PlayerState.PLAYING)
            } else {
                stop()
                updatePlayerState(PlayerState.END)
                if (event == PLAY_ERR_NET_DISCONNECT) {
                    onError(TXPlayerCode.NET_ERROR, "网络不给力,点击重试")
                } else {
                    onError(
                        TXPlayerCode.LIVE_PLAY_END,
                        param.getString(EVT_DESCRIPTION)
                    )
                }
            }
            PLAY_EVT_PLAY_LOADING ->
                updatePlayerState(PlayerState.LOADING)
            PLAY_EVT_RCV_FIRST_I_FRAME -> {
            }
            PLAY_EVT_STREAM_SWITCH_SUCC -> updateStreamEndStatus(
                true,
                PlayerType.LIVE,
                mVideoQuality
            )
            PLAY_ERR_STREAM_SWITCH_FAIL -> updateStreamEndStatus(
                false,
                PlayerType.LIVE,
                mVideoQuality
            )
            PLAY_EVT_PLAY_PROGRESS -> {
                val progress = param.getInt(EVT_PLAY_PROGRESS_MS)
                mMaxLiveProgressTime =
                    if (progress > mMaxLiveProgressTime) progress.toLong() else mMaxLiveProgressTime
                updatePlayProgress((progress / 1000).toLong(), mMaxLiveProgressTime / 1000)
            }
            else -> {
            }
        }
    }

    /**
     * 直播播放器网络状态回调
     *
     * @param bundle
     */
    override fun onNetStatus(bundle: Bundle) {
        if (playerState == PlayerState.PLAYING) {
            mWidth = bundle.get(NET_STATUS_VIDEO_WIDTH) as Int
            mHeight = bundle.get(NET_STATUS_VIDEO_HEIGHT) as Int
            mObserver?.onVideoSize(mWidth, mHeight)
        }
    }


    /**
     * 点播播放器事件回调
     *
     * @param player
     * @param event
     * @param param
     */
    override fun onPlayEvent(player: TXVodPlayer, event: Int, param: Bundle) {
        if (event != PLAY_EVT_PLAY_PROGRESS) {
            val playEventLog =
                "TXVodPlayer onPlayEvent event: " + event + ", " + param.getString(EVT_DESCRIPTION)
            TXCLog.d(TAG, playEventLog)
        }
        when (event) {
            PLAY_EVT_VOD_PLAY_PREPARED -> {
                if (isAutoPlay) {
                    updatePlayerState(PlayerState.LOADING)
                }
                mWidth = player.width
                mHeight = player.height
                if (mIsMultiBitrateStream) {
                    val bitrateItems: List<TXBitrateItem>? = mVodPlayer!!.supportedBitrates
                    if (bitrateItems == null || bitrateItems.isEmpty()) {
                        return
                    }
                    bitrateItems.sorted()
                    val videoQualities: ArrayList<VideoQuality> = ArrayList()
                    val size = bitrateItems.size
                    val resolutionNames =
                        if (mCurrentProtocol != null) mCurrentProtocol!!.resolutionNameList else null
                    var i = 0
                    while (i < size) {
                        val bitrateItem = bitrateItems[i]
                        val quality: VideoQuality = if (resolutionNames != null) {
                            TXVideoQualityUtils.convertToVideoQuality(
                                bitrateItem,
                                mCurrentProtocol!!.resolutionNameList
                            )
                        } else {
                            TXVideoQualityUtils.convertToVideoQuality(bitrateItem, i)
                        }
                        videoQualities.add(quality)
                        i++
                    }
                    if (!mDefaultQualitySet) {
                        mVodPlayer!!.bitrateIndex =
                            bitrateItems[bitrateItems.size - 1].index //默认播放码率最高的
                        mDefaultQualitySet = true
                    }
                    updateVideoQualityList(videoQualities, null)
                }
            }
            PLAY_EVT_CHANGE_RESOLUTION -> {
                mWidth = player.width
                mHeight = player.height
                mObserver?.onVideoSize(mWidth, mHeight)
            }
            PLAY_EVT_CHANGE_ROTATION -> {
                mRotation = ((param.getInt(EVT_PARAM1)))
                mWidth = player.width
                mHeight = player.height
                mObserver?.onVideoSize(mWidth, mHeight)
            }
            PLAY_EVT_RCV_FIRST_I_FRAME -> if (mChangeHWAcceleration) { //切换软硬解码器后，重新seek位置
                TXCLog.i(TAG, "seek pos:$mSeekPos")
                seek(mSeekPos)
                mChangeHWAcceleration = false
            }
            PLAY_EVT_PLAY_END -> {
                mObserver?.onPlayComplete()
                //优先级：定时
                if (TimerConfigure.instance.isCourseTime()) {
                    TimerConfigure.instance.stateChange(TimerConfigure.STATE_COMPLETE)
                    updatePlayerState(PlayerState.END)
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
                updatePlayerState(PlayerState.END)
            }
            PLAY_EVT_PLAY_PROGRESS -> {
                val progress = param.getInt(EVT_PLAY_PROGRESS_MS)
                val duration = param.getInt(EVT_PLAY_DURATION_MS)
                this.duration = duration.toLong()
                updatePlayProgress((progress / 1000).toLong(), (duration / 1000).toLong())
                if (playerState == PlayerState.LOADING) {
                    updatePlayerState(PlayerState.PLAYING)
                }
            }
            PLAY_EVT_PLAY_BEGIN -> {
                updatePlayerState(PlayerState.PLAYING)
            }
            PLAY_EVT_PLAY_LOADING -> {

            }
            else -> {

            }
        }
        if (event < 0) { // 播放点播文件失败
            mVodPlayer!!.stopPlay(true)
            updatePlayerState(PlayerState.PAUSE)
            onError(
                TXPlayerCode.VOD_PLAY_FAIL,
                param.getString(EVT_DESCRIPTION)
            )
        }
    }

    /**
     * 点播播放器网络状态回调
     *
     * @param player
     * @param bundle
     */
    override fun onNetStatus(player: TXVodPlayer, bundle: Bundle) {}

    private fun initialize(context: Context?, playView: TXVideoPlayerView) {
        this.mContext = context
        this.playView = playView
        this.mVideoView = playView.getPlayerView()
        initLivePlayer(mContext)
        initVodPlayer(mContext)
    }

    /**
     * 初始化点播播放器
     *
     * @param context
     */
    private fun initVodPlayer(context: Context?) {
        mVodPlayer = TXVodPlayer(context)
        mVodPlayConfig = TXVodPlayConfig().apply {
            val sdcardDir = context!!.getExternalFilesDir(DIRECTORY_MOVIES)
            if (sdcardDir != null) {
                setCacheFolderPath(sdcardDir.path + "/vdcache")
            }
            setCacheMp4ExtName(GlobalConfig.cacheMp4ExtName)
            setHeaders(playerConfig.header)
            setAutoRotate(true)
            setMaxCacheItems(GlobalConfig.maxCacheItem)
        }
        mVodPlayer!!.setRate(GlobalConfig.speed)
        mVodPlayer!!.setConfig(mVodPlayConfig)
        mVodPlayer!!.setRenderMode(playerConfig.renderMode)
        mVodPlayer!!.setVodListener(this)
        mVodPlayer!!.enableHardwareDecode(GlobalConfig.enableHWAcceleration)
    }

    /**
     * 初始化直播播放器
     *
     * @param context
     */
    private fun initLivePlayer(context: Context?) {
        mLivePlayer = TXLivePlayer(context)
        mLivePlayConfig = TXLivePlayConfig()
        mLivePlayer!!.setConfig(mLivePlayConfig)
        mLivePlayer!!.setRenderMode(playerConfig.liveRenderMode)
        mLivePlayer!!.setRenderRotation(RENDER_ROTATION_PORTRAIT)
        mLivePlayer!!.setPlayListener(this)
        mLivePlayer!!.enableHardwareDecode(GlobalConfig.enableHWAcceleration)
    }

    override fun changeRenderMode(renderMode: Int) {
        mLivePlayer?.setRenderMode(renderMode)
        mVodPlayer?.setRenderMode(renderMode)
    }

    /**
     * 播放视频
     *
     * @param model
     */
    private fun playWithModel(model: TXVideoPlayerModel?) {
        mCurrentModel = model
        if (PlayerState.END != playerState) {
            stop()
        }
        val params = PlayInfoParams()
        params.appId = model!!.appId
        if (model.videoId != null) {
            params.fileId = model.videoId!!.fileId
            params.videoId = model.videoId
            mCurrentProtocol = PlayInfoProtocolV4(params)
        } else {
            mCurrentProtocol = null // 当前播放的是非v2和v4协议视频，将其置空
        }
        if (model.videoId != null) { // 根据FileId播放
            mCurrentProtocol!!.sendRequest(object : IPlayInfoRequestCallback {
                override fun onSuccess(protocol: IPlayInfoProtocol?, param: PlayInfoParams) {
                    TXCLog.i(TAG, "onSuccess: protocol params = $param")
                    mReportVodStartTime = System.currentTimeMillis()
                    mVodPlayer!!.setPlayerView(mVideoView)
                    playModeVideo(mCurrentProtocol!!)
                    autoPlay(isAutoPlay)
                    updatePlayerType(PlayerType.VOD)
                    updatePlayProgress(0, 0)
                    updateImageSpriteAndKeyFrame(
                        mCurrentProtocol!!.imageSpriteInfo,
                        mCurrentProtocol!!.keyFrameDescInfo
                    )
                }

                override fun onError(errCode: Int, message: String) {
                    TXCLog.i(TAG, "onFail: errorCode = $errCode message = $message")
                    this@TXPlayerImpl.onError(
                        TXPlayerCode.VOD_REQUEST_FILE_ID_FAIL,
                        "播放视频文件失败 code = $errCode msg = $message"
                    )
                }
            })
        } else { // 根据URL播放
            var videoURL: String? = null
            val videoQualities: ArrayList<VideoQuality> = ArrayList()
            var defaultVideoQuality: VideoQuality? = null
            if (model.multiURLs != null && model.multiURLs!!.isNotEmpty()) { // 多码率URL播放
                for ((i, superPlayerURL) in model.multiURLs!!.withIndex()) {
                    if (i == model.playDefaultIndex) {
                        videoURL = superPlayerURL!!.url
                    }
                    videoQualities.add(
                        VideoQuality(
                            i,
                            superPlayerURL!!.qualityName,
                            superPlayerURL.url
                        )
                    )
                }
                defaultVideoQuality = videoQualities[model.playDefaultIndex]
            } else if (!TextUtils.isEmpty(model.url)) { // 传统URL模式播放
                videoURL = model.url
            }
            if (TextUtils.isEmpty(videoURL)) {
                onError(
                    TXPlayerCode.PLAY_URL_EMPTY,
                    "播放视频失败，播放链接为空"
                )
                return
            }
            if (isRTMPPlay(videoURL)) { // 直播播放器：普通RTMP流播放
                mReportLiveStartTime = System.currentTimeMillis()
                mLivePlayer!!.setPlayerView(mVideoView)
                playLiveURL(videoURL, TXLivePlayer.PLAY_TYPE_LIVE_RTMP)
            } else if (isFLVPlay(videoURL)) { // 直播播放器：直播FLV流播放
                mReportLiveStartTime = System.currentTimeMillis()
                mLivePlayer!!.setPlayerView(mVideoView)
                playTimeShiftLiveURL(model.appId, videoURL)
                if (model.multiURLs != null && model.multiURLs!!.isNotEmpty()) {
                    startMultiStreamLiveURL(videoURL)
                }
            } else { // 点播播放器：播放点播文件
                mReportVodStartTime = System.currentTimeMillis()
                mVodPlayer!!.setPlayerView(mVideoView)
                playVodURL(videoURL)
            }
            val isLivePlay = isRTMPPlay(videoURL) || isFLVPlay(videoURL)
            updatePlayerType(if (isLivePlay) PlayerType.LIVE else PlayerType.VOD)
            updatePlayProgress(0, 0)
            updateVideoQualityList(videoQualities, defaultVideoQuality)
        }
    }

    /**
     * 播放v2或v4协议视频
     *
     * @param protocol
     */
    private fun playModeVideo(protocol: IPlayInfoProtocol) {
        playVodURL(protocol.url)
        val videoQualities = protocol.videoQualityList
        mIsMultiBitrateStream = videoQualities == null
        val defaultVideoQuality = protocol.defaultVideoQuality
        updateVideoQualityList(videoQualities, defaultVideoQuality)
    }

    override fun getVideoRotation() = mRotation

    /**
     * 播放非v2和v4协议视频
     *
     * @param model
     */
    private fun playModeVideo(model: VideoPlayerModel?) {
        if (model!!.multiURLs != null && model.multiURLs!!.isNotEmpty()) { // 多码率URL播放
            for (i in model.multiURLs!!.indices) {
                if (i == model.playDefaultIndex) {
                    playVodURL(model.multiURLs!![i]!!.url)
                }
            }
        } else if (!TextUtils.isEmpty(model.url)) {
            playVodURL(model.url)
        }
    }

    /**
     * 播放直播URL
     */
    private fun playLiveURL(url: String?, playType: Int) {
        playURL = url
        if (mLivePlayer != null) {
            mLivePlayer!!.setPlayListener(this)
            val result = mLivePlayer!!.startPlay(
                url,
                playType
            )
            if (result != 0) {
                TXCLog.e(TAG, "playLiveURL videoURL:$url,result:$result")
            } else {
                updatePlayerState(PlayerState.LOADING)
            }
        }
    }

    /**
     * 播放点播url
     */
    private fun playVodURL(url: String?) {
        if (url == null || "" == url) {
            return
        }
        playURL = url
        if (url.contains(".m3u8")) {
            mIsMultiBitrateStream = true
        }
        if (mVodPlayer != null) {
            mDefaultQualitySet = false
            mVodPlayer!!.setStartTime(mPlaySeekPos.toFloat())
            mPlaySeekPos = 0
            mVodPlayer!!.setAutoPlay(isAutoPlay)
            mVodPlayer!!.setVodListener(this)
            if (mCurrentProtocol != null) {
                TXCLog.d(TAG, "TOKEN: " + mCurrentProtocol!!.token)
                mVodPlayer!!.setToken(mCurrentProtocol!!.token)
            } else {
                mVodPlayer!!.setToken(null)
            }
            mVodPlayer!!.startPlay(url)
            if (!isAutoPlay) {
                updatePlayerState(PlayerState.PAUSE)
            }
        }
        mIsPlayWithFileId = false
    }

    /**
     * 播放时移直播url
     */
    private fun playTimeShiftLiveURL(appId: Int, url: String?) {
        val bizid = url!!.substring(url.indexOf("//") + 2, url.indexOf("."))
        val domian: String = TXVideoFactory.playShiftDomain
        val streamid = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."))
        TXCLog.i(TAG, "bizid:$bizid,streamid:$streamid,appid:$appId")
        playLiveURL(url, TXLivePlayer.PLAY_TYPE_LIVE_FLV)
        var bizidNum = -1
        try {
            bizidNum = bizid.toInt()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            TXCLog.e(TAG, "playTimeShiftLiveURL: bizidNum error = $bizid")
        }
        mLivePlayer!!.prepareLiveSeek(domian, bizidNum)
    }

    /**
     * 配置多码流url
     *
     * @param url
     */
    private fun startMultiStreamLiveURL(url: String?) {
        mLivePlayConfig!!.setAutoAdjustCacheTime(false)
        mLivePlayConfig!!.setMaxAutoAdjustCacheTime(5f)
        mLivePlayConfig!!.setMinAutoAdjustCacheTime(5f)
        mLivePlayer!!.setConfig(mLivePlayConfig)
        if (mObserver != null) {
            mObserver!!.onPlayTimeShiftLive(this, url)
        }
    }

    /**
     * 更新播放进度
     *
     * @param current  当前播放进度(秒)
     * @param duration 总时长(秒)
     */
    private fun updatePlayProgress(current: Long, duration: Long) {
        this.currentPosition = current
        if (mObserver != null) {
            mObserver!!.onPlayProgress(current, duration)
        }
    }

    /**
     * 更新播放类型
     *
     * @param playType
     */
    private fun updatePlayerType(playType: PlayerType) {
        if (playType != playerType) {
            playerType = playType
        }
        if (mObserver != null) {
            mObserver!!.onPlayerTypeChange(playType)
        }
    }

    /**
     * 更新播放状态
     *
     * @param playState
     */
    private fun updatePlayerState(playState: PlayerState) {
        if (playState == playerState) return
        playerState = playState
        if (mObserver == null) {
            return
        }
        when (playState) {
            PlayerState.PLAYING -> mObserver!!.onPlayBegin()
            PlayerState.PAUSE -> mObserver!!.onPlayPause()
            PlayerState.LOADING -> mObserver!!.onPlayLoading()
            PlayerState.END -> mObserver!!.onPlayStop()
        }
    }

    private fun updateStreamStartStatus(
        success: Boolean,
        playerType: PlayerType,
        quality: VideoQuality
    ) {
        if (mObserver != null) {
            mObserver!!.onSwitchStreamStart(success, playerType, quality)
        }
    }

    private fun updateStreamEndStatus(
        success: Boolean,
        playerType: PlayerType,
        quality: VideoQuality?
    ) {
        if (mObserver != null) {
            mObserver!!.onSwitchStreamEnd(success, playerType, quality)
        }
    }

    private fun updateVideoQualityList(
        videoQualities: ArrayList<VideoQuality>?,
        defaultVideoQuality: VideoQuality?
    ) {
        if (mObserver != null) {
            mObserver!!.onVideoQualityListChange(videoQualities, defaultVideoQuality)
        }
    }

    override fun updateImageSpriteAndKeyFrame(
        info: PlayImageSpriteInfo?,
        list: ArrayList<PlayKeyFrameDescInfo>?
    ) {
        if (mObserver != null) {
            mObserver!!.onVideoImageSpriteAndKeyFrameChanged(info, list)
        }
    }

    private fun onError(code: Int, message: String?) {
        if (mObserver != null) {
            mObserver!!.onError(code, message)
        }
    }

    protected val playName: String?
        get() {
            var title: String? = ""
            if (mCurrentModel != null && !TextUtils.isEmpty(mCurrentModel!!.title)) {
                title = mCurrentModel!!.title
            } else if (mCurrentProtocol != null && !TextUtils.isEmpty(mCurrentProtocol!!.name)) {
                title = mCurrentProtocol!!.name
            }
            return title
        }

    /**
     * 是否是RTMP协议
     *
     * @param videoURL
     * @return
     */
    private fun isRTMPPlay(videoURL: String?): Boolean {
        return !TextUtils.isEmpty(videoURL) && videoURL!!.startsWith("rtmp")
    }

    /**
     * 是否是HTTP-FLV协议
     *
     * @param videoURL
     * @return
     */
    private fun isFLVPlay(videoURL: String?): Boolean {
        return (!TextUtils.isEmpty(videoURL) && videoURL!!.startsWith("http://")
                || videoURL!!.startsWith("https://")) && videoURL.contains(".flv")
    }

    override fun play(url: String?) {
        val model = TXVideoPlayerModel()
        model.url = url!!
        playWithModel(model)
    }

    override fun play(appId: Int, url: String?) {
        val model = TXVideoPlayerModel()
        model.appId = appId
        model.url = url!!
        playWithModel(model)
    }

    override fun play(appId: Int, fileId: String?, psign: String?) {
        val videoId = TXPlayerVideoId()
        videoId.fileId = fileId
        videoId.pSign = psign
        val model = TXVideoPlayerModel()
        model.appId = appId
        model.videoId = videoId
        playWithModel(model)
    }

    override fun play(
        appId: Int,
        superPlayerURLS: List<VideoPlayerModel.PlayerURL?>?,
        defaultIndex: Int
    ) {
        val model = TXVideoPlayerModel()
        model.appId = appId
        model.multiURLs = superPlayerURLS
        model.playDefaultIndex = defaultIndex
        playWithModel(model)
    }

    override fun play(
        superPlayerURLS: List<VideoPlayerModel.PlayerURL?>?,
        defaultIndex: Int
    ) {
        val model = TXVideoPlayerModel()
        model.multiURLs = superPlayerURLS
        model.playDefaultIndex = defaultIndex
        playWithModel(model)
    }


    override fun reStart() {
        if (playerType == PlayerType.LIVE || playerType == PlayerType.LIVE_SHIFT) {
            if (isRTMPPlay(playURL)) {
                playLiveURL(playURL, TXLivePlayer.PLAY_TYPE_LIVE_RTMP)
            } else if (isFLVPlay(playURL)) {
                playTimeShiftLiveURL(mCurrentModel!!.appId, playURL)
                if (mCurrentModel!!.multiURLs != null && mCurrentModel!!.multiURLs!!.isNotEmpty()) {
                    startMultiStreamLiveURL(playURL)
                }
            }
        } else {
            playVodURL(playURL)
            mVodPlayer!!.setAutoPlay(true)
        }

    }

    override fun pause() {
        if (playerType == PlayerType.VOD) {
            mVodPlayer!!.pause()
        } else {
            mLivePlayer!!.pause()
        }
        updatePlayerState(PlayerState.PAUSE)
    }


    override fun resume() {
        if (playerType == PlayerType.VOD) {
            mVodPlayer!!.resume()
        } else {
            mLivePlayer!!.resume()
        }
        updatePlayerState(PlayerState.PLAYING)
    }

    override fun resumeLive() {
        if (playerType == PlayerType.LIVE_SHIFT) {
            mLivePlayer!!.resumeLive()
        }
        updatePlayerType(PlayerType.LIVE)
    }

    override fun autoPlay(auto: Boolean) {
        this.isAutoPlay = auto
        if (playerType == PlayerType.VOD) {
            mVodPlayer?.setAutoPlay(auto)
        }
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


    override fun stop() {
        if (mVodPlayer != null) {
            mVodPlayer!!.stopPlay(false)
        }
        if (mLivePlayer != null) {
            mLivePlayer!!.stopPlay(false)
            mVideoView!!.removeVideoView()
        }
        updatePlayerState(PlayerState.END)
    }

    override fun destroy() {
        duration = 0
        currentPosition = 0
        mCurrentModel = null
        if (mVodPlayer != null) {
            mVodPlayer!!.stopPlay(true)
        }
        if (mLivePlayer != null) {
            mLivePlayer!!.stopPlay(true)
            mVideoView!!.removeVideoView()
        }
    }

    override fun switchPlayMode(playerMode: PlayerMode) {
        if (this.playerMode == playerMode) {
            return
        }
        this.playerMode = playerMode
    }

    override fun enableHardwareDecode(enable: Boolean) {
        if (playerType == PlayerType.VOD) {
            mChangeHWAcceleration = true
            mVodPlayer!!.enableHardwareDecode(enable)
            mSeekPos = mVodPlayer!!.currentPlaybackTime.toInt()
            TXCLog.i(TAG, "save pos:$mSeekPos")
            stop()
            if (mCurrentProtocol == null) {    // 当protocol为空时，则说明当前播放视频为非v2和v4视频
                playModeVideo(mCurrentModel)
            } else {
                playModeVideo(mCurrentProtocol!!)
            }
            autoPlay(true)
        } else {
            mLivePlayer!!.enableHardwareDecode(enable)
            playWithModel(mCurrentModel)
        }
    }

    /**
     * 更新播放的view
     */
    override fun setPlayerView(videoView: IPlayerView) {
        if (videoView.getPlayerView() is TXCloudVideoView) {
            (videoView.getPlayerView() as? TXCloudVideoView)?.let {
                this.mVideoView = it

                if (playerType == PlayerType.VOD) {
                    mVodPlayer!!.setPlayerView(it)
                } else {
                    mLivePlayer!!.setPlayerView(it)
                }
            }
        }
    }

    /**
     * 只有播放前设置有效
     */
    override fun setPlayToSeek(position: Int) {
        if (playerState == PlayerState.END) {
            mPlaySeekPos = position
        } else {
            seek(position)
        }
    }

    override fun seek(position: Int, isCallback: Boolean) {
        if (playerType == PlayerType.VOD) {
            if (mVodPlayer != null) {
                mVodPlayer!!.seek(position)
            }
        } else {
            updatePlayerType(PlayerType.LIVE_SHIFT)
            if (mLivePlayer != null) {
                mLivePlayer!!.seek(position)
            }
        }
        if (isCallback) {
            if (mObserver != null) {
                mObserver!!.onSeek(position)
            }
        }
    }


    override fun setPlaySpeed(speedLevel: Float) {
        if (playerType == PlayerType.VOD) {
            mVodPlayer!!.setRate(speedLevel)
        }
    }

    override fun setMirror(isMirror: Boolean) {
        if (playerType == PlayerType.VOD) {
            mVodPlayer!!.setMirror(isMirror)
        }
    }

    override fun switchStream(quality: VideoQuality) {
        mVideoQuality = quality
        if (playerType == PlayerType.VOD) {
            if (mVodPlayer != null) {
                if (quality.url != null) {
                    val isPlay = playerState == PlayerState.PLAYING
                    val currentTime = mVodPlayer!!.currentPlaybackTime
                    mVodPlayer!!.stopPlay(true)
                    TXCLog.i(TAG, "onQualitySelect quality.url:" + quality.url)
                    mVodPlayer!!.setStartTime(currentTime)
                    mVodPlayer!!.startPlay(quality.url)
                    mVodPlayer!!.setAutoPlay(isPlay)
                } else { //br!=0;index!=-1;url=null
                    TXCLog.i(TAG, "setBitrateIndex quality.index:" + quality.index)
                    mVodPlayer!!.bitrateIndex = quality.index
                }
                updateStreamStartStatus(true, PlayerType.VOD, quality)
            }
        } else {
            var success = false
            if (mLivePlayer != null && !TextUtils.isEmpty(quality.url)) {
                val result = mLivePlayer!!.switchStream(quality.url)
                success = result >= 0
            }
            updateStreamStartStatus(success, PlayerType.LIVE, quality)
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

    companion object {
        private const val TAG = "PlayerKit"
    }

    init {
        initialize(context, playView)
    }

    override fun snapshot(listener: ISnapshotListener) {
        val listener2 = TXLivePlayer.ITXSnapshotListener { p0 -> listener.onSnapshot(p0) }
        when (playerType) {
            PlayerType.VOD -> {
                mVodPlayer!!.snapshot(listener2)
            }
            PlayerType.LIVE -> {
                mLivePlayer!!.snapshot(listener2)
            }
            else -> {
                listener.onSnapshot(null)
            }
        }
    }
}