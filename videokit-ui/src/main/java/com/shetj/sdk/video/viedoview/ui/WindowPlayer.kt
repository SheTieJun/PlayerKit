package com.shetj.sdk.video.viedoview.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.*
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.view.isVisible
import me.shetj.sdk.video.base.PlayerConfig
import com.shetj.sdk.video.casehelper.WinSpeedHelper
import com.shetj.sdk.video.kit.VideoGestureDetector
import me.shetj.sdk.video.base.UIConfig
import com.shetj.sdk.video.casehelper.KeyListListener
import com.shetj.sdk.video.ui.R
import com.shetj.sdk.video.ui.view.*
import com.shetj.sdk.video.viedoview.AbBaseUIPlayer
import me.shetj.sdk.video.player.PlayerDef
import me.shetj.sdk.video.model.VideoQuality
import kotlin.math.roundToInt

/**
 * 窗口模式播放控件
 *
 * 除基本播放控制外，还有手势控制快进快退、手势调节亮度音量等
 *
 * 1、点击事件监听[.onClick]
 *
 * 2、触摸事件监听[.onTouchEvent]
 *
 * 2、进度条事件监听[.onProgressChanged]
 * [.onStartTrackingTouch]
 * [.onStopTrackingTouch]
 */
class WindowPlayer :  AbBaseUIPlayer, View.OnClickListener, PointSeekBar.PointDrag,
    VodMoreView.Callback,
    VodQualityView.Callback,
    PointSeekBar.OnSeekBarChangeListener, KeyListListener {
    // UI控件
    private var mLayoutTop: View? = null // 顶部标题栏布局

    private var mLayoutBottom: LinearLayout? = null// 底部进度条所在布局

    private var mIvPause: ImageView? = null // 暂停播放按钮

    private var mIvFullScreen: ImageView? = null// 全屏按钮

    private var mTvTitle: TextView? = null// 视频名称文本

    private var mTvBackToLive: TextView? = null // 返回直播文本

    private var mBackground: ImageView? = null// 背景

    private var mIvWatermark: ImageView? = null// 水印

    private var mTvCurrent: TextView? = null// 当前进度文本

    private var mTvDuration: TextView? = null// 总时长文本

    private var mSeekBarProgress: PointSeekBar? = null// 播放进度条

    private var mPbLiveLoading: ProgressBar? = null// 加载圈

    private var mGestureVolumeProgress: VolumeBrightnessProgressLayout? = null// 音量亮度调节布局

    private var mGestureVideoProgressLayout: VideoProgressLayout? = null// 手势快进提示布局

    private var mGestureDetector: GestureDetector? = null // 手势检测监听器

    private var mVideoGestureDetector: VideoGestureDetector? = null// 手势控制工具

    private var isShowing = false // 自身是否可见
    private var mIsChangingSeekBarProgress = false// 进度条是否正在拖动，避免SeekBar由于视频播放的update而跳动
    private var mPlayType: PlayerDef.PlayerType? = null// 当前播放视频类型
    private var mCurrentPlayState: PlayerDef.PlayerState? = PlayerDef.PlayerState.END // 当前播放状态
    private var mDuration: Long = 0 // 视频总时长
    private var mLivePushDuration: Long = 0 // 直播推流总时长
    private var mProgress: Long = 0 // 当前播放进度
    private var mBackgroundBmp: Bitmap? = null// 背景图
    private var mWaterMarkBmp: Bitmap? = null// 水印图
    private var mWaterMarkBmpX = 0f// 水印x坐标
    private var mWaterMarkBmpY = 0f// 水印y坐标
    private var mLastClickTime: Long = 0 // 上次点击事件的时间
    private var mSpeedHelper: WinSpeedHelper? = null
    private var mVodQualityView: VodQualityView? = null// 画质列表弹窗
    private var mDefaultVideoQuality: VideoQuality? = null// 默认画质
    private var mVideoQualityList: ArrayList<VideoQuality>? = null // 画质列表
    private var mTvQuality: TextView? = null
    private var mFirstShowQuality = false // 是都是首次显示画质信息
    private var mIvTV: View? = null

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

    /**
     * 初始化控件、手势检测监听器、亮度/音量/播放进度的回调
     */
    private fun initialize(context: Context?) {
        initView(context)
        mGestureDetector =
            GestureDetector(getContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    if (isLive()) return false  //直播双击不做处理
                    togglePlayState()
                    show()
                    delayHide()
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    if (isLive()) {
                        mControllerCallback?.onControlViewToggle(!isShowing)
                        return false
                    }
                    toggle()
                    return true
                }

                override fun onScroll(
                    downEvent: MotionEvent?,
                    moveEvent: MotionEvent?,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    if (downEvent == null || moveEvent == null) {
                        return false
                    }
                    if (mVideoGestureDetector != null && mGestureVolumeProgress != null) {
                        mVideoGestureDetector!!.check(
                            mGestureVolumeProgress!!.height,
                            downEvent,
                            moveEvent,
                            distanceX,
                            distanceY
                        )
                    }
                    return true
                }

                override fun onDown(e: MotionEvent?): Boolean {
                    if (mVideoGestureDetector != null) {
                        mVideoGestureDetector!!.reset(width, mSeekBarProgress!!.progress)
                    }
                    return true
                }
            })
        mGestureDetector!!.setIsLongpressEnabled(false)
        mVideoGestureDetector = VideoGestureDetector(getContext())
        mVideoGestureDetector!!.setVideoGestureListener(object :
            VideoGestureDetector.VideoGestureListener {
            override fun onBrightnessGesture(newBrightness: Float) {
                if (mGestureVolumeProgress != null) {
                    mGestureVolumeProgress!!.setProgress((newBrightness * 100).toInt())
                    mGestureVolumeProgress!!.setImageResource(R.drawable.superplayer_ic_light_max)
                    mGestureVolumeProgress!!.show()
                }
            }

            override fun onVolumeGesture(volumeProgress: Float) {
                if (mGestureVolumeProgress != null) {
                    mGestureVolumeProgress!!.setImageResource(R.drawable.superplayer_ic_volume_max)
                    mGestureVolumeProgress!!.setProgress(volumeProgress.toInt())
                    mGestureVolumeProgress!!.show()
                }
            }

            override fun onSeekGesture(seekProgress: Int) {
                if (isLive()) return
                var progressTemp = seekProgress
                mIsChangingSeekBarProgress = true
                if (mGestureVideoProgressLayout != null) {
                    if (progressTemp > mSeekBarProgress!!.max) {
                        progressTemp = mSeekBarProgress!!.max
                    }
                    if (progressTemp < 0) {
                        progressTemp = 0
                    }
                    mGestureVideoProgressLayout!!.setProgress(progressTemp)
                    mGestureVideoProgressLayout!!.show()
                    val percentage = progressTemp.toFloat() / mSeekBarProgress!!.max
                    var currentTime = mDuration * percentage
                    if (mPlayType == PlayerDef.PlayerType.LIVE || mPlayType == PlayerDef.PlayerType.LIVE_SHIFT) {
                        currentTime = if (mLivePushDuration > MAX_SHIFT_TIME) {
                            (mLivePushDuration - MAX_SHIFT_TIME * (1 - percentage))
                        } else {
                            mLivePushDuration * percentage
                        }
                        mGestureVideoProgressLayout!!.setTimeText(formattedTime(currentTime.toLong()))
                    } else {
                        mGestureVideoProgressLayout!!.setTimeText(
                            formattedTime(currentTime.toLong()) + " / " + formattedTime(
                                mDuration
                            )
                        )
                        updateVideoProgress(currentTime.toLong(), mDuration)
                    }
                }
                if (mSeekBarProgress != null) mSeekBarProgress!!.progress = (progressTemp)
            }
        })
    }

    fun isLive() = (mPlayType == PlayerDef.PlayerType.LIVE || mPlayType == PlayerDef.PlayerType.LIVE_SHIFT)


    override fun updateUIConfig(uiConfig: UIConfig) {
        super.updateUIConfig(uiConfig)
        mSpeedHelper?.updateUIConfig(uiConfig)
        uiConfig.let {
            mIvTV?.isVisible = uiConfig.showTV
            mLayoutTop?.isVisible = uiConfig.showTop
            mLayoutBottom?.isVisible = uiConfig.showBottom
            mSpeedHelper?.getSpeedView()?.isVisible = uiConfig.showSpeed
            isShowing = uiConfig.showTop || uiConfig.showBottom
            mIvFullScreen?.isVisible = uiConfig.showFull
            delayHide()
        }
    }

    override fun updatePlayConfig(config: PlayerConfig) {
        super.updatePlayConfig(config)
        mSpeedHelper?.updatePlayConfig(config)
    }

    /**
     * 初始化view
     */
    private fun initView(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.superplayer_vod_player_window, this)
        mLayoutTop = findViewById(R.id.superplayer_rl_top)
        mLayoutTop!!.setOnClickListener(this)
        mLayoutBottom = findViewById<View>(R.id.superplayer_ll_bottom) as LinearLayout
        mLayoutBottom!!.setOnClickListener(this)
        mTvTitle = findViewById<View>(R.id.superplayer_tv_title) as TextView
        mIvPause = findViewById<View>(R.id.superplayer_iv_pause) as ImageView
        mTvCurrent = findViewById<View>(R.id.superplayer_tv_current) as TextView
        mTvDuration = findViewById<View>(R.id.superplayer_tv_duration) as TextView
        mSeekBarProgress = findViewById<View>(R.id.superplayer_seekbar_progress) as PointSeekBar
        mSeekBarProgress!!.progress = (0)
        mSeekBarProgress!!.max = (100)
        mIvFullScreen = findViewById<View>(R.id.superplayer_iv_fullscreen) as ImageView
        mTvBackToLive = findViewById<View>(R.id.superplayer_tv_back_to_live) as TextView
        mPbLiveLoading = findViewById<View>(R.id.superplayer_pb_live) as ProgressBar
        mTvBackToLive!!.setOnClickListener(this)
        mIvPause!!.setOnClickListener(this)
        mIvFullScreen!!.setOnClickListener(this)
        mLayoutTop!!.setOnClickListener(this)
        mSeekBarProgress!!.setOnSeekBarChangeListener(this)
        mGestureVolumeProgress =
            findViewById<View>(R.id.superplayer_gesture_progress) as VolumeBrightnessProgressLayout
        mGestureVideoProgressLayout =
            findViewById<View>(R.id.superplayer_video_progress_layout) as VideoProgressLayout
        mBackground = findViewById<View>(R.id.superplayer_small_iv_background) as ImageView
        setBackground(mBackgroundBmp)
        mIvWatermark = findViewById<View>(R.id.superplayer_small_iv_water_mark) as ImageView
        mSpeedHelper = WinSpeedHelper(this)
        mVodQualityView = findViewById(R.id.superplayer_vod_quality)
        mVodQualityView!!.setCallback(false, this)
        mTvQuality = findViewById(R.id.superplayer_tv_quality)
        if (mDefaultVideoQuality != null) {
            mTvQuality!!.text = mDefaultVideoQuality!!.title
        }
        mIvTV = findViewById(R.id.iv_tv)
        mIvTV!!.setOnClickListener(this)
        mTvQuality!!.setOnClickListener(this)
    }

    override fun isDrag() = mSeekBarProgress?.mIsOnDrag == true

    /**
     * 切换播放状态
     *
     * 双击和点击播放/暂停按钮会触发此方法
     */
    private fun togglePlayState() {
        when (mCurrentPlayState) {
            PlayerDef.PlayerState.PAUSE, PlayerDef.PlayerState.END -> if (mControllerCallback != null) {
                mControllerCallback!!.onResume()
            }
            PlayerDef.PlayerState.PLAYING, PlayerDef.PlayerState.LOADING -> {
                if (mControllerCallback != null) {
                    mControllerCallback!!.onPause()
                }
            }
        }
        show()
    }

    /**
     * 切换自身的可见性
     */
    private fun toggle() {
        if (isShowing) {
            hide()
        } else {
            show()
        }
        mControllerCallback?.onControlViewToggle(isShowing)
    }

    /**
     * 设置水印
     *
     * @param bmp 水印图
     * @param x   水印的x坐标
     * @param y   水印的y坐标
     */
    override fun setWatermark(bmp: Bitmap?, x: Float, y: Float) {
        mWaterMarkBmp = bmp
        mWaterMarkBmpX = x
        mWaterMarkBmpY = y
        if (bmp != null) {
            post {
                val width = this@WindowPlayer.width
                val height = this@WindowPlayer.height
                val xTemp = (width * mWaterMarkBmpX).toInt() - bmp.width / 2
                val yTemp = (height * mWaterMarkBmpY).toInt() - bmp.height / 2
                mIvWatermark!!.x = xTemp.toFloat()
                mIvWatermark!!.y = yTemp.toFloat()
                mIvWatermark!!.visibility = VISIBLE
                setBitmap(mIvWatermark, bmp)
            }
        } else {
            mIvWatermark!!.visibility = GONE
        }
    }

    /**
     * 显示控件
     */
    override fun show() {
        isShowing = true
        isShowControl(true)
        delayHide()
    }

    private fun delayHide() {
        removeCallbacks(mHideViewRunnable)
        postDelayed(mHideViewRunnable, 7000)
    }

    private fun isShowControl(isShow: Boolean) {
        if (mPlayType == PlayerDef.PlayerType.LIVE_SHIFT) {
            mTvBackToLive!!.isVisible = isShow
        }
        if (isShow) {
            if (mLayoutTop!!.visibility != View.VISIBLE) {
                if (uiConfig.showTop) {
                    mLayoutTop?.animation =
                        AnimationUtils.loadAnimation(context, R.anim.push_top_in)
                    mLayoutTop?.isVisible = isShow
                }
            }
            if (mLayoutBottom!!.visibility != View.VISIBLE) {
                if (uiConfig.showBottom) {
                    mLayoutBottom?.animation =
                        AnimationUtils.loadAnimation(context, R.anim.push_bottom_in)
                    mLayoutBottom?.isVisible = isShow
                }
            }
        } else {
            if (mLayoutTop!!.visibility == View.VISIBLE) {
                if (uiConfig.showTop) {
                    mLayoutTop?.animation =
                        AnimationUtils.loadAnimation(context, R.anim.push_top_out)
                    mLayoutTop?.isVisible = isShow
                }
            }
            if (mLayoutBottom!!.visibility == View.VISIBLE) {
                if (uiConfig.showBottom) {
                    mLayoutBottom?.animation =
                        AnimationUtils.loadAnimation(context, R.anim.push_bottom_out)
                    mLayoutBottom?.isVisible = isShow
                }
            }
        }
    }

    /**
     * 隐藏控件
     */
    override fun hide() {
        isShowing = false
        isShowControl(false)
        showQualityView(false)
        mSpeedHelper?.showSpeedList(false)
    }

    override fun updatePlayState(playState: PlayerDef.PlayerState?) {
        when (playState) {
            PlayerDef.PlayerState.PLAYING -> {
                mIvPause!!.setImageResource(R.drawable.superplayer_ic_vod_pause_normal)
            }
            PlayerDef.PlayerState.LOADING -> {
                mIvPause!!.setImageResource(R.drawable.superplayer_ic_vod_pause_normal)
            }
            PlayerDef.PlayerState.PAUSE -> {
                mIvPause!!.setImageResource(R.drawable.superplayer_ic_vod_play_normal)
            }
            PlayerDef.PlayerState.END -> {
                mIvPause!!.setImageResource(R.drawable.superplayer_ic_vod_play_normal)
            }
        }
        mCurrentPlayState = playState
    }

    /**
     * 更新视频名称
     *
     * @param title 视频名称
     */
    override fun updateTitle(title: String?) {
        mTvTitle!!.text = title
    }

    /**
     * 更新视频播放进度
     *
     * @param current  当前进度(秒)
     * @param duration 视频总时长(秒)
     */
    override fun updateVideoProgress(current: Long, duration: Long) {
        mProgress = if (current < 0) 0 else current
        mDuration = if (duration < 0) 0 else duration
        mTvCurrent!!.text = formattedTime(mProgress)
        var percentage = if (mDuration > 0) mProgress.toFloat() / mDuration.toFloat() else 1.0f
        if (mProgress == 0L) {
            mLivePushDuration = 0
            percentage = 0f
        }
        if (mPlayType == PlayerDef.PlayerType.LIVE || mPlayType == PlayerDef.PlayerType.LIVE_SHIFT) {
            mLivePushDuration = if (mLivePushDuration > mProgress) mLivePushDuration else mProgress
            val leftTime = mDuration - mProgress
            mDuration =
                if (mDuration > MAX_SHIFT_TIME) MAX_SHIFT_TIME.toLong() else mDuration
            percentage = 1 - leftTime.toFloat() / mDuration.toFloat()
        }
        if (percentage in 0.0..1.0) {
            val progress = (percentage * mSeekBarProgress!!.max).roundToInt()
            if (!mIsChangingSeekBarProgress) {
                if (mPlayType == PlayerDef.PlayerType.LIVE) {
                    mSeekBarProgress!!.progress = (mSeekBarProgress!!.max)
                } else {
                    mSeekBarProgress!!.progress = (progress)
                }
            }
            mTvDuration!!.text = formattedTime(mDuration)
        }
    }

    override fun updatePlayType(type: PlayerDef.PlayerType?) {
        mPlayType = type
        when (type) {
            PlayerDef.PlayerType.VOD -> {
                mTvBackToLive!!.visibility = GONE
                mTvDuration!!.visibility = VISIBLE
            }
            PlayerDef.PlayerType.LIVE -> {
                mTvBackToLive!!.visibility = GONE
                mTvDuration!!.visibility = GONE
                mSeekBarProgress!!.progress = (100)
            }
            PlayerDef.PlayerType.LIVE_SHIFT -> {
                if (mLayoutBottom!!.visibility == VISIBLE) mTvBackToLive!!.visibility = VISIBLE
                mTvDuration!!.visibility = GONE
            }
        }
    }

    /**
     * 设置背景
     *
     * @param bitmap 背景图
     */
    override fun setBackground(bitmap: Bitmap?) {
        post(Runnable {
            if (bitmap == null) return@Runnable
            if (mBackground == null) {
                mBackgroundBmp = bitmap
            } else {
                setBitmap(mBackground, mBackgroundBmp)
            }
        })
    }

    /**
     * 设置目标ImageView显示的图片
     */
    private fun setBitmap(view: ImageView?, bitmap: Bitmap?) {
        if (view == null || bitmap == null) return
        view.background = BitmapDrawable(context.resources, bitmap)
    }

    /**
     * 显示背景
     */
    override fun showBackground() {
        post {
            val alpha = ValueAnimator.ofFloat(0.0f, 1f)
            alpha.duration = 500
            alpha.addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                mBackground!!.alpha = value
                if (value == 1f) {
                    mBackground!!.visibility = VISIBLE
                }
            }
            alpha.start()
        }
    }

    /**
     * 隐藏背景
     */
    override fun hideBackground() {
        post(Runnable {
            if (mBackground!!.visibility != VISIBLE) return@Runnable
            val alpha = ValueAnimator.ofFloat(1.0f, 0.0f)
            alpha.duration = 500
            alpha.addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                mBackground!!.alpha = value
                if (value == 0f) {
                    mBackground!!.visibility = GONE
                }
            }
            alpha.start()
        })
    }
    private var seekTime: Int = 0

    /**
     * 重写触摸事件监听，实现手势调节亮度、音量以及播放进度
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mGestureDetector != null) mGestureDetector!!.onTouchEvent(event)
        if ((event?.action == MotionEvent.ACTION_CANCEL || event?.action == MotionEvent.ACTION_UP) && mVideoGestureDetector != null && mVideoGestureDetector!!.isVideoProgressModel) {
            var progress = mVideoGestureDetector!!.videoProgress
            if (progress > mSeekBarProgress!!.max) {
                progress = mSeekBarProgress!!.max
            }
            if (progress < 0) {
                progress = 0
            }
            mSeekBarProgress!!.progress = (progress)
            val percentage = progress * 1.0f / mSeekBarProgress!!.max

            seekTime = if (mPlayType == PlayerDef.PlayerType.LIVE || mPlayType == PlayerDef.PlayerType.LIVE_SHIFT) {
                if (mLivePushDuration > Companion.MAX_SHIFT_TIME) {
                    (mLivePushDuration - Companion.MAX_SHIFT_TIME * (1 - percentage)).toInt()
                } else {
                    (mLivePushDuration * percentage).toInt()
                }
            } else {
                (percentage * mDuration).toInt()
            }
            if (mControllerCallback != null) {
                mControllerCallback!!.onSeekTo(seekTime)
            }
            mIsChangingSeekBarProgress = false
        }
        if (event?.action == MotionEvent.ACTION_DOWN) {
            removeCallbacks(mHideViewRunnable)
        } else if (event?.action == MotionEvent.ACTION_UP) {
            postDelayed(mHideViewRunnable, 7000)
        }
        return true
    }

    override val playerMode: PlayerDef.PlayerMode
        get() = PlayerDef.PlayerMode.WINDOW

    override fun updateSpeedChange(speedLevel: Float) {
        mSpeedHelper?.showSpeedImage()
    }


    override fun onSpeedChange(speedLevel: Float) {
        if (mControllerCallback != null) {
            mControllerCallback!!.onSpeedChange(speedLevel)
        }
    }

    override fun onMirrorChange(isMirror: Boolean) {
        if (mControllerCallback != null) {
            mControllerCallback!!.onMirrorToggle(isMirror)
        }
    }

    override fun onHWAcceleration(isAccelerate: Boolean) {
        if (mControllerCallback != null) {
            mControllerCallback!!.onHWAccelerationToggle(isAccelerate)
        }
    }

    /**
     * 设置点击事件监听
     */
    override fun onClick(view: View) {
        if (System.currentTimeMillis() - mLastClickTime < 300) { //限制点击频率
            return
        }
        mLastClickTime = System.currentTimeMillis()
        val id = view.id
        if (id == R.id.superplayer_rl_top) { //顶部标题栏
            if (mControllerCallback != null) {
                mControllerCallback!!.onBackPressed(PlayerDef.PlayerMode.WINDOW)
            }
        } else if (id == R.id.superplayer_iv_pause) { //暂停\播放按钮
            togglePlayState()
        } else if (id == R.id.superplayer_iv_fullscreen) { //全屏按钮
            if (mControllerCallback != null) {
                mControllerCallback!!.onSwitchPlayMode(PlayerDef.PlayerMode.FULLSCREEN)
            }
        } else if (id == R.id.superplayer_tv_back_to_live) { //返回直播按钮
            if (mControllerCallback != null) {
                mControllerCallback!!.onResumeLive()
            }
        } else if (id == R.id.superplayer_tv_quality) {
            showQualityView()
        } else if (id == R.id.iv_tv) {
            showTvLink()
        }
    }

    /**
     * 展示投屏信息
     */
    private fun showTvLink() {
        mControllerCallback?.onShotScreen()
    }

    override fun onProgressChanged(seekBar: PointSeekBar, progress: Int, fromUser: Boolean) {
        if (mGestureVideoProgressLayout != null && fromUser) {
            mGestureVideoProgressLayout!!.show()
            val percentage = progress.toFloat() / seekBar.max
            var currentTime = mDuration * percentage
            if (mPlayType == PlayerDef.PlayerType.LIVE || mPlayType == PlayerDef.PlayerType.LIVE_SHIFT) {
                (if (mLivePushDuration > MAX_SHIFT_TIME) {
                    (mLivePushDuration - MAX_SHIFT_TIME * (1 - percentage))
                } else {
                    mLivePushDuration * percentage
                }).also { currentTime = it }
                mGestureVideoProgressLayout!!.setTimeText(formattedTime(currentTime.toLong()))
            } else {
                mGestureVideoProgressLayout!!.setTimeText(
                    formattedTime(currentTime.toLong()) + " / " + formattedTime(
                        mDuration
                    )
                )
                updateVideoProgress(currentTime.toLong(), mDuration)
            }
            mGestureVideoProgressLayout!!.setProgress(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: PointSeekBar?) {
        removeCallbacks(mHideViewRunnable)

    }

    override fun onStopTrackingTouch(seekBar: PointSeekBar) {
        val curProgress = seekBar.progress
        val maxProgress = seekBar.max
        when (mPlayType) {
            PlayerDef.PlayerType.VOD -> if (curProgress in 0..maxProgress) {
                // 关闭重播按钮
                val percentage = curProgress.toFloat() / maxProgress
                val position = (mDuration * percentage).toInt()
                if (mControllerCallback != null) {
                    mControllerCallback!!.onSeekTo(position)
                }
            }
            PlayerDef.PlayerType.LIVE, PlayerDef.PlayerType.LIVE_SHIFT -> {
                var seekTime = (mLivePushDuration * curProgress * 1.0f / maxProgress).toInt()
                if (mLivePushDuration > MAX_SHIFT_TIME) {
                    seekTime =
                        (mLivePushDuration - MAX_SHIFT_TIME * (maxProgress - curProgress) * 1.0f / maxProgress).toInt()
                }
                if (mControllerCallback != null) {
                    mControllerCallback!!.onSeekTo(seekTime)
                }
            }
        }
        postDelayed(mHideViewRunnable, 7000)
    }

    override fun onQualitySelect(quality: VideoQuality) {
        if (mControllerCallback != null) {
            mControllerCallback!!.onQualityChange(quality)
        }
        showQualityView(false)
    }

    private fun showQualityView(isShow: Boolean) {
        if (isShow) {
            isShowControl(false)
            if (mVodQualityView?.visibility == View.GONE) {
                mVodQualityView?.visibility = View.VISIBLE
                mVodQualityView?.animation =
                    AnimationUtils.loadAnimation(context, R.anim.slide_right_in)
            }
        } else {
            if (mVodQualityView?.visibility == View.VISIBLE) {
                mVodQualityView?.visibility = View.GONE
                mVodQualityView?.animation =
                    AnimationUtils.loadAnimation(context, R.anim.slide_right_exit)
            }
        }
    }

    override fun showLoading() {
        mPbLiveLoading?.let { toggleView(it, true) }
    }

    override fun hideLoading() {
        mPbLiveLoading?.let { toggleView(it, false) }
    }

    /**
     * 设置视频画质信息
     *
     * @param list 画质列表
     */
    override fun setVideoQualityList(list: ArrayList<VideoQuality>?) {

        if (list?.isEmpty() == true) {
            mTvQuality?.isVisible = false
        } else {
            mVideoQualityList = list
            mFirstShowQuality = false
            mTvQuality?.isVisible = true
        }
    }

    override fun updateVideoQuality(videoQuality: VideoQuality?) {
        if (videoQuality == null) {
            mTvQuality!!.text = ""
            return
        }
        mDefaultVideoQuality = videoQuality
        if (mTvQuality != null) {
            mTvQuality!!.text = videoQuality.title
        }
        if (mVideoQualityList != null && mVideoQualityList!!.isNotEmpty()) {
            for (i in mVideoQualityList!!.indices) {
                val quality = mVideoQualityList!![i]
                if (quality.title != null && quality.title == mDefaultVideoQuality!!.title) {
                    mVodQualityView!!.setDefaultSelectedQuality(i)
                    break
                }
            }
        }
    }

    private fun showQualityView() {
        if (mVideoQualityList == null || mVideoQualityList!!.size == 0) {
            return
        }
        if (mVideoQualityList!!.size == 1
            && (TextUtils.isEmpty(mVideoQualityList!![0].title))
        ) {
            return
        }
        // 设置默认显示分辨率文字
        showQualityView(true)
        if (!mFirstShowQuality && mDefaultVideoQuality != null) {
            for (i in mVideoQualityList!!.indices) {
                val quality = mVideoQualityList!![i]
                if (quality.title != null && quality.title == mDefaultVideoQuality!!.title) {
                    mVodQualityView!!.setDefaultSelectedQuality(i)
                    break
                }
            }
            mFirstShowQuality = true
        }
        mVodQualityView!!.setVideoQualityList(mVideoQualityList)
    }

}