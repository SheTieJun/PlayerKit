package com.tencent.video.superplayer

import android.app.Activity
import android.app.AppOpsManager
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.net.Uri
import android.os.AsyncTask
import android.os.Binder
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.transition.TransitionManager
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.updateLayoutParams
import com.tencent.liteav.basic.log.TXCLog
import com.tencent.liteav.superplayer.R
import com.tencent.rtmp.TXLivePlayer
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.video.superplayer.SuperPlayerDef.*
import com.tencent.video.superplayer.base.BaseKitAdapter
import com.tencent.video.superplayer.base.PlayerConfig
import com.tencent.video.superplayer.base.PlayerConfig.Companion.OP_SYSTEM_ALERT_WINDOW
import com.tencent.video.superplayer.base.PlayerConfig.Companion.ofDef
import com.tencent.video.superplayer.casehelper.PlayKeyListHelper
import com.tencent.video.superplayer.casehelper.onNext
import com.tencent.video.superplayer.model.SuperPlayer
import com.tencent.video.superplayer.model.SuperPlayerImpl
import com.tencent.video.superplayer.model.SuperPlayerObserver
import com.tencent.video.superplayer.model.entity.PlayImageSpriteInfo
import com.tencent.video.superplayer.model.entity.PlayKeyFrameDescInfo
import com.tencent.video.superplayer.model.entity.VideoQuality
import com.tencent.video.superplayer.model.utils.NetWatcher
import com.tencent.video.superplayer.base.timer.TimeType
import com.tencent.video.superplayer.base.timer.TimerConfigure
import com.tencent.video.superplayer.tv.TVControl
import com.tencent.video.superplayer.ui.player.FloatPlayer
import com.tencent.video.superplayer.ui.player.FullScreenPlayer
import com.tencent.video.superplayer.ui.player.Player
import com.tencent.video.superplayer.ui.player.WindowPlayer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 *
 * 超级播放器view
 *
 * 具备播放器基本功能，此外还包括横竖屏切换、悬浮窗播放、画质切换、硬件加速、倍速播放、镜像播放、手势控制等功能，同时支持直播与点播
 * 使用方式极为简单，只需要在布局文件中引入并获取到该控件，通过[.playWithModel]传入[SuperPlayerModel]即可实现视频播放
 *
 * 1、播放视频[.playWithModel]
 * 2、设置回调[.setPlayerViewCallback]
 * 3、controller回调实现[.mControllerCallback]
 * 4、退出播放释放内存[.resetPlayer]
 */
open class SuperPlayerView : FrameLayout, TimerConfigure.CallBack {

    protected var configure :PlayerConfig = ofDef()

    protected var mContext: Context? = null

    // SuperPlayerView的根view
    protected var mRootView: ViewGroup? = null

    // 腾讯云视频播放view
    protected var mTXCloudVideoView: TXCloudVideoView? = null

    // 全屏模式控制view
    protected var mFullScreenPlayer: FullScreenPlayer? = null

    // 窗口模式控制view
    protected var mWindowPlayer: WindowPlayer? = null

    // 悬浮窗模式控制view
    protected var mFloatPlayer: FloatPlayer? = null

    // 窗口播放时SuperPlayerView的布局参数
    protected var mLayoutParamWindowMode: ViewGroup.LayoutParams? = null

    // 全屏播放时SuperPlayerView的布局参数
    protected var mLayoutParamFullScreenMode: ViewGroup.LayoutParams? = null

    // 窗口controller的布局参数
    protected var mVodControllerWindowParams: LayoutParams? = null

    // 全屏controller的布局参数
    protected var mVodControllerFullScreenParams: LayoutParams? = null

    // 悬浮窗窗口管理器
    protected var mWindowManager: WindowManager? = null

    // 悬浮窗布局参数
    protected var mWindowParams: WindowManager.LayoutParams? = null

    // SuperPlayerView回调
    protected var mPlayerViewCallback: OnSuperPlayerViewCallback? = null

