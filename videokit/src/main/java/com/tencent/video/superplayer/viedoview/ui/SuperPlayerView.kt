package com.tencent.video.superplayer.viedoview.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import com.tencent.liteav.superplayer.R
import com.tencent.liteav.superplayer.databinding.SuperplayerVodViewBinding
import com.tencent.rtmp.TXLivePlayer
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.video.superplayer.base.*
import com.tencent.video.superplayer.base.timer.TimerConfigure
import com.tencent.video.superplayer.casehelper.KeyListListener
import com.tencent.video.superplayer.casehelper.onNext
import com.tencent.video.superplayer.kit.PlayerKit.checkOp
import com.tencent.video.superplayer.model.entity.PlayImageSpriteInfo
import com.tencent.video.superplayer.model.entity.PlayKeyFrameDescInfo
import com.tencent.video.superplayer.model.entity.VideoQuality
import com.tencent.video.superplayer.viedoview.base.AbBaseUIPlayer
import com.tencent.video.superplayer.viedoview.base.SuperPlayerDef.*
import com.tencent.video.superplayer.viedoview.base.UIPlayer
import com.tencent.video.superplayer.viedoview.model.SuperPlayerModel
import com.tencent.video.superplayer.viedoview.player.OnPlayerCallback
import com.tencent.video.superplayer.viedoview.player.SuperPlayer
import com.tencent.video.superplayer.viedoview.player.SuperPlayerImpl
import com.tencent.video.superplayer.viedoview.player.SuperPlayerObserver

/**
 *
 * 具备播放器基本功能，
 * 此外还包括横竖屏切换、
 * 悬浮窗播放、画质切换、
 * 硬件加速、倍速播放、镜像播放、手势控制等功能，同时支持直播与点播
 */
