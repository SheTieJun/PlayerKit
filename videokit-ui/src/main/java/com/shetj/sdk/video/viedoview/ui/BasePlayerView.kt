package com.shetj.sdk.video.viedoview.ui

import android.app.Activity
import android.app.ActivityManager
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
import com.shetj.sdk.video.casehelper.KeyListListener
import com.shetj.sdk.video.casehelper.PlayKeyListConfig
import com.shetj.sdk.video.casehelper.onNext
import com.shetj.sdk.video.kit.PlayerKit.checkOp
import com.shetj.sdk.video.ui.R
import com.shetj.sdk.video.ui.databinding.SuperplayerVodViewBinding
import com.shetj.sdk.video.viedoview.AbBaseUIPlayer
import me.shetj.sdk.video.base.*
import me.shetj.sdk.video.model.PlayImageSpriteInfo
import me.shetj.sdk.video.model.PlayKeyFrameDescInfo
import me.shetj.sdk.video.model.VideoPlayerModel
import me.shetj.sdk.video.model.VideoQuality
import me.shetj.sdk.video.player.IPlayer
import me.shetj.sdk.video.player.IPlayerObserver
import me.shetj.sdk.video.player.ISnapshotListener
import me.shetj.sdk.video.player.OnPlayerCallback
import me.shetj.sdk.video.player.PlayerDef.*
import me.shetj.sdk.video.timer.TimerConfigure
import me.shetj.sdk.video.ui.ABUIPlayer
import me.shetj.sdk.video.ui.IUIPlayer

/**
 *
 * 具备播放器基本功能，
 * 此外还包括横竖屏切换、
 * 悬浮窗播放、画质切换、
 * 硬件加速、倍速播放、镜像播放、手势控制等功能，同时支持直播与点播
 */