    // 网络质量监视器
    protected var mWatcher: NetWatcher? = null

    protected var mSuperPlayer: SuperPlayer? = null

    protected var coveImage: ImageView? = null
    protected var cover_root: ViewGroup? = null
    protected var ivStartPlayer: ImageView? = null
    var mTvController: TVControl? = null
    var isRepeat = false
    var mAutoPlay: Boolean = false
    var currentPosition: Long = 0L
    var duration: Long = 0L

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
        initView()
        initPlayer()
    }

    /**
     * 初始化view
     */
    private fun initView() {
        mRootView =
            LayoutInflater.from(mContext).inflate(R.layout.superplayer_vod_view, null) as ViewGroup
        mTXCloudVideoView =
            mRootView!!.findViewById<View>(R.id.superplayer_cloud_video_view) as TXCloudVideoView
        mFullScreenPlayer =
            mRootView!!.findViewById<View>(R.id.superplayer_controller_large) as FullScreenPlayer
        mWindowPlayer =
            mRootView!!.findViewById<View>(R.id.superplayer_controller_small) as WindowPlayer
        mFloatPlayer =
            mRootView!!.findViewById<View>(R.id.superplayer_controller_float) as FloatPlayer
        cover_root = mRootView!!.findViewById(R.id.cover_root)
        mVodControllerWindowParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        mVodControllerFullScreenParams =
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        coveImage = mRootView!!.findViewById(R.id.cove_url)
        ivStartPlayer = mRootView!!.findViewById(R.id.iv_start_player)
        mFullScreenPlayer!!.setCallback(mControllerCallback)
        mWindowPlayer!!.setCallback(mControllerCallback)
        mFloatPlayer!!.setCallback(mControllerCallback)
        removeAllViews()
        mRootView!!.removeView(coveImage)
        mRootView!!.removeView(mTXCloudVideoView)
        mRootView!!.removeView(mWindowPlayer)
        mRootView!!.removeView(mFullScreenPlayer)
        mRootView!!.removeView(mFloatPlayer)
        mRootView!!.removeView(cover_root)
        mRootView!!.removeView(ivStartPlayer)
        addView(mTXCloudVideoView)
        addView(coveImage)
        addView(cover_root)
        addView(ivStartPlayer)
        ivStartPlayer?.updateLayoutParams<LayoutParams> {
            gravity = Gravity.CENTER
        }
        if (configure.isHideAll) {
            mFullScreenPlayer!!.hide()
            mWindowPlayer!!.hide()
            mFloatPlayer!!.hide()
        }
    }

    fun updatePlayerConfig(config: PlayerConfig){
        this.configure = config
        getPlayer().setPlayConfig(config)
    }

    private fun getPlayer(): SuperPlayer {
        if (mSuperPlayer == null) {
            initPlayer()
        }
        return mSuperPlayer!!
    }

    private fun initPlayer() {
        mSuperPlayer = SuperPlayerImpl(mContext, mTXCloudVideoView)
        mSuperPlayer!!.setObserver(mSuperPlayerObserver)
        if (mSuperPlayer!!.playerMode == PlayerMode.FULLSCREEN) {
            addView(mFullScreenPlayer)
            mFullScreenPlayer!!.hide()
        } else if (mSuperPlayer!!.playerMode == PlayerMode.WINDOW) {
            addView(mWindowPlayer)
            mWindowPlayer!!.hide()
        }
        post {
            if (mSuperPlayer!!.playerMode == PlayerMode.WINDOW) {
                mLayoutParamWindowMode = layoutParams
            }
            try {
                // 依据上层Parent的LayoutParam类型来实例化一个新的fullscreen模式下的LayoutParam
                val parentLayoutParamClazz: Class<*> = layoutParams.javaClass
                val constructor = parentLayoutParamClazz.getDeclaredConstructor(
                    Int::class.javaPrimitiveType, Int::class.javaPrimitiveType
                )
                mLayoutParamFullScreenMode = constructor.newInstance(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ) as ViewGroup.LayoutParams
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (mWatcher == null) {
            mWatcher = NetWatcher(mContext)
        }
        TimerConfigure.instance.addCallBack(this)
    }

    /**
     * 播放视频
     *
     * @param model
     */
    open fun playWithModel(model: SuperPlayerModel) {
        if (model.videoId != null) {
            mSuperPlayer!!.play(model.appId, model.videoId!!.fileId, model.videoId!!.pSign)
        } else if (model.videoIdV2 != null) {
            mSuperPlayer!!.play(model.appId, model.videoIdV2!!.fileId, model.videoIdV2!!.sign)
        } else if (model.multiURLs != null && model.multiURLs!!.isNotEmpty()) {
            mSuperPlayer!!.play(model.appId, model.multiURLs, model.playDefaultIndex)
        } else {
            mSuperPlayer!!.play(model.url)
        }
    }

    /**
     * 开始播放
     *
     * @param url 视频地址
     */
    open fun play(url: String?) {
        mSuperPlayer!!.play(url)
    }

    /**
     * 开始播放
     *
     * @param appId 腾讯云视频appId
     * @param url   直播播放地址
     */
    open fun play(appId: Int, url: String?) {
        mSuperPlayer!!.play(appId, url)
    }

    /**
     * 开始播放
     *
     * @param appId  腾讯云视频appId
     * @param fileId 腾讯云视频fileId
     * @param psign  防盗链签名，开启防盗链的视频必填，非防盗链视频可不填
     */
    open fun play(appId: Int, fileId: String?, psign: String?) {
        mSuperPlayer!!.play(appId, fileId, psign)
    }

    /**
     * 多分辨率播放
     *
     * @param appId           腾讯云视频appId
     * @param superPlayerURLS 不同分辨率数据
     * @param defaultIndex    默认播放Index
     */
    open fun play(
        appId: Int,
        superPlayerURLS: List<SuperPlayerModel.SuperPlayerURL?>?,
        defaultIndex: Int
    ) {
        mSuperPlayer!!.play(appId, superPlayerURLS, defaultIndex)
    }

    open fun setPlayToSeek(position: Int) {
        mSuperPlayer!!.setPlayToSeek(position)
    }

    open fun seek(position: Int) {
        mSuperPlayer!!.seek(position)
    }

    open fun setKeyList(
        name: String?,
        adapter: BaseKitAdapter<*>?,
        position: Int = 0,
        onNext: onNext? = null
    ) {
        mFullScreenPlayer?.getKeyListHelper()?.setKeyAndAdapter(name, adapter, position, onNext)
    }


    fun showLoading() {
        getWinPlayer()?.showLoading()
        getFullPlayer()?.showLoading()
    }


    fun hideLoading() {
        getWinPlayer()?.hideLoading()
        getFullPlayer()?.hideLoading()
    }


    fun getKeyListHelper(): PlayKeyListHelper? {
        return mFullScreenPlayer?.getKeyListHelper()
    }

    /**
     * 更新标题
     *
     * @param title 视频名称
     */
    open fun updateTitle(title: String?) {
        mWindowPlayer!!.updateTitle(title)
        mFullScreenPlayer!!.updateTitle(title)
    }

    /**
     * resume生命周期回调
     */
    open fun onResume() {
        mSuperPlayer!!.resume()
    }

    /**
     * pause生命周期回调
     */
    open fun onPause() {
        mSuperPlayer!!.pauseVod()
    }

    open fun onRePlay() {
        mSuperPlayer!!.reStart()
    }

    /**
     * 重置播放器
     */
    open fun resetPlayer() {
        stopPlay()
    }

    /**
     * 停止播放
     */
    private fun stopPlay() {
        mSuperPlayer!!.stop()
        if (mWatcher != null) {
            mWatcher!!.stop()
        }
    }

    open fun getFullPlayer(): FullScreenPlayer? {
        return mFullScreenPlayer
    }

    open fun getWinPlayer(): WindowPlayer? {
        return mWindowPlayer
    }

    /**
     * 设置超级播放器的回掉
     *
     * @param callback
     */
    open fun setPlayerViewCallback(callback: OnSuperPlayerViewCallback?) {
        mPlayerViewCallback = callback
    }

    /**
     * 设置投屏
     */
    open fun setTVControl(controller: TVControl?) {
        mFullScreenPlayer?.hideTV(controller != null)
        mWindowPlayer?.hideTV(controller != null)
        this.mTvController = controller
    }

    /**
     * 初始化controller回调
     */
    private val mControllerCallback: Player.Callback? = object : Player.Callback {
        override fun onSwitchPlayMode(playerMode: PlayerMode) {
            if (mSuperPlayer!!.getWidth() <= 0) return
            mFullScreenPlayer!!.hide()
            mWindowPlayer!!.hide()
            mFloatPlayer!!.hide()
            if (playerMode == PlayerMode.FULLSCREEN) {
                if (mLayoutParamFullScreenMode == null) {
                    return
                }
                if (mPlayerViewCallback != null) {
                    mPlayerViewCallback!!.onStartFullScreenPlay()
                }
                removeView(mWindowPlayer)
                addView(mFullScreenPlayer, mVodControllerFullScreenParams)
                if (mSuperPlayer!!.getWidth() <= mSuperPlayer!!.getHeight()) {
                    TransitionManager.beginDelayedTransition(parent as ViewGroup)
                }
                layoutParams = mLayoutParamFullScreenMode
                if (mSuperPlayer!!.getWidth() > mSuperPlayer!!.getHeight()) {
                    rotateScreenOrientation(Orientation.LANDSCAPE)
                }
            } else if (playerMode == PlayerMode.WINDOW) { // 请求窗口模式
                // 当前是悬浮窗
                if (mSuperPlayer!!.playerMode == PlayerMode.FLOAT) {
                    try {
                        val viewContext = context
                        var intent: Intent? = null
                        intent = if (viewContext is Activity) {
                            Intent(viewContext, viewContext.javaClass)
                        } else {
                            showToast(R.string.superplayer_float_play_fail)
                            return
                        }
                        mContext!!.startActivity(intent)
                        mSuperPlayer!!.pause()
                        if (mLayoutParamWindowMode == null) {
                            return
                        }
                        mWindowManager!!.removeView(mFloatPlayer)
                        mSuperPlayer!!.setPlayerView(mTXCloudVideoView)
                        mSuperPlayer!!.resume()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else if (mSuperPlayer!!.playerMode == PlayerMode.FULLSCREEN) { // 当前是全屏模式
                    if (mLayoutParamWindowMode == null) {
                        return
                    }
                    if (mPlayerViewCallback != null) {
                        mPlayerViewCallback!!.onStopFullScreenPlay()
                    }
                    removeView(mFullScreenPlayer)
                    addView(mWindowPlayer, mVodControllerWindowParams)
                    if ((mContext as Activity?)!!.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        TransitionManager.beginDelayedTransition(parent as ViewGroup)
                    }
                    layoutParams = mLayoutParamWindowMode
                    rotateScreenOrientation(Orientation.PORTRAIT)
                }
            } else if (playerMode == PlayerMode.FLOAT) { //请求悬浮窗模式
                TXCLog.i(TAG, "requestPlayMode Float :" + Build.MANUFACTURER)
                if (!configure.enableFloatWindow) {
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
                    if (!checkOp(mContext, OP_SYSTEM_ALERT_WINDOW)) {
                        showToast(R.string.superplayer_enter_setting_fail)
                        return
                    }
                }
                mSuperPlayer!!.pause()
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
                mWindowParams!!.gravity = Gravity.LEFT or Gravity.TOP
                val rect = configure.floatViewRect
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
                    mSuperPlayer!!.setPlayerView(videoView)
                    mSuperPlayer!!.resume()
                }
            }
            mSuperPlayer!!.switchPlayMode(playerMode)
        }

        override fun onBackPressed(playMode: PlayerMode?) {
            when (playMode) {
                PlayerMode.FULLSCREEN -> {
                    onSwitchPlayMode(PlayerMode.WINDOW)
                }
                PlayerMode.WINDOW -> if (mPlayerViewCallback != null) {
                    mPlayerViewCallback!!.onClickSmallReturnBtn()
                }
                PlayerMode.FLOAT -> {
                    mWindowManager!!.removeView(mFloatPlayer)
                    if (mPlayerViewCallback != null) {
                        mPlayerViewCallback!!.onClickFloatCloseBtn()
                    }
                }
                else -> {
                }
            }
        }

        override fun onShare() {
            mPlayerViewCallback?.onClickShare()
        }

        override fun onTV() {
            mTvController?.startShowTVLink()
        }

        override fun onFloatPositionChange(x: Int, y: Int) {
            mWindowParams!!.x = x
            mWindowParams!!.y = y
            mWindowManager!!.updateViewLayout(mFloatPlayer, mWindowParams)
        }

        override fun onPause() {
            mSuperPlayer!!.pause()
            if (mSuperPlayer!!.playerType != PlayerType.VOD) {
                if (mWatcher != null) {
                    mWatcher!!.stop()
                }
            }
        }

        override fun onResume() {
            if (mSuperPlayer!!.playerState == PlayerState.END) { //重播
                mSuperPlayer!!.reStart()
            } else if (mSuperPlayer!!.playerState == PlayerState.PAUSE) { //继续播放
                mSuperPlayer!!.resume()
            }
        }

        override fun onSeekTo(position: Int) {
            mSuperPlayer!!.seek(position)
        }

        override fun onResumeLive() {
            mSuperPlayer!!.resumeLive()
        }


        override fun onSnapshot() {
            mSuperPlayer!!.snapshot { bitmap ->
                if (bitmap != null) {
                    showSnapshotWindow(bitmap)
                } else {
                    showToast(R.string.superplayer_screenshot_fail)
                }
            }
        }

        override fun onQualityChange(quality: VideoQuality) {
            mFullScreenPlayer!!.updateVideoQuality(quality)
            mWindowPlayer!!.updateVideoQuality(quality)
            mSuperPlayer!!.switchStream(quality)
            showMsg("已切换为${quality.title}播放", "${quality.title}")
        }

        override fun onSpeedChange(speedLevel: Float) {
            mWindowPlayer!!.updateSpeedChange(speedLevel)
            mFullScreenPlayer!!.updateSpeedChange(speedLevel)
            mSuperPlayer!!.setRate(speedLevel)
            onSpeedUpdate(speedLevel)
            showMsg("当前列表已切换为${speedLevel}倍速度播放", "$speedLevel")
        }

        override fun onMirrorToggle(isMirror: Boolean) {
            mSuperPlayer!!.setMirror(isMirror)
        }

        override fun onHWAccelerationToggle(isAccelerate: Boolean) {
            mSuperPlayer!!.enableHardwareDecode(isAccelerate)
        }

        override fun toggle(showing: Boolean) {
            toggleShow(showing)
        }


    }

    open fun onSpeedUpdate(speedLevel: Float) {


    }

    open fun toggleShow(showing: Boolean) {

    }


    /**
     * 拖动完成
     */
    open fun onSeekComplete() {

    }

    /**
     * 修改播放器的渲染模式
     */
    fun changeRenderMode(renderMode: Int) {
        mSuperPlayer?.changeRenderMode(renderMode)
    }

    /**
     * 显示截图窗口
     *
     * @param bmp
     */
    private fun showSnapshotWindow(bmp: Bitmap) {
        val popupWindow = PopupWindow(mContext)
        popupWindow.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
        val view =
            LayoutInflater.from(mContext).inflate(
                R.layout.superplayer_layout_new_vod_snap,
                null
            )
        val imageView = view.findViewById<View>(R.id.superplayer_iv_snap) as ImageView
        imageView.setImageBitmap(bmp)
        popupWindow.contentView = view
        popupWindow.isOutsideTouchable = true
        popupWindow.showAtLocation(mRootView, Gravity.TOP, 1800, 300)
        AsyncTask.execute { save2MediaStore(mContext, bmp) }
        postDelayed({ popupWindow.dismiss() }, 3000)
    }

    /**
     * 旋转屏幕方向
     *
     * @param orientation
     */
    private fun rotateScreenOrientation(orientation: com.tencent.video.superplayer.SuperPlayerDef.Orientation) {
        when (orientation) {
            Orientation.LANDSCAPE -> (mContext as? Activity?)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            Orientation.PORTRAIT -> (mContext as? Activity?)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    /**
     * 检查悬浮窗权限
     *
     *
     * API <18，默认有悬浮窗权限，不需要处理。无法接收无法接收触摸和按键事件，不需要权限和无法接受触摸事件的源码分析
     * API >= 19 ，可以接收触摸和按键事件
     * API >=23，需要在manifest中申请权限，并在每次需要用到权限的时候检查是否已有该权限，因为用户随时可以取消掉。
     * API >25，TYPE_TOAST 已经被谷歌制裁了，会出现自动消失的情况
     */
    private fun checkOp(context: Context?, op: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val manager = context!!.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            try {
                val method = AppOpsManager::class.java.getDeclaredMethod(
                    "checkOp",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    String::class.java
                )
                return AppOpsManager.MODE_ALLOWED == method.invoke(
                    manager,
                    op,
                    Binder.getCallingUid(),
                    context.packageName
                ) as Int
            } catch (e: Exception) {
                TXCLog.e(TAG, Log.getStackTraceString(e))
            }
        }
        return true
    }

    open fun release() {
        if (mWindowPlayer != null) {
            mWindowPlayer!!.release()
        }
        if (mFullScreenPlayer != null) {
            mFullScreenPlayer!!.release()
        }
        if (mFloatPlayer != null) {
            mFloatPlayer!!.release()
        }
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        try {
            release()
        } catch (e: Throwable) {
            TXCLog.e(TAG, Log.getStackTraceString(e))
        }
    }

    open fun switchPlayMode(playerMode: PlayerMode) {
        if (playerMode == PlayerMode.WINDOW) {
            mControllerCallback?.onSwitchPlayMode(PlayerMode.WINDOW)
        } else if (playerMode == PlayerMode.FLOAT) {
            if (mPlayerViewCallback != null) {
                mPlayerViewCallback!!.onStartFloatWindowPlay()
            }
            mControllerCallback?.onSwitchPlayMode(PlayerMode.FLOAT)
        } else {
            mControllerCallback?.onSwitchPlayMode(PlayerMode.FULLSCREEN)
        }
    }

    val playerMode: PlayerMode
        get() = mSuperPlayer!!.playerMode
    val playerState: PlayerState
        get() = mSuperPlayer!!.playerState

    fun setPlayerObserver(mSuperPlayerObserver: SuperPlayerObserver) {
        this.mSuperPlayerObserver = mSuperPlayerObserver
        mSuperPlayer?.setObserver(mSuperPlayerObserver)
    }

    open fun onPlayFinish() {

    }

    private var mSuperPlayerObserver: SuperPlayerObserver = object : SuperPlayerObserver() {
        override fun onPlayBegin() {
            updatePlayState(PlayerState.PLAYING)
            mWindowPlayer!!.hideBackground()
            if (mWatcher != null) {
                mWatcher!!.exitLoading()
            }
        }

        override fun onPlayComplete() {
            super.onPlayComplete()
            onPlayFinish()
            if (TimerConfigure.instance.isCourseTime()) {
                TimerConfigure.instance.stateChange(TimerConfigure.STATE_COMPLETE)
                return
            }
            if (isLiveType()) {
                mSuperPlayer?.reStart()
            } else {
                if (TimerConfigure.instance.isRepeatOne()) {
                    mSuperPlayer?.reStart()
                    return
                } else {
                    getKeyListHelper()?.nextOne()
                }
            }
        }

        override fun onPlayPause() {
            updatePlayState(PlayerState.PAUSE)
        }

        override fun onPlayStop() {
            updatePlayState(PlayerState.END)
            // 清空关键帧和视频打点信息
            if (mWatcher != null) {
                mWatcher!!.stop()
            }
            mFullScreenPlayer!!.updateImageSpriteInfo(null)
            mFullScreenPlayer!!.updateKeyFrameDescInfo(null)
        }

        override fun onPlayLoading() {
            updatePlayState(PlayerState.LOADING)
            if (mWatcher != null) {
                mWatcher!!.enterLoading()
            }
        }

        override fun onVideoSize(width: Int, height: Int) {
            super.onVideoSize(width, height)
            onVideoSizeChange(width, height)
            mPlayerViewCallback?.onVideoSize(width, height)
        }

        override fun onPlayProgress(current: Long, duration: Long) {
            if (!mWindowPlayer!!.isDrag()) {
                mWindowPlayer!!.updateVideoProgress(current, duration)
            }
            if (!mFullScreenPlayer!!.isDrag()) {
                mFullScreenPlayer!!.updateVideoProgress(current, duration)
            }
            mPlayerViewCallback?.onPlayProgress(current, duration)
            if (duration > 0 && playerState != PlayerState.PLAYING) {
                hideLoading()
            }
            onPlayProgressChange(current, duration)
        }

        override fun onSeek(position: Int) {
            if (mSuperPlayer!!.playerType != PlayerType.VOD) {
                if (mWatcher != null) {
                    mWatcher!!.stop()
                }
            }
            onSeekComplete()
        }

        override fun onSwitchStreamStart(
            success: Boolean,
            playerType: PlayerType,
            quality: VideoQuality
        ) {
            if (playerType == PlayerType.LIVE) {
                if (success) {
                    Toast.makeText(mContext, "正在切换到" + quality.title + "...", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        mContext,
                        "切换" + quality.title + "清晰度失败，请稍候重试",
                        Toast.LENGTH_SHORT
                    ).show()
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
                    Toast.makeText(mContext, "清晰度切换成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(mContext, "清晰度切换失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun onPlayerTypeChange(playType: PlayerType?) {
            mWindowPlayer!!.updatePlayType(playType)
            mFullScreenPlayer!!.updatePlayType(playType)
        }

        override fun onPlayTimeShiftLive(player: TXLivePlayer?, url: String?) {
            if (mWatcher == null) {
                mWatcher = NetWatcher(mContext)
            }
            mWatcher!!.start(url, player)
        }

        override fun onVideoQualityListChange(
            videoQualities: ArrayList<VideoQuality>?,
            defaultVideoQuality: VideoQuality?
        ) {
            if (videoQualities != null && videoQualities.isNotEmpty()) {
                mFullScreenPlayer!!.setVideoQualityList(videoQualities)
                mWindowPlayer!!.setVideoQualityList(videoQualities)
            }
            if (defaultVideoQuality != null) {
                mFullScreenPlayer!!.updateVideoQuality(defaultVideoQuality)
                mWindowPlayer!!.updateVideoQuality(defaultVideoQuality)
            }

            if (videoQualities.isNullOrEmpty()) {
                mWindowPlayer?.hideQualities()
                mFullScreenPlayer?.hideQualities()
            }
        }

        override fun onVideoImageSpriteAndKeyFrameChanged(
            info: PlayImageSpriteInfo?,
            list: ArrayList<PlayKeyFrameDescInfo>?
        ) {
            mFullScreenPlayer!!.updateImageSpriteInfo(info)
            mFullScreenPlayer!!.updateKeyFrameDescInfo(list)
        }

        override fun onError(code: Int, message: String?) {
            showToast(message)
        }
    }

    open fun onVideoSizeChange(width: Int, height: Int) {

    }

    open fun isLiveType(): Boolean {
        return isRepeat
    }

    open fun onPlayProgressChange(current: Long, duration: Long) {
        this.currentPosition = current
        this.duration = duration
    }

    open fun updatePlayState(playState: PlayerState?) {
        mWindowPlayer!!.updatePlayState(playState)
        mFullScreenPlayer!!.updatePlayState(playState)
    }

    open fun showToast(message: String?) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }

    open fun showToast(resId: Int) {
        Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show()
    }

    open fun showMsg(text: String, hideText: String) {

    }

    open fun setAutoPlay(auto: Boolean) {
        mAutoPlay = auto
        mSuperPlayer?.autoPlay(auto)
    }

    open fun onBackPressed(): Boolean {
        return if (mSuperPlayer!!.playerMode == PlayerMode.WINDOW
            || mSuperPlayer!!.playerMode == PlayerMode.FLOAT
        ) {
            true
        } else {
            switchPlayMode(PlayerMode.WINDOW)
            false
        }
    }

    fun onDestroy() {
        getFullPlayer()?.onDestroyCallBack()
        resetPlayer()
        removeCallBack()
        getPlayer().destroy()
    }

    open fun removeCallBack() {
        TimerConfigure.instance.removeCallBack(this)
    }


    open fun hide() {
        mFullScreenPlayer!!.hide()
        mWindowPlayer!!.hide()
        mFloatPlayer!!.hide()
    }


    companion object {
        private const val TAG = "SuperPlayerView"
        fun save2MediaStore(context: Context?, image: Bitmap) {
            val sdcardDir = context!!.getExternalFilesDir(null)
            if (sdcardDir == null) {
                Log.e(TAG, "sdcardDir is null")
                return
            }
            val appDir = File(sdcardDir, "superplayer")
            if (!appDir.exists()) {
                appDir.mkdir()
            }
            val dateSeconds = System.currentTimeMillis() / 1000
            val fileName = "$dateSeconds.jpg"
            val file = File(appDir, fileName)
            val filePath = file.absolutePath
            val f = File(filePath)
            if (f.exists()) {
                f.delete()
            }
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(f)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            try {
                // Save the screenshot to the MediaStore
                val values = ContentValues()
                val resolver = context.contentResolver
                values.put(MediaStore.Images.ImageColumns.DATA, filePath)
                values.put(MediaStore.Images.ImageColumns.TITLE, fileName)
                values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName)
                values.put(MediaStore.Images.ImageColumns.DATE_ADDED, dateSeconds)
                values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, dateSeconds)
                values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg")
                values.put(MediaStore.Images.ImageColumns.WIDTH, image.width)
                values.put(MediaStore.Images.ImageColumns.HEIGHT, image.height)
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                val out = resolver.openOutputStream(uri!!)
                image.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out!!.flush()
                out.close()

                // update file size in the database
                values.clear()
                values.put(MediaStore.Images.ImageColumns.SIZE, File(filePath).length())
                resolver.update(uri, values, null, null)
            } catch (e: Exception) {
                TXCLog.e(TAG, Log.getStackTraceString(e))
            }
        }
    }

    override fun onTick(progress: Long) {

    }

    override fun onStateChange(state: Int) {
        when (state) {
            TimerConfigure.STATE_COMPLETE -> {
                if (playerState == PlayerState.PLAYING) {
                    onPause()
                }
            }
            TimerConfigure.STATE_CLOSE -> {
                showMsg("不开启定时关闭", "定时关闭")
            }
            TimerConfigure.STATE_COURSE -> {
                showMsg("视频将在当前片段播放完毕后关闭", "播放完毕")
            }
            TimerConfigure.STATE_START -> {
                showMsg(
                    "视频将在${TimeType.getMin(TimerConfigure.instance.getDuration())}后关闭",
                    TimeType.getMin(TimerConfigure.instance.getDuration())
                )
            }
        }
    }

    override fun onChangeModel(repeatMode: Int) {
        if (repeatMode == TimerConfigure.REPEAT_MODE_ALL) {
            showMsg("已切换为顺序播放", "顺序播放")
        } else {
            showMsg("已切换为单课循环", "单课循环")
        }
    }
}