open class SuperPlayerView : FrameLayout, TimerConfigure.CallBack, SuperPlayer, UIPlayer,
    ConfigInterface, SuperPlayerObserver, KeyListListener {

    protected var parentViewGroup: ViewGroup? = null
    protected var showInFullView: ViewGroup? = null
    protected var playerConfig: PlayerConfig = PlayerConfig.playerConfig
    protected var uiConfig: UIConfig = UIConfig.uiConfig
    protected val playKeyListConfig: PlayKeyListConfig by lazy { PlayKeyListConfig.ofDef() }
    protected var mContext: Context? = null
    protected lateinit var mSuperPlayer: SuperPlayer

    protected var mViewBinding: SuperplayerVodViewBinding? = null

    // 全屏模式控制view
    protected var mFullScreenPlayer: FullScreenPlayer? = null

    // 窗口模式控制view
    protected var mWindowPlayer: WindowPlayer? = null

    // 悬浮窗模式控制view
    protected var mFloatPlayer: FloatPlayer? = null

    // 腾讯云视频播放view
    protected var mTXCloudVideoView: TXCloudVideoView? = null
    private var mLayoutParamWindowMode: ViewGroup.LayoutParams? = null// 窗口播放时SuperPlayerView的布局参数

    // 全屏controller的布局参数
    protected var mVodControllerParams: LayoutParams? = null

    // 悬浮窗窗口管理器
    protected var mWindowManager: WindowManager? = null

    // 悬浮窗布局参数
    protected var mWindowParams: WindowManager.LayoutParams? = null

    protected val uiPlayerList = ArrayList<AbBaseUIPlayer>()
    protected var onPlayerCallback: OnPlayerCallback? = null

    //释放循环播放
    var isLoop = false


    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize(context)
    }

    private fun initialize(context: Context) {
        mContext = if (context !is Activity && context is ContextWrapper) {
            context.baseContext
        } else {
            context
        }
        TimerConfigure.instance.addCallBack(this)
        initView()
        initPlayer()
    }

    //ignore
    override fun isDrag(): Boolean {
        return false
    }

    /**
     * 初始化view
     */
    private fun initView() {
        mVodControllerParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        mViewBinding = SuperplayerVodViewBinding.inflate(LayoutInflater.from(mContext), this, true)
        mViewBinding?.apply {
            mTXCloudVideoView = superplayerCloudVideoView
            mFullScreenPlayer = superplayerControllerLarge
            mWindowPlayer = superplayerControllerSmall
            mFloatPlayer = superplayerControllerFloat
            uiPlayerContent.apply {
                removeView(mTXCloudVideoView)
                removeView(mFullScreenPlayer)
                removeView(mWindowPlayer)
                removeView(superplayerControllerFloat)
            }
            //添加到列表方便操作
            uiPlayerList.add(superplayerControllerLarge)
            uiPlayerList.add(superplayerControllerSmall)
            uiPlayerList.add(superplayerControllerFloat)
            ivStartPlayer.setOnClickListener {
                when (playerState) {
                    PlayerState.END -> {
                        reStart()
                    }
                    PlayerState.PAUSE -> {
                        resume()
                    }
                    else -> {
                    }
                }
            }
        }
        post {
            if (mSuperPlayer.playerMode == PlayerMode.WINDOW) {
                mLayoutParamWindowMode = layoutParams
            }
        }
        setUICallback(mControllerCallback)
        setPlayConfig(playerConfig)
        setUIConfig(uiConfig)
    }

    //初始话播放器
    private fun initPlayer() {
        mSuperPlayer = SuperPlayerImpl(mContext, mTXCloudVideoView)
        setObserver(this)
        if (mSuperPlayer.playerMode == PlayerMode.FULLSCREEN) {
            mViewBinding?.uiPlayerContent?.addView(mFullScreenPlayer)
            mFullScreenPlayer!!.hide()
        } else if (mSuperPlayer.playerMode == PlayerMode.WINDOW) {
            mViewBinding?.uiPlayerContent?.addView(mWindowPlayer)
            mWindowPlayer!!.hide()
        }
    }

    fun setPlayerCallback(onPlayerCallback: OnPlayerCallback) {
        this.onPlayerCallback = onPlayerCallback
    }

    /**
     * 设置全屏展示的window
     */
    fun setFullInWindow(window: Window) {
        showInFullView = window.decorView.findViewById(android.R.id.content)
    }

    fun setFullInWindow(view: ViewGroup) {
        showInFullView = view
    }

    override fun onTick(progress: Long) {
    }

    override fun onStateChange(state: Int) {
    }

    override fun onChangeModel(repeatMode: Int) {
        if (repeatMode == TimerConfigure.REPEAT_MODE_ALL) {
            showMsg("已切换为顺序播放", "顺序播放")
        } else {
            showMsg("已切换为单课循环", "单课循环")
        }
    }

    open fun showMsg(text: String?, hideText: String?) {

    }

    open fun showToast(resId: Int) {
        Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show()
    }

    open fun showToast(message: String?) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }

    open fun playWithModel(model: SuperPlayerModel) {
        if (model.videoId != null) {
            mSuperPlayer.play(model.appId, model.videoId!!.fileId, model.videoId!!.pSign)
        } else if (model.multiURLs != null && model.multiURLs!!.isNotEmpty()) {
            mSuperPlayer.play(model.appId, model.multiURLs, model.playDefaultIndex)
        } else {
            mSuperPlayer.play(model.url)
        }
    }

    override fun play(url: String?) {
        mSuperPlayer.play(url)
    }

    override fun play(appId: Int, url: String?) {
        mSuperPlayer.play(appId, url)
    }

    override fun play(appId: Int, fileId: String?, psign: String?) {
        mSuperPlayer.play(appId, fileId, psign)
    }

    override fun play(
        appId: Int,
        superPlayerURLS: List<SuperPlayerModel.SuperPlayerURL?>?,
        defaultIndex: Int
    ) {
        mSuperPlayer.play(appId, superPlayerURLS, defaultIndex)
    }

    override fun reStart() {
        mSuperPlayer.reStart()
    }

    override fun pause() {
        mSuperPlayer.pause()
    }

    override fun resume() {
        mSuperPlayer.resume()
    }

    override fun resumeLive() {
        mSuperPlayer.resumeLive()
    }

    /**
     * 必须在播放链接设置之前[play]设置
     */
    override fun autoPlay(auto: Boolean) {
        mSuperPlayer.autoPlay(auto)
    }

    override fun getVideoWidth(): Int {
        return mSuperPlayer.getVideoWidth()
    }

    override fun getVideoHeight(): Int {
        return mSuperPlayer.getVideoHeight()
    }

    override fun getVideoRotation(): Int {
        return mSuperPlayer.getVideoRotation()
    }

    override fun stop() {
        mSuperPlayer.stop()
    }

    override fun destroy() {
        mSuperPlayer.destroy()
        release()
        onDestroy()
    }

    /**
     * 设置播放模式
     *  [PlayerMode.WINDOW]：窗口模式 ；[PlayerMode.FULLSCREEN]: 全屏模式 ；[PlayerMode.FLOAT]: 悬浮窗模式
     */
    override fun switchPlayMode(playerMode: PlayerMode) {
        mControllerCallback.onSwitchPlayMode(playerMode)
    }

    override fun enableHardwareDecode(enable: Boolean) {
        mSuperPlayer.enableHardwareDecode(enable)
    }

    override fun setPlayerView(videoView: TXCloudVideoView?) {
        mSuperPlayer.setPlayerView(videoView)
    }

    override fun seek(position: Int, isCallback: Boolean) {
        mSuperPlayer.seek(position, isCallback)
    }

    override fun setPlayToSeek(position: Int) {
        mSuperPlayer.setPlayToSeek(position)
    }

    override fun snapshot(listener: TXLivePlayer.ITXSnapshotListener) {
        mSuperPlayer.snapshot(listener)
    }

    override fun setPlaySpeed(speedLevel: Float) {
        mSuperPlayer.setPlaySpeed(speedLevel)
    }

    override fun setMirror(isMirror: Boolean) {
        mSuperPlayer.setMirror(isMirror)
    }

    override fun switchStream(quality: VideoQuality) {
        mSuperPlayer.switchStream(quality)
    }

    override fun changeRenderMode(renderMode: Int) {
        mSuperPlayer.changeRenderMode(renderMode)
    }

    override var playURL: String? = null
        get() = mSuperPlayer.playURL
    override val playerType: PlayerType
        get() = mSuperPlayer.playerType
    override val playerMode: PlayerMode
        get() = mSuperPlayer.playerMode
    override val playerState: PlayerState
        get() = mSuperPlayer.playerState


    override fun setObserver(observer: SuperPlayerObserver?) {
        mSuperPlayer.setObserver(observer)
    }

    override fun getDuration(): Long {
        return mSuperPlayer.getDuration()
    }

    override fun getPosition(): Long {
        return mSuperPlayer.getPosition()
    }

    override fun setUICallback(callback: UIPlayer.VideoViewCallback?) {
        uiPlayerList.forEach {
            it.setUICallback(callback)
        }
    }

    override fun setWatermark(bmp: Bitmap?, x: Float, y: Float) {
        uiPlayerList.forEach {
            it.setWatermark(bmp, x, y)
        }
    }

    override fun showLoading() {
        uiPlayerList.forEach {
            it.showLoading()
        }
    }

    override fun hideLoading() {
        uiPlayerList.forEach {
            it.hideLoading()
        }
    }

    override fun show() {
        uiPlayerList.forEach {
            it.show()
        }
    }

    override fun hide() {
        uiPlayerList.forEach {
            it.hide()
        }
    }

    override fun release() {
        uiPlayerList.forEach {
            it.release()
        }
    }

    override fun updatePlayState(playState: PlayerState?) {
        uiPlayerList.forEach {
            it.updatePlayState(playState)
        }
        checkShowLoading(playState == PlayerState.LOADING)
        showPlayBtn(playState != PlayerState.PLAYING && playState != PlayerState.LOADING)
    }

    private fun checkShowLoading(isShowLoading: Boolean) {
        if (isShowLoading) {
            showLoading()
        } else {
            hideLoading()
        }
    }

    private fun showPlayBtn(isShow: Boolean) {
        mViewBinding?.ivStartPlayer?.isVisible = isShow
        mViewBinding?.halfTranBg?.isVisible = isShow
    }

    override fun setVideoQualityList(list: ArrayList<VideoQuality>?) {
        uiPlayerList.forEach {
            it.setVideoQualityList(list)
        }
    }

    override fun updateSpeedChange(speedLevel: Float) {
        mSuperPlayer.setPlaySpeed(speedLevel)
        uiPlayerList.forEach {
            it.updateSpeedChange(speedLevel)
        }
        onPlayerCallback?.onSpeedChange(speedLevel)
    }

    override fun updateTitle(title: String?) {
        uiPlayerList.forEach {
            it.updateTitle(title)
        }
    }

    override fun updateVideoProgress(current: Long, duration: Long) {
        uiPlayerList.forEach {
            it.updateVideoProgress(current, duration)
        }
    }

    override fun updatePlayType(type: PlayerType?) {
        uiPlayerList.forEach {
            it.updatePlayType(type)
        }
    }

    override fun setBackground(bitmap: Bitmap?) {
        uiPlayerList.forEach {
            it.setBackground(bitmap)
        }
    }

    override fun showBackground() {
        uiPlayerList.forEach {
            it.showBackground()
        }
    }

    override fun hideBackground() {
        uiPlayerList.forEach {
            it.hideBackground()
        }
    }

    override fun updateVideoQuality(videoQuality: VideoQuality?) {
        uiPlayerList.forEach {
            it.updateVideoQuality(videoQuality)
        }
    }

    override fun updateImageSpriteInfo(info: PlayImageSpriteInfo?) {
        uiPlayerList.forEach {
            it.updateImageSpriteInfo(info)
        }
    }

    override fun updateKeyFrameDescInfo(list: ArrayList<PlayKeyFrameDescInfo>?) {
        uiPlayerList.forEach {
            it.updateKeyFrameDescInfo(list)
        }
    }

    override fun onPlayBegin() {
        updatePlayState(PlayerState.PLAYING)
        onPlayerCallback?.onStart()
    }

    override fun onPlayPause() {
        updatePlayState(PlayerState.PAUSE)
        onPlayerCallback?.onPause()

    }

    override fun onPlayStop() {
        updatePlayState(PlayerState.END)
        onPlayerCallback?.onStop()
        uiPlayerList.forEach {
            it.updateImageSpriteInfo(null)
            it.updateKeyFrameDescInfo(null)
        }
    }

    override fun onPlayLoading() {
        updatePlayState(PlayerState.LOADING)
        onPlayerCallback?.onLoading()
    }

    override fun onPlayComplete() {
        if (TimerConfigure.instance.isCourseTime()) {
            TimerConfigure.instance.stateChange(TimerConfigure.STATE_COMPLETE)
            onPlayerCallback?.onComplete()
            return
        }
        if (isLoop) {
            mSuperPlayer.reStart()
            return
        }
        if (TimerConfigure.instance.isRepeatOne()) {
            mSuperPlayer.reStart()
            return
        } else {
            onPlayerCallback?.onComplete()
            nextOneKey()
        }
    }

    /**
     * 如果是列表播放自定一下一集
     */
    override fun nextOneKey() {
        val newPosition = playKeyListConfig.position + 1
        if (playKeyListConfig.keyList?.size ?: 0 > newPosition) {
            playKeyListConfig.keyList!![newPosition]?.apply {
                playKeyListConfig.onNext?.invoke(this)
                updateListPosition(newPosition)
            }
        }
    }

    /***
     * 指定当前播放对应的keyList的位置
     */
    override fun updateListPosition(position: Int) {
        playKeyListConfig.position = position
        uiPlayerList.forEach {
            it.updateListPosition(position)
        }
    }

    override fun setKeyList(
        name: String?,
        adapter: BaseKitAdapter<*>?,
        position: Int,
        onNext: onNext?
    ) {
        uiPlayerList.forEach {
            it.setKeyList(name, adapter, position, onNext)
        }
        playKeyListConfig.onNext = onNext
        playKeyListConfig.keyList = adapter?.data
    }

    override fun onVideoSize(width: Int, height: Int) {
        onPlayerCallback?.onVideoSize(width, height)
    }


    override fun onPlayProgress(current: Long, duration: Long) {
        uiPlayerList.forEach {
            it.updateVideoProgress(current, duration)
        }
        onPlayerCallback?.onPlayProgress(current, duration)
    }

    override fun onSeek(position: Int) {
        onPlayerCallback?.onSeekTo(position)
    }

    override fun onSwitchStreamStart(
        success: Boolean,
        playerType: PlayerType,
        quality: VideoQuality
    ) {
        if (playerType == PlayerType.LIVE) {
            if (success) {
                showToast("正在切换到${quality.title}...")
            } else {
                showToast("切换${quality.title}清晰度失败，请稍候重试")
            }
        }
    }

    override fun onSwitchStreamEnd(
        success: Boolean,
        playerType: PlayerType,
        quality: VideoQuality?
    ) {
        if (playerType == PlayerType.LIVE) {
            if (success) {
                showToast("清晰度切换成功")
            } else {
                showToast("清晰度切换失败")
            }
        }
    }

    override fun onError(code: Int, message: String?) {
        if (message != null) {
            showMsg(message, "")
        }
        onPlayerCallback?.onError(code, message)
    }

    override fun onPlayerTypeChange(playType: PlayerType?) {
        uiPlayerList.forEach {
            it.updatePlayType(playType)
        }
    }

    override fun onPlayTimeShiftLive(player: TXLivePlayer?, url: String?) {
    }

    override fun onVideoQualityListChange(
        videoQualities: ArrayList<VideoQuality>?,
        defaultVideoQuality: VideoQuality?
    ) {
        uiPlayerList.forEach {
            it.setVideoQualityList(videoQualities)
        }
        if (defaultVideoQuality != null) {
            uiPlayerList.forEach {
                it.updateVideoQuality(defaultVideoQuality)
            }
        }
    }

    override fun onVideoImageSpriteAndKeyFrameChanged(
        info: PlayImageSpriteInfo?,
        list: ArrayList<PlayKeyFrameDescInfo>?
    ) {
        uiPlayerList.forEach {
            it.updateImageSpriteInfo(info)
            it.updateKeyFrameDescInfo(list)
        }
    }

    open fun onBackPressed(): Boolean {
        return if (mSuperPlayer.playerMode == PlayerMode.WINDOW
            || mSuperPlayer.playerMode == PlayerMode.FLOAT
        ) {
            true
        } else {
            switchPlayMode(PlayerMode.WINDOW)
            false
        }
    }

    override fun onDestroy() {
        playKeyListConfig.clear()
        uiPlayerList.forEach {
            it.onDestroy()
        }
    }

    /**
     * 用户操作界面的回调
     */
    @Suppress("DEPRECATION")
    private val mControllerCallback: UIPlayer.VideoViewCallback =
        object : UIPlayer.VideoViewCallback {
            override fun onSwitchPlayMode(playMode: PlayerMode) {
                if (mSuperPlayer.getVideoWidth() <= 0) return
                mFullScreenPlayer!!.hide()
                mWindowPlayer!!.hide()
                mFloatPlayer!!.hide()
                if (playMode == PlayerMode.FULLSCREEN) {
                    if (showInFullView == null) {
                        showToast("设置全屏失败，请先设置全屏显示的 window 或者viewGroup")
                        return
                    }
                    onPlayerCallback?.onStartFullScreenPlay()
                    mViewBinding?.uiPlayerContent?.removeView(mWindowPlayer)
                    mViewBinding?.uiPlayerContent?.addView(mFullScreenPlayer, mVodControllerParams)
                    rotateScreenOrientation(getVideoOrientation())
                    showFullPlayer()

                } else if (playMode == PlayerMode.WINDOW) { // 请求窗口模式
                    // 当前是悬浮窗
                    if (mSuperPlayer.playerMode == PlayerMode.FLOAT) {
                        try {
                            val viewContext = context
                            val intent: Intent? = if (viewContext is Activity) {
                                Intent(viewContext, viewContext.javaClass)
                            } else {
                                showToast(R.string.superplayer_float_play_fail)
                                return
                            }
                            mContext!!.startActivity(intent)
                            mSuperPlayer.pause()
                            mWindowManager!!.removeView(mFloatPlayer)
                            mSuperPlayer.setPlayerView(mTXCloudVideoView)
                            mSuperPlayer.resume()
                            onPlayerCallback?.onStopFloatWindow()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else if (mSuperPlayer.playerMode == PlayerMode.FULLSCREEN) { // 当前是全屏模式
                        onPlayerCallback?.onStopFullScreenPlay()
                        mViewBinding?.uiPlayerContent?.removeView(mFullScreenPlayer)
                        mViewBinding?.uiPlayerContent?.addView(mWindowPlayer, mVodControllerParams)
                        rotateScreenOrientation(Orientation.PORTRAIT)
                        exitFullPlayer()
                    }
                } else if (playMode == PlayerMode.FLOAT) { //请求悬浮窗模式
                    if (!playerConfig.enableFloatWindow) {
                        return
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 6.0动态申请悬浮窗权限
                        if (!Settings.canDrawOverlays(mContext)) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                            intent.data = Uri.parse("package:" + mContext!!.packageName)
                            mContext!!.startActivity(intent)
                            return
                        }
                    } else {
                        if (!checkOp(mContext, PlayerConfig.OP_SYSTEM_ALERT_WINDOW)) {
                            showToast(R.string.superplayer_enter_setting_fail)
                            return
                        }
                    }
                    mSuperPlayer.pause()
                    mWindowManager =
                        mContext!!.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    mWindowParams = WindowManager.LayoutParams()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mWindowParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        mWindowParams!!.type = WindowManager.LayoutParams.TYPE_PHONE
                    }
                    mWindowParams!!.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                    mWindowParams!!.format = PixelFormat.TRANSLUCENT
                    mWindowParams!!.gravity = Gravity.START or Gravity.TOP
                    val rect = playerConfig.floatViewRect
                    mWindowParams!!.x = rect.x
                    mWindowParams!!.y = rect.y
                    mWindowParams!!.width = rect.width
                    mWindowParams!!.height = rect.height
                    try {
                        mWindowManager!!.addView(mFloatPlayer, mWindowParams)
                    } catch (e: Exception) {
                        showToast(R.string.superplayer_float_play_fail)
                        return
                    }
                    val videoView = mFloatPlayer!!.floatVideoView
                    if (videoView != null) {
                        mSuperPlayer.setPlayerView(videoView)
                        mSuperPlayer.resume()
                    }
                    onPlayerCallback?.onStartFloatWindowPlay()
                }
                mSuperPlayer.switchPlayMode(playMode)
            }


            override fun onBackPressed(playMode: PlayerMode) {
                when (playMode) {
                    PlayerMode.FULLSCREEN -> {
                        onSwitchPlayMode(PlayerMode.WINDOW)
                    }
                    PlayerMode.WINDOW -> {
                        onPlayerCallback?.onClickSmallReturnBtn()
                    }
                    PlayerMode.FLOAT -> {
                        mWindowManager!!.removeView(mFloatPlayer)
                        onPlayerCallback?.onClickFloatCloseBtn()
                    }
                }
            }

            override fun onShare() {
                onPlayerCallback?.onClickShare()
            }

            override fun onShotScreen() {
                onPlayerCallback?.onShotScreen()
            }

            override fun onFloatPositionChange(x: Int, y: Int) {
                mWindowParams!!.x = x
                mWindowParams!!.y = y
                mWindowManager!!.updateViewLayout(mFloatPlayer, mWindowParams)
            }

            override fun onPause() {
                mSuperPlayer.pause()
            }

            override fun onResume() {
                if (mSuperPlayer.playerState == PlayerState.END) { //重播
                    mSuperPlayer.reStart()
                } else if (mSuperPlayer.playerState == PlayerState.PAUSE) { //继续播放
                    mSuperPlayer.resume()
                }
            }

            override fun onSeekTo(position: Int) {
                mSuperPlayer.seek(position)
            }

            override fun onResumeLive() {
                mSuperPlayer.resumeLive()
            }


            override fun onSnapshot() {
                mSuperPlayer.snapshot { bitmap ->
                    if (bitmap != null) {
                        onPlayerCallback?.onSnapshot(bitmap)
                    } else {
                        showToast(R.string.superplayer_screenshot_fail)
                    }
                }
            }

            override fun onQualityChange(quality: VideoQuality) {
                mFullScreenPlayer!!.updateVideoQuality(quality)
                mWindowPlayer!!.updateVideoQuality(quality)
                mSuperPlayer.switchStream(quality)
                showMsg("已切换为${quality.title}播放", "${quality.title}")
            }

            override fun onSpeedChange(speedLevel: Float) {
                updateSpeedChange(speedLevel)
                showMsg("当前列表已切换为${speedLevel}倍速度播放", "$speedLevel")
            }

            override fun onMirrorToggle(isMirror: Boolean) {
                mSuperPlayer.setMirror(isMirror)
            }

            override fun onHWAccelerationToggle(isAccelerate: Boolean) {
                mSuperPlayer.enableHardwareDecode(isAccelerate)
            }

            override fun onControlViewToggle(showing: Boolean) {

            }
        }

    private fun getVideoOrientation(): Orientation {
        val videoRotation = getVideoRotation()
        return if (mSuperPlayer.getVideoHeight() >= mSuperPlayer.getVideoWidth()) {
            if (videoRotation == 90) {
                Orientation.LANDSCAPE
            } else {
                Orientation.PORTRAIT
            }
        } else {
            if (videoRotation == 90) {
                Orientation.PORTRAIT
            } else {
                Orientation.LANDSCAPE
            }
        }
    }

    private fun showFullPlayer() {
        if (showInFullView != null) {
            parentViewGroup = this.parent as ViewGroup
            parentViewGroup?.removeView(this)
            showInFullView!!.addView(
                this,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            )
            return
        }
        Log.e("SuperPlayerView","showInFullView is null ,please set setFullInWindow")
    }

    private fun exitFullPlayer() {
        if (showInFullView != null) {
            showInFullView!!.removeView(this)
            parentViewGroup?.addView(this, mLayoutParamWindowMode)
        }
    }

    private fun rotateScreenOrientation(orientation: Orientation) {
        when (orientation) {
            Orientation.LANDSCAPE -> (mContext as? Activity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            Orientation.PORTRAIT -> (mContext as? Activity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun setPlayConfig(config: PlayerConfig) {
        this.playerConfig = config
        uiPlayerList.forEach {
            it.setPlayConfig(config)
        }
    }

    override fun setUIConfig(uiConfig: UIConfig) {
        this.uiConfig = uiConfig
        uiPlayerList.forEach {
            it.setUIConfig(uiConfig)
        }
    }
}