open class BasePlayerView : FrameLayout, TimerConfigure.CallBack,
    IPlayer, IUIPlayer,
    ConfigInterface, IPlayerObserver, KeyListListener {

    protected var parentViewGroup: ViewGroup? = null //播放器原容器
    protected var fullContainer: ViewGroup? = null //点击全屏按钮时，播放器相关内容会放到这个容器
    protected var playerConfig: PlayerConfig = PlayerConfig.playerConfig
    protected var uiConfig: UIConfig = UIConfig.uiConfig
    protected val playKeyListConfig: PlayKeyListConfig by lazy { PlayKeyListConfig.ofDef() }
    protected var mContext: Context? = null
    protected var mSuperPlayer: IPlayer? = null

    protected var mViewBinding: SuperplayerVodViewBinding? = null

    // 全屏模式控制view
    protected var mFullScreenPlayer: ABUIPlayer? = null

    // 窗口模式控制view
    protected var mWindowPlayer: ABUIPlayer? = null

    // 悬浮窗模式控制view
    protected var mFloatPlayer: BaseFloatPlayer? = null

    // 腾讯云视频播放view
    private var mVideoView: IPlayerView? = null

    private var mLayoutParamWindowMode: ViewGroup.LayoutParams? = null// 窗口播放时SuperPlayerView的布局参数

    // 全屏controller的布局参数
    protected var mVodControllerParams: LayoutParams? = null

    // 悬浮窗窗口管理器
    protected var mWindowManager: WindowManager? = null

    // 悬浮窗布局参数
    protected var mWindowParams: WindowManager.LayoutParams? = null

    protected val uiPlayerList = ArrayList<ABUIPlayer>()
    protected var onPlayerCallback: OnPlayerCallback? = null
    protected val activityManager: ActivityManager by lazy { getActivityManagerService() }


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

    /**
     * 初始化view
     */
    private fun initView() {
        mVodControllerParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        mViewBinding = SuperplayerVodViewBinding.inflate(LayoutInflater.from(mContext), this, true)
        mViewBinding?.apply {
            mFullScreenPlayer = FullScreenPlayer(context)
            mWindowPlayer = WindowPlayer(context)
            //添加到列表方便操作
            uiPlayerList.add(mFullScreenPlayer!!)
            uiPlayerList.add(mWindowPlayer!!)
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
            if (mSuperPlayer?.playerMode == PlayerMode.WINDOW) {
                mLayoutParamWindowMode = layoutParams
            }
        }
        setUICallback(mControllerCallback)
        updatePlayConfig(playerConfig)
        updateUIConfig(uiConfig)
    }

    //初始话播放器
    private fun initPlayer() {
        if (playerMode == PlayerMode.FULLSCREEN) {
            mViewBinding?.uiPlayerContent?.addView(mFullScreenPlayer)
            mFullScreenPlayer!!.hide()
        } else if (playerMode == PlayerMode.WINDOW) {
            mViewBinding?.uiPlayerContent?.addView(mWindowPlayer)
            mWindowPlayer!!.hide()
        }
    }

    /**
     * 更新player
     */
    fun updatePlayer(player: IPlayer) {
        if (mSuperPlayer != null) {
            mSuperPlayer!!.destroy()
        }
        mSuperPlayer = player
        setObserver(this)
    }

    /**
     * 更新悬浮样式
     */
    fun updateFloatView(floatPlayer: BaseFloatPlayer) {
        if (playerMode == PlayerMode.FLOAT) {
            switchPlayMode(PlayerMode.WINDOW)
        }
        if (mFloatPlayer != null) {
            uiPlayerList.remove(mFloatPlayer!!)
        }
        mFloatPlayer = floatPlayer
        mFloatPlayer!!.setUICallback(mControllerCallback)
        uiPlayerList.add(mFloatPlayer!!)
    }

    /**
     * 全屏
     */
    fun updateFullScreenView(fullPlayer: ABUIPlayer) {
        if (playerMode == PlayerMode.FULLSCREEN) {
            mViewBinding?.uiPlayerContent?.removeView(mFullScreenPlayer)
        }
        uiPlayerList.remove(mFullScreenPlayer!!)
        fullPlayer.updatePlayConfig(playerConfig)
        fullPlayer.updateUIConfig(uiConfig)
        this.mFullScreenPlayer = fullPlayer
        uiPlayerList.add(fullPlayer)
        if (playerMode == PlayerMode.FULLSCREEN) {
            mViewBinding?.uiPlayerContent?.addView(mFullScreenPlayer)
            mFullScreenPlayer!!.hide()
        }
    }

    /**
     * 小屏
     */
    fun updateWindowView(winPlayer: ABUIPlayer) {
        if (playerMode == PlayerMode.WINDOW) {
            mViewBinding?.uiPlayerContent?.removeView(mWindowPlayer)
        }
        winPlayer.updatePlayConfig(playerConfig)
        winPlayer.updateUIConfig(uiConfig)
        this.uiPlayerList.remove(mWindowPlayer!!)
        this.mWindowPlayer = winPlayer
        this.uiPlayerList.add(winPlayer)
        if (playerMode == PlayerMode.WINDOW) {
            mViewBinding?.uiPlayerContent?.addView(mWindowPlayer)
            mWindowPlayer!!.hide()
        }
    }

    /**
     * 设置播放回调
     */
    fun setPlayerCallback(onPlayerCallback: OnPlayerCallback) {
        this.onPlayerCallback = onPlayerCallback
    }

    /**
     * 设置全屏展示的window
     */
    fun setFullInWindow(window: Window) {
        fullContainer = window.decorView.findViewById(android.R.id.content)
    }

    /**
     * 设置全屏展示viewGroup
     */
    fun setFullInWindow(view: ViewGroup) {
        fullContainer = view
    }

    /**
     * 定时倒计事件
     */
    override fun onTick(progress: Long) {

    }

    /**
     * 状态切换
     * 0 ： 开启定时  ； 1 ：关闭/取消定时 ；2 ：完成定时complete； 3：切换到课程计时
     */
    override fun onStateChange(state: Int) {

    }

    /**
     * 播放模式切换
     */
    override fun onChangeModel(repeatMode: Int) {

    }

    open fun showToast(resId: Int) {
        Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show()
    }

    open fun showToast(message: String?) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }

    open fun <T : VideoPlayerModel> playWithModel(model: T) {
        if (model.multiURLs != null && model.multiURLs!!.isNotEmpty()) {
            mSuperPlayer?.play(model.multiURLs, model.playDefaultIndex)
        } else {
            mSuperPlayer?.play(model.url)
        }
    }

    override fun play(url: String?) {
        mSuperPlayer?.play(url)
    }

    override fun play(
        superPlayerURLS: List<VideoPlayerModel.PlayerURL?>?,
        defaultIndex: Int
    ) {
        mSuperPlayer?.play(superPlayerURLS, defaultIndex)
    }

    override fun reStart() {
        mSuperPlayer?.reStart()
    }

    override fun pause() {
        mSuperPlayer?.pause()
    }

    override fun resume() {
        mSuperPlayer?.resume()
    }

    override fun resumeLive() {
        mSuperPlayer?.resumeLive()
    }

    /**
     * 必须在播放链接设置之前[play]设置
     */
    override fun autoPlay(auto: Boolean) {
        mSuperPlayer?.autoPlay(auto)
    }

    override fun setLoopPlay(isLoop: Boolean) {
        mSuperPlayer?.setLoopPlay(isLoop)
    }

    override fun isLoop(): Boolean {
        return mSuperPlayer?.isLoop() == true
    }

    override fun getVideoWidth(): Int {
        return mSuperPlayer?.getVideoWidth() ?: 0
    }

    override fun getVideoHeight(): Int {
        return mSuperPlayer?.getVideoHeight() ?: 0
    }

    override fun getVideoRotation(): Int {
        return mSuperPlayer?.getVideoRotation() ?: 0
    }

    override fun stop() {
        mSuperPlayer?.stop()
    }

    override fun destroy() {
        mSuperPlayer?.destroy()
        release()
        onDestroy()
    }

    /**
     * 设置播放模式
     *  [PlayerMode.WINDOW]：窗口模式 ；[PlayerMode.FULLSCREEN]: 全屏模式 ；[PlayerMode.FLOAT]: 悬浮窗模式
     */
    override fun switchPlayMode(playerMode: PlayerMode) {
        if (playerMode != this.playerMode) {
            mControllerCallback.onSwitchPlayMode(playerMode)
        }
    }

    /**
     * 是否开启硬解
     */
    override fun enableHardwareDecode(enable: Boolean) {
        mSuperPlayer?.enableHardwareDecode(enable)
    }


    /**
     * 设置播放的view,必须设置，因为不同player实现是需要不用view 播放
     */
    override fun setPlayerView(videoView: IPlayerView) {
        this.mVideoView = videoView
        mViewBinding?.superplayerRoot?.removeAllViews()
        mViewBinding?.superplayerRoot
            ?.addView(videoView.getPlayerView().apply {
                layoutParams = ViewGroup.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            })
        mSuperPlayer?.setPlayerView(videoView)
    }

    override fun seek(position: Int, isCallback: Boolean) {
        mSuperPlayer?.seek(position, isCallback)
    }

    override fun setPlayToSeek(position: Int) {
        mSuperPlayer?.setPlayToSeek(position)
    }

    override fun snapshot(listener: ISnapshotListener) {
        mSuperPlayer?.snapshot(listener)
    }

    override fun setPlaySpeed(speedLevel: Float) {
        mSuperPlayer?.setPlaySpeed(speedLevel)
    }

    override fun setMirror(isMirror: Boolean) {
        mSuperPlayer?.setMirror(isMirror)
    }

    override fun switchStream(quality: VideoQuality) {
        mSuperPlayer?.switchStream(quality)
    }

    override fun changeRenderMode(renderMode: Int) {
        mSuperPlayer?.changeRenderMode(renderMode)
    }

    override var playURL: String? = null
        get() = mSuperPlayer?.playURL
    override val playerType: PlayerType
        get() = mSuperPlayer?.playerType ?: PlayerType.VOD
    override val playerMode: PlayerMode
        get() = mSuperPlayer?.playerMode ?: PlayerMode.WINDOW
    override val playerState: PlayerState
        get() = mSuperPlayer?.playerState ?: PlayerState.END


    override fun setObserver(observer: IPlayerObserver?) {
        mSuperPlayer?.setObserver(observer)
    }

    override fun getDuration(): Long {
        return mSuperPlayer?.getDuration() ?: 0
    }

    override fun getPosition(): Long {
        return mSuperPlayer?.getPosition() ?: 0
    }

    override fun updateImageSpriteAndKeyFrame(
        info: PlayImageSpriteInfo?,
        list: ArrayList<PlayKeyFrameDescInfo>?
    ) {
        mSuperPlayer?.updateImageSpriteAndKeyFrame(info, list)
    }

    override fun setUICallback(callback: IUIPlayer.VideoViewCallback?) {
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
        mSuperPlayer?.setPlaySpeed(speedLevel)
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
        onPlayerCallback?.onComplete()
        if (!isLoop()) {
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
            if (it is AbBaseUIPlayer) {
                it.updateListPosition(position)
            }
        }
    }

    override fun setKeyList(
        name: String?,
        adapter: BaseKitAdapter<*>?,
        position: Int,
        onNext: onNext?
    ) {
        uiPlayerList.forEach {
            if (it is AbBaseUIPlayer) {
                it.setKeyList(name, adapter, position, onNext)
            }
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
        onPlayerCallback?.onError(code, message)
    }

    override fun onPlayerTypeChange(playType: PlayerType?) {
        uiPlayerList.forEach {
            it.updatePlayType(playType)
        }
    }

    override fun onPlayTimeShiftLive(player: IPlayer?, url: String?) {
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
        return if (mSuperPlayer?.playerMode == PlayerMode.WINDOW
            || mSuperPlayer?.playerMode == PlayerMode.FLOAT
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
        TimerConfigure.instance.removeCallBack(this)
    }

    /**
     * 用户操作界面的回调
     */
    @Suppress("DEPRECATION")
    private val mControllerCallback: IUIPlayer.VideoViewCallback =
        object : IUIPlayer.VideoViewCallback {
            override fun onSwitchPlayMode(playMode: PlayerMode) {
                if (getVideoWidth() <= 0) return
                mFullScreenPlayer?.hide()
                mWindowPlayer?.hide()
                mFloatPlayer?.hide()
                if (playMode == PlayerMode.FULLSCREEN) {
                    if (fullContainer == null) {
                        showToast("设置全屏失败，请先设置全屏显示的 window 或者 viewGroup:setFullInWindow")
                        return
                    }
                    onPlayerCallback?.onStartFullScreenPlay()
                    mViewBinding?.uiPlayerContent?.removeView(mWindowPlayer)
                    mViewBinding?.uiPlayerContent?.addView(mFullScreenPlayer, mVodControllerParams)
                    rotateScreenOrientation(getVideoOrientation())
                    showFullPlayer()

                } else if (playMode == PlayerMode.WINDOW) { // 请求窗口模式
                    // 当前是悬浮窗
                    if (mSuperPlayer?.playerMode == PlayerMode.FLOAT) {
                        try {
                            processStopFloatWindow()
                            onPlayerCallback?.onStopFloatWindow()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else if (mSuperPlayer?.playerMode == PlayerMode.FULLSCREEN) { // 当前是全屏模式
                        onPlayerCallback?.onStopFullScreenPlay()
                        mViewBinding?.uiPlayerContent?.removeView(mFullScreenPlayer)
                        mViewBinding?.uiPlayerContent?.addView(mWindowPlayer, mVodControllerParams)
                        rotateScreenOrientation(Orientation.PORTRAIT)
                        exitFullPlayer()
                    }
                } else if (playMode == PlayerMode.FLOAT) { //请求悬浮窗模式
                    if (!GlobalConfig.enableFloatWindow) {
                        return
                    }
                    if (mFloatPlayer == null) {
                        Log.e("playerKit", "无设置悬浮view,无法启动悬浮模式")
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
                    mSuperPlayer?.pause()
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
                    if (mFloatPlayer != null) {
                        mSuperPlayer?.setPlayerView(mFloatPlayer!!)
                        mSuperPlayer?.resume()
                    }
                    onPlayerCallback?.onStartFloatWindowPlay()
                }
                mSuperPlayer?.switchPlayMode(playMode)
            }


            override fun onBackPressed(playMode: PlayerMode) {
                processOnBackPressed(playMode)
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
                mSuperPlayer?.pause()
            }

            override fun onResume() {
                if (mSuperPlayer?.playerState == PlayerState.END) { //重播
                    mSuperPlayer?.reStart()
                } else if (mSuperPlayer?.playerState == PlayerState.PAUSE) { //继续播放
                    mSuperPlayer?.resume()
                }
            }

            override fun onSeekTo(position: Int) {
                mSuperPlayer?.seek(position)
            }

            override fun onResumeLive() {
                mSuperPlayer?.resumeLive()
            }


            override fun onSnapshot() {
                mSuperPlayer?.snapshot(object : ISnapshotListener {
                    override fun onSnapshot(bitmap: Bitmap?) {
                        if (bitmap != null) {
                            onPlayerCallback?.onSnapshot(bitmap)
                        } else {
                            showToast(R.string.superplayer_screenshot_fail)
                        }
                    }
                })
            }

            override fun onQualityChange(quality: VideoQuality) {
                mFullScreenPlayer!!.updateVideoQuality(quality)
                mWindowPlayer!!.updateVideoQuality(quality)
                mSuperPlayer?.switchStream(quality)
                showToast("已切换为${quality.title}播放")
            }

            override fun onSpeedChange(speedLevel: Float) {
                updateSpeedChange(speedLevel)
                showToast("当前列表已切换为${speedLevel}倍速度播放")
            }

            override fun onMirrorToggle(isMirror: Boolean) {
                mSuperPlayer?.setMirror(isMirror)
            }

            override fun onHWAccelerationToggle(isAccelerate: Boolean) {
                mSuperPlayer?.enableHardwareDecode(isAccelerate)
            }

            override fun onControlViewToggle(showing: Boolean) {

            }
        }

    /**
     * 处理退出悬浮模式，可以继承后修改处理方式
     */
    open fun processStopFloatWindow() {
        val viewContext = context
        if (viewContext is Activity && !viewContext.isDestroyed) {
            //让该类型的Activity 会到前台
            val intent: Intent =
                Intent(viewContext, viewContext.javaClass).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                }
            viewContext.startActivity(intent)
            mSuperPlayer?.pause()
            mVideoView?.let { mSuperPlayer?.setPlayerView(it) }
            mSuperPlayer?.resume()
            mWindowManager?.removeView(mFloatPlayer)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activityManager.appTasks?.first {
                    it.taskInfo.baseIntent.component?.packageName == viewContext.packageName
                }?.apply {
                    moveToFront()
                }
            }
            mSuperPlayer?.pause()
            mWindowManager?.removeView(mFloatPlayer)
        }
    }

    /**
     * 处理不同模式的关闭按钮事件
     * 可以继承后修改处理方式
     */
    open fun processOnBackPressed(playMode: PlayerMode) {
        when (playMode) {
            PlayerMode.FULLSCREEN -> {
                mControllerCallback.onSwitchPlayMode(PlayerMode.WINDOW)
            }
            PlayerMode.WINDOW -> {
                onPlayerCallback?.onClickSmallReturnBtn()
            }
            PlayerMode.FLOAT -> {
                mVideoView?.let { mSuperPlayer?.setPlayerView(it) }
                mSuperPlayer?.pause()
                mWindowManager!!.removeView(mFloatPlayer)
                onPlayerCallback?.onClickFloatCloseBtn()
                mSuperPlayer?.switchPlayMode(PlayerMode.WINDOW)
            }
        }
    }

    private fun getActivityManagerService(): ActivityManager {
        return context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    /**
     * 获取视频的横竖屏状态
     */
    private fun getVideoOrientation(): Orientation {
        val videoRotation = getVideoRotation()
        if (mSuperPlayer == null) {
            return if (videoRotation == 90) {
                Orientation.LANDSCAPE
            } else {
                Orientation.PORTRAIT
            }
        } else {
            return if (mSuperPlayer!!.getVideoHeight() >= mSuperPlayer!!.getVideoWidth()) {
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
    }

    /**
     * 展示全屏
     */
    private fun showFullPlayer() {
        if (fullContainer != null) {
            parentViewGroup = this.parent as ViewGroup
            parentViewGroup?.removeView(this)
            fullContainer!!.addView(
                this,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            )
            return
        }
        Log.e("SuperPlayerView", "showInFullView is null ,please set setFullInWindow")
    }

    /**
     * 退出全屏
     */
    private fun exitFullPlayer() {
        if (fullContainer != null) {
            fullContainer!!.removeView(this)
            parentViewGroup?.addView(this, mLayoutParamWindowMode)
        }
    }

    /**
     * 设置全屏
     */
    private fun rotateScreenOrientation(orientation: Orientation) {
        when (orientation) {
            Orientation.LANDSCAPE -> (mContext as? Activity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            Orientation.PORTRAIT -> (mContext as? Activity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    /**
     * 更新播放的配置
     */
    override fun updatePlayConfig(config: PlayerConfig) {
        this.playerConfig = config
        uiPlayerList.forEach {
            it.updatePlayConfig(config)
        }
    }

    /**
     * 更新UI的配置
     */
    override fun updateUIConfig(uiConfig: UIConfig) {
        this.uiConfig = uiConfig
        uiPlayerList.forEach {
            it.updateUIConfig(uiConfig)
        }
    }

}