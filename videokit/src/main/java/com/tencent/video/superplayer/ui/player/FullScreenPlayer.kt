package com.tencent.video.superplayer.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.text.TextUtils
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.view.isVisible
import com.tencent.liteav.superplayer.R
import com.tencent.liteav.superplayer.databinding.SuperplayerVodPlayerFullscreenBinding
import com.tencent.rtmp.TXImageSprite
import com.tencent.video.superplayer.SuperPlayerDef.*
import com.tencent.video.superplayer.base.ConfigInterface
import com.tencent.video.superplayer.base.PlayerConfig
import com.tencent.video.superplayer.casehelper.PlayKeyListHelper
import com.tencent.video.superplayer.casehelper.VideoCaseHelper
import com.tencent.video.superplayer.casehelper.WinSpeedHelper
import com.tencent.video.superplayer.model.entity.PlayImageSpriteInfo
import com.tencent.video.superplayer.model.entity.PlayKeyFrameDescInfo
import com.tencent.video.superplayer.model.entity.VideoQuality
import com.tencent.video.superplayer.model.utils.VideoGestureDetector
import com.tencent.video.superplayer.base.UIConfig
import com.tencent.video.superplayer.ui.view.*
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

/**
 * 全屏模式播放控件
 *
 * 除[WindowPlayer]基本功能外，还包括进度条关键帧打点信息显示与跳转、快进快退时缩略图的显示、切换画质
 * 镜像播放、硬件加速、倍速播放、弹幕、截图等功能
 *
 * 1、点击事件监听[.onClick]
 *
 * 2、触摸事件监听[.onTouchEvent]
 *
 * 3、进度条滑动事件监听[.onProgressChanged]
 * [.onStartTrackingTouch][.onStopTrackingTouch]
 *
 * 4、进度条打点信息点击监听[.onSeekBarPointClick]
 *
 * 5、切换画质监听[.onQualitySelect]
 *
 * 6、倍速播放监听[.onSpeedChange]
 *
 * 7、镜像播放监听[.onMirrorChange]
 *
 * 8、硬件加速监听[.onHWAcceleration]
 *
 */
class FullScreenPlayer : AbsPlayer, View.OnClickListener, VodMoreView.Callback,
    VodQualityView.Callback, PointSeekBar.OnSeekBarChangeListener,
    PointSeekBar.OnSeekBarPointClickListener, ConfigInterface {
    private lateinit var playerConfig: PlayerConfig
    private lateinit var uiConfig: UIConfig

    private lateinit var mViewBinding: SuperplayerVodPlayerFullscreenBinding

    // UI控件
    private var mLayoutTop // 顶部标题栏布局
            : View? = null
    private var mLayoutBottom // 底部进度条所在布局
            : View? = null
    private var mIvPause // 暂停播放按钮
            : ImageView? = null
    private var mTvTitle // 视频名称文本
            : TextView? = null
    private var mTvBackToLive // 返回直播文本
            : TextView? = null
    private var mIvWatermark // 水印
            : ImageView? = null
    private var mTvCurrent // 当前进度文本
            : TextView? = null
    private var mTvDuration // 总时长文本
            : TextView? = null
    private var mSeekBarProgress // 播放进度条
            : PointSeekBar? = null
    private var mPbLiveLoading // 加载圈
            : ProgressBar? = null
    private var mGestureVolumeBrightnessProgressLayout // 音量亮度调节布局
            : VolumeBrightnessProgressLayout? = null
    private var mGestureVideoProgressLayout // 手势快进提示布局
            : VideoProgressLayout? = null
    private var mTvQuality // 当前画质文本
            : TextView? = null
    private var mIvBack // 顶部标题栏中的返回按钮
            : ImageView? = null
    private var mIvLock // 锁屏按钮
            : ImageView? = null
    private var mIvMore // 更多设置弹窗按钮
            : ImageView? = null
    private var mVodQualityView // 画质列表弹窗
            : VodQualityView? = null
    private var mVodMoreView // 更多设置弹窗
            : VodMoreView? = null
    private var mTvVttText // 关键帧打点信息文本
            : TextView? = null
    private var mHideLockViewRunnable // 隐藏锁屏按钮子线程
            : HideLockViewRunnable? = null
    private var mGestureDetector // 手势检测监听器
            : GestureDetector? = null
    private var mVideoGestureDetector // 手势控制工具
            : VideoGestureDetector? = null
    private var isShowing // 自身是否可见
            = false
    private var mIsChangingSeekBarProgress // 进度条是否正在拖动，避免SeekBar由于视频播放的update而跳动
            = false
    private var mPlayType // 当前播放视频类型
            : PlayerType? = null
    private var mCurrentPlayState: PlayerState? = PlayerState.END // 当前播放状态
    private var mDuration // 视频总时长
            : Long = 0
    private var mLivePushDuration // 直播推流总时长
            : Long = 0
    private var mProgress // 当前播放进度
            : Long = 0
    private val mBackgroundBmp // 背景图
            : Bitmap? = null
    private var mWaterMarkBmp // 水印图
            : Bitmap? = null
    private var mWaterMarkBmpX // 水印x坐标
            = 0f
    private var mWaterMarkBmpY // 水印y坐标
            = 0f
    private var mBarrageOn // 弹幕是否开启
            = false
    private var mLockScreen // 是否锁屏
            = false
    private var mTXImageSprite // 雪碧图信息
            : TXImageSprite? = null
    private var mTXPlayKeyFrameDescInfoList // 关键帧信息
            : ArrayList<PlayKeyFrameDescInfo>? = null
    private var mSelectedPos = -1 // 点击的关键帧时间点
    private var mDefaultVideoQuality // 默认画质
            : VideoQuality? = null
    private var mVideoQualityList // 画质列表
            : ArrayList<VideoQuality>? = null
    private var mFirstShowQuality // 是都是首次显示画质信息
            = false
    private var mIvSpeed: ImageView? = null
    private var keyListHelper: PlayKeyListHelper? = null
    private var mTopShare: View? = null
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
    private fun initialize(context: Context) {
        initView(context)
        mGestureDetector =
            GestureDetector(getContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    if (mLockScreen || isLive()) return false
                    togglePlayState()
                    show()
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    toggle()
                    return true
                }

                override fun onScroll(
                    downEvent: MotionEvent?,
                    moveEvent: MotionEvent?,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    if (mLockScreen || isLive()) return false
                    if (downEvent == null || moveEvent == null) {
                        return false
                    }
                    if (mVideoGestureDetector != null && mGestureVolumeBrightnessProgressLayout != null) {
                        mVideoGestureDetector!!.check(
                            mGestureVolumeBrightnessProgressLayout!!.height,
                            downEvent,
                            moveEvent,
                            distanceX,
                            distanceY
                        )
                    }
                    return true
                }

                override fun onDown(e: MotionEvent?): Boolean {
                    if (mLockScreen || isLive()) return true
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
                if (mGestureVolumeBrightnessProgressLayout != null) {
                    mGestureVolumeBrightnessProgressLayout!!.setProgress((newBrightness * 100).toInt())
                    mGestureVolumeBrightnessProgressLayout!!.setImageResource(R.drawable.superplayer_ic_light_max)
                    mGestureVolumeBrightnessProgressLayout!!.show()
                }
            }

            override fun onVolumeGesture(volumeProgress: Float) {
                if (mGestureVolumeBrightnessProgressLayout != null) {
                    mGestureVolumeBrightnessProgressLayout!!.setImageResource(R.drawable.superplayer_ic_volume_max)
                    mGestureVolumeBrightnessProgressLayout!!.setProgress(volumeProgress.toInt())
                    mGestureVolumeBrightnessProgressLayout!!.show()
                }
            }

            override fun onSeekGesture(progress: Int) {
                if (isLive()) return
                var progress = progress
                mIsChangingSeekBarProgress = true
                if (mGestureVideoProgressLayout != null) {
                    if (progress > mSeekBarProgress!!.max) {
                        progress = mSeekBarProgress!!.max
                    }
                    if (progress < 0) {
                        progress = 0
                    }
                    mGestureVideoProgressLayout!!.setProgress(progress)
                    mGestureVideoProgressLayout!!.show()
                    val percentage = progress.toFloat() / mSeekBarProgress!!.max
                    var currentTime = mDuration * percentage
                    if (mPlayType == PlayerType.LIVE || mPlayType == PlayerType.LIVE_SHIFT) {
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
                    setThumbnail(progress)
                }
                mSeekBarProgress?.progress = (progress)
            }
        })
    }

    fun isLive() = (mPlayType == PlayerType.LIVE || mPlayType == PlayerType.LIVE_SHIFT)

    /**
     * 初始化view
     */
    private fun initView(context: Context) {
        mViewBinding = SuperplayerVodPlayerFullscreenBinding.inflate(LayoutInflater.from(context))
        mHideLockViewRunnable = HideLockViewRunnable(this)
        LayoutInflater.from(context).inflate(R.layout.superplayer_vod_player_fullscreen, this)
        mLayoutTop = findViewById(R.id.superplayer_rl_top)
        mLayoutTop!!.setOnClickListener(this)
        mLayoutBottom = findViewById(R.id.superplayer_ll_bottom)
        mLayoutBottom!!.setOnClickListener(this)
        mIvBack = findViewById<View>(R.id.superplayer_iv_back) as ImageView
        mIvLock = findViewById<View>(R.id.superplayer_iv_lock) as ImageView
        mTvTitle = findViewById<View>(R.id.superplayer_tv_title) as TextView
        mIvPause = findViewById<View>(R.id.superplayer_iv_pause) as ImageView
        mIvMore = findViewById<View>(R.id.superplayer_iv_more) as ImageView
        mTvCurrent = findViewById<View>(R.id.superplayer_tv_current) as TextView
        mTvDuration = findViewById<View>(R.id.superplayer_tv_duration) as TextView
        mSeekBarProgress = findViewById<View>(R.id.superplayer_seekbar_progress) as PointSeekBar
        mSeekBarProgress!!.progress = (0)
        mSeekBarProgress!!.setOnPointClickListener(this)
        mSeekBarProgress!!.setOnSeekBarChangeListener(this)
        mTvQuality = findViewById(R.id.superplayer_tv_quality)
        mTvBackToLive = findViewById(R.id.superplayer_tv_back_to_live)
        mPbLiveLoading = findViewById(R.id.superplayer_pb_live)
        mVodQualityView = findViewById(R.id.superplayer_vod_quality)
        mVodQualityView!!.setCallback(true, this)
        mVodMoreView = findViewById(R.id.superplayer_vod_more)
        mVodMoreView!!.setCallback(this)
        mTvBackToLive!!.setOnClickListener(this)
        mIvLock!!.setOnClickListener(this)
        mIvBack!!.setOnClickListener(this)
        mIvPause!!.setOnClickListener(this)
        mIvMore!!.setOnClickListener(this)
        mTvQuality!!.setOnClickListener(this)
        mTvVttText = findViewById<View>(R.id.superplayer_large_tv_vtt_text) as TextView
        mTvVttText!!.setOnClickListener(this)
        if (mDefaultVideoQuality != null) {
            mTvQuality!!.text = mDefaultVideoQuality!!.title
        }
        mGestureVolumeBrightnessProgressLayout =
            findViewById<View>(R.id.superplayer_gesture_progress) as VolumeBrightnessProgressLayout
        mGestureVideoProgressLayout =
            findViewById<View>(R.id.superplayer_video_progress_layout) as VideoProgressLayout
        mIvWatermark = findViewById<View>(R.id.superplayer_large_iv_water_mark) as ImageView
        mIvSpeed = findViewById(R.id.iv_speed)
        mIvSpeed!!.setOnClickListener(this)
        mTopShare = findViewById(R.id.top_share)
        mTopShare!!.setOnClickListener(this)
        mIvTV = findViewById(R.id.iv_tv)
        mIvTV!!.setOnClickListener(this)
        keyListHelper = PlayKeyListHelper(this)

    }

    fun getKeyListHelper(): PlayKeyListHelper? {
        return keyListHelper
    }

    fun getCaseHelper(): VideoCaseHelper? {
        return mVodMoreView?.getCaseHelper()
    }

    /**
     * 切换播放状态
     *
     * 双击和点击播放/暂停按钮会触发此方法
     */
    private fun togglePlayState() {
        when (mCurrentPlayState) {
            PlayerState.PAUSE, PlayerState.END -> if (mControllerCallback != null) {
                mControllerCallback!!.onResume()
            }
            PlayerState.PLAYING, PlayerState.LOADING -> {
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
        if (!mLockScreen) {
            if (isShowing) {
                hide()
            } else {
                show()
            }
            mControllerCallback?.toggle(isShowing)
        } else {
            mIvLock!!.visibility = VISIBLE
            if (mHideLockViewRunnable != null) {
                removeCallbacks(mHideLockViewRunnable)
                postDelayed(mHideLockViewRunnable, 7000)
            }
        }
        moreView(false)
    }

    private fun moreView(isShow: Boolean) {
        if (isShow) {
            if (mVodMoreView?.visibility == View.GONE) {
                mVodMoreView?.visibility = View.VISIBLE
                mVodMoreView?.animation =
                    AnimationUtils.loadAnimation(context, R.anim.slide_right_in)
            }
        } else {
            if (mVodMoreView?.visibility == View.VISIBLE) {
                mVodMoreView?.visibility = View.GONE
                mVodMoreView?.animation =
                    AnimationUtils.loadAnimation(context, R.anim.slide_right_exit)
            }
        }
    }

    private fun showQualityView(isShow: Boolean) {
        mVodQualityView?.showQualityView(isShow)
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
        mWaterMarkBmpY = y
        mWaterMarkBmpX = x
    }

    /**
     * 显示控件
     */
    override fun show() {
        //直播也不显示进度
        isShowing = true
        isShowControl(true)
        if (mHideLockViewRunnable != null) {
            removeCallbacks(mHideLockViewRunnable)
        }
        if (uiConfig.showLock) {
            mIvLock!!.visibility = VISIBLE
        }
        if (mPlayType == PlayerType.LIVE_SHIFT) {
            if (mLayoutBottom!!.visibility == VISIBLE) mTvBackToLive!!.visibility = VISIBLE
        }
        val pointParams: MutableList<PointSeekBar.PointParams> = ArrayList()
        if (mTXPlayKeyFrameDescInfoList != null) for (info in mTXPlayKeyFrameDescInfoList!!) {
            val progress = (info.time / mDuration * mSeekBarProgress!!.max) as Int
            pointParams.add(PointSeekBar.PointParams(progress, Color.WHITE))
        }
        mSeekBarProgress!!.setPointList(pointParams)
        if (mHideViewRunnable != null) {
            removeCallbacks(mHideViewRunnable)
            postDelayed(mHideViewRunnable, 7000)
        }
    }

    /**
     * 隐藏控件
     */
    override fun hide() {
        isShowing = false
        isShowControl(false)
        showQualityView(false)
        moreView(false)
        mTvVttText!!.visibility = GONE
        mIvLock!!.visibility = GONE
        if (mPlayType == PlayerType.LIVE_SHIFT) {
            mTvBackToLive!!.visibility = GONE
        }
    }

    private fun isShowControl(isShow: Boolean) {
        if (mPlayType == PlayerType.LIVE_SHIFT) {
            mTvBackToLive!!.isVisible = isShow
        }
        if (isShow) {
            if (mLayoutTop!!.visibility != View.VISIBLE) {
                if (uiConfig.showTop) {
                    mLayoutTop?.animation =
                        AnimationUtils.loadAnimation(context, R.anim.push_top_in)
                    mLayoutTop!!.isVisible = isShow
                }
            }
            if (mLayoutBottom!!.visibility != View.VISIBLE) {
                if (uiConfig.showBottom) {
                    mLayoutBottom?.animation =
                        AnimationUtils.loadAnimation(context, R.anim.push_bottom_in)
                    mLayoutBottom!!.isVisible = isShow
                }
            }
        } else {
            if (mLayoutTop!!.visibility == View.VISIBLE) {
                if (uiConfig.showTop && !uiConfig.keepTop) {
                    mLayoutTop?.animation =
                        AnimationUtils.loadAnimation(context, R.anim.push_top_out)
                    mLayoutTop!!.isVisible = isShow
                }

            }
            if (uiConfig.showBottom) {
                if (mLayoutBottom!!.visibility == View.VISIBLE) {
                    mLayoutBottom?.animation =
                        AnimationUtils.loadAnimation(context, R.anim.push_bottom_out)
                    mLayoutBottom!!.isVisible = isShow
                }
            }
        }
    }


    /**
     * 释放控件的内存
     */
    override fun release() {
        releaseTXImageSprite()
    }

    override fun updatePlayState(playState: PlayerState?) {
        when (playState) {
            PlayerState.PLAYING -> {
                mIvPause!!.setImageResource(R.drawable.superplayer_ic_vod_pause_normal)
            }
            PlayerState.LOADING -> {
                mIvPause!!.setImageResource(R.drawable.superplayer_ic_vod_pause_normal)
            }
            PlayerState.PAUSE -> {
                mIvPause!!.setImageResource(R.drawable.superplayer_ic_vod_play_normal)
            }
            PlayerState.END -> {
                mIvPause!!.setImageResource(R.drawable.superplayer_ic_vod_play_normal)
            }
        }
        mCurrentPlayState = playState
    }

    /**
     * 设置视频画质信息
     *
     * @param ArrayList 画质列表
     */
    override fun setVideoQualityList(ArrayList: ArrayList<VideoQuality>?) {
        mVideoQualityList = ArrayList
        mFirstShowQuality = false
        mTvQuality?.isVisible = true
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
     * 更新是屁播放进度
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
        if (mPlayType == PlayerType.LIVE || mPlayType == PlayerType.LIVE_SHIFT) {
            mLivePushDuration = if (mLivePushDuration > mProgress) mLivePushDuration else mProgress
            val leftTime = mDuration - mProgress
            mDuration =
                if (mDuration > Companion.MAX_SHIFT_TIME) Companion.MAX_SHIFT_TIME.toLong() else mDuration
            percentage = 1 - leftTime.toFloat() / mDuration.toFloat()
        }
        if (percentage in 0.0..1.0) {
            val progress = (percentage * mSeekBarProgress!!.max).roundToInt()
            if (!mIsChangingSeekBarProgress) mSeekBarProgress!!.progress = (progress)
            mTvDuration!!.text = formattedTime(mDuration)
        }
    }

    fun isDrag() = mSeekBarProgress?.mIsOnDrag == true


    override fun updatePlayType(type: PlayerType?) {
        mPlayType = type
        when (type) {
            PlayerType.VOD -> {
                mTvBackToLive!!.visibility = GONE
                mTvDuration!!.visibility = VISIBLE
            }
            PlayerType.LIVE -> {
                mTvBackToLive!!.visibility = GONE
                mTvDuration!!.visibility = GONE
                mSeekBarProgress!!.progress = (100)
            }
            PlayerType.LIVE_SHIFT -> {
                if (mLayoutBottom!!.visibility == VISIBLE) {
                    mTvBackToLive!!.visibility = VISIBLE
                }
                mTvDuration!!.visibility = GONE
            }
        }
    }

    /**
     * 更新视频播放画质
     *
     * @param videoQuality 画质
     */
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
                if (quality?.title != null && quality.title == mDefaultVideoQuality!!.title) {
                    mVodQualityView!!.setDefaultSelectedQuality(i)
                    break
                }
            }
        }
    }

    /**
     * 更新雪碧图信息
     *
     * @param info 雪碧图信息
     */
    override fun updateImageSpriteInfo(info: PlayImageSpriteInfo?) {
        if (mTXImageSprite != null) {
            releaseTXImageSprite()
        }
        // 有缩略图的时候不显示进度
        mGestureVideoProgressLayout!!.setProgressVisibility(!(info?.imageUrls != null && info.imageUrls!!.size != 0))
        if (mPlayType == PlayerType.VOD) {
            mTXImageSprite = TXImageSprite(context)
            if (info != null) {
                mTXImageSprite!!.setVTTUrlAndImageUrls(info.webVttUrl, info.imageUrls)
            } else {
                mTXImageSprite!!.setVTTUrlAndImageUrls(null, null)
            }
        }
    }

    private fun releaseTXImageSprite() {
        if (mTXImageSprite != null) {
            mTXImageSprite!!.release()
            mTXImageSprite = null
        }
    }

    /**
     * 更新关键帧信息
     *
     * @param ArrayList 关键帧信息列表
     */
    override fun updateKeyFrameDescInfo(list: ArrayList<PlayKeyFrameDescInfo>?) {
        mTXPlayKeyFrameDescInfoList = list
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mGestureDetector != null) mGestureDetector!!.onTouchEvent(event)
        if (!mLockScreen) {
            if (event?.action == MotionEvent.ACTION_UP && mVideoGestureDetector != null && mVideoGestureDetector!!.isVideoProgressModel) {
                var progress = mVideoGestureDetector!!.videoProgress
                if (progress > mSeekBarProgress!!.max) {
                    progress = mSeekBarProgress!!.max
                }
                if (progress < 0) {
                    progress = 0
                }
                mSeekBarProgress!!.progress = (progress)
                var seekTime = 0
                val percentage = progress * 1.0f / mSeekBarProgress!!.max
                seekTime = if (mPlayType == PlayerType.LIVE || mPlayType == PlayerType.LIVE_SHIFT) {
                    if (mLivePushDuration > Companion.MAX_SHIFT_TIME) {
                        (mLivePushDuration - Companion.MAX_SHIFT_TIME * (1 - percentage)) as Int
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
        }
        if (event?.action == MotionEvent.ACTION_DOWN) {
            removeCallbacks(mHideViewRunnable)
        } else if (event?.action == MotionEvent.ACTION_UP) {
            postDelayed(mHideViewRunnable, 7000)
        }
        return true
    }

    override fun updateSpeedChange(speedLevel: Float) {
        mVodMoreView?.updateSpeedChange(speedLevel)
        mIvSpeed?.let { WinSpeedHelper.showSpeedImage(playerConfig,it) }
    }

    /**
     * 设置点击事件监听
     */
    override fun onClick(view: View) {
        val i = view.id

        if (i == R.id.superplayer_iv_back || i == R.id.superplayer_tv_title) { //顶部标题栏
            if (mControllerCallback != null) {
                mControllerCallback!!.onBackPressed(PlayerMode.FULLSCREEN)
            }
        } else if (i == R.id.superplayer_iv_pause) {            //暂停\播放按钮
            togglePlayState()
        } else if (i == R.id.superplayer_iv_more) {             //更多设置按钮
            showMoreView()
        } else if (i == R.id.superplayer_tv_quality) {          //画质按钮
            showQualityView()
        } else if (i == R.id.superplayer_iv_lock) {             //锁屏按钮
            toggleLockState()
        }
//        else if (i == R.id.superplayer_ll_replay) {           //重播按钮
//            replay()
//        }
        else if (i == R.id.superplayer_tv_back_to_live) {     //返回直播按钮
            if (mControllerCallback != null) {
                mControllerCallback!!.onResumeLive()
            }
        } else if (i == R.id.superplayer_large_tv_vtt_text) {   //关键帧打点信息按钮
            seekToKeyFramePos()
        } else if (i == R.id.iv_speed) {
            showMoreView()
        } else if (i == R.id.top_share) {
            showShare()
        } else if (i == R.id.iv_tv) {
            showTVLink()
        }
    }

    private fun showTVLink() {
        mControllerCallback?.onTV()
    }

    private fun showShare() {

        mControllerCallback?.onShare()
    }

    /**
     * 显示更多设置弹窗
     */
    private fun showMoreView() {
        hide()
        moreView(true)
    }

    /**
     * 显示画质列表弹窗
     */
    private fun showQualityView() {
        if (mVideoQualityList == null || mVideoQualityList!!.size == 0) {
            return
        }
        if (mVideoQualityList!!.size < 1) {
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

    fun showLoading() {
        mPbLiveLoading?.let { toggleView(it, true) }
    }

    fun hideLoading() {
        mPbLiveLoading?.let { toggleView(it, false) }
    }

    /**
     * 切换锁屏状态
     */
    private fun toggleLockState() {
        mLockScreen = !mLockScreen
        if (mHideLockViewRunnable != null) {
            removeCallbacks(mHideLockViewRunnable)
            postDelayed(mHideLockViewRunnable, 7000)
        }
        if (mLockScreen) {
            mIvLock!!.setImageResource(R.drawable.superplayer_ic_player_lock)
            hide()
            mIvLock!!.visibility = VISIBLE
        } else {
            mIvLock!!.setImageResource(R.drawable.superplayer_ic_player_unlock)
            show()
        }
    }

    /**
     * 重播
     */
    private fun replay() {
        if (mControllerCallback != null) {
            mControllerCallback!!.onResume()
        }
    }

    /**
     * 跳转至关键帧打点处
     */
    private fun seekToKeyFramePos() {
        val time: Float =
            if (mTXPlayKeyFrameDescInfoList != null) mTXPlayKeyFrameDescInfoList!![mSelectedPos].time else 0F
        if (mControllerCallback != null) {
            mControllerCallback!!.onSeekTo(time.toInt())
            mControllerCallback!!.onResume()
        }
        mTvVttText!!.visibility = GONE
    }

    override fun onProgressChanged(seekBar: PointSeekBar, progress: Int, isFromUser: Boolean) {
        if (mGestureVideoProgressLayout != null && isFromUser) {
            mGestureVideoProgressLayout!!.show()
            val percentage = progress.toFloat() / seekBar.max
            var currentTime = mDuration * percentage
            if (mPlayType == PlayerType.LIVE || mPlayType == PlayerType.LIVE_SHIFT) {
                currentTime = if (mLivePushDuration > Companion.MAX_SHIFT_TIME) {
                    (mLivePushDuration - Companion.MAX_SHIFT_TIME * (1 - percentage))
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
            mGestureVideoProgressLayout!!.setProgress(progress)
        }
        // 加载点播缩略图
        if (isFromUser && mPlayType == PlayerType.VOD) {
            setThumbnail(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: PointSeekBar?) {
        removeCallbacks(mHideViewRunnable)
    }

    override fun onStopTrackingTouch(seekBar: PointSeekBar) {
        val curProgress = seekBar.progress
        val maxProgress = seekBar.max
        when (mPlayType) {
            PlayerType.VOD -> if (curProgress in 0..maxProgress) {
                // 关闭重播按钮
                val percentage = curProgress.toFloat() / maxProgress
                val position = (mDuration * percentage).toInt()
                if (mControllerCallback != null) {
                    mControllerCallback!!.onSeekTo(position)
//                    mControllerCallback!!.onResume()
                }
            }
            PlayerType.LIVE, PlayerType.LIVE_SHIFT -> {
                var seekTime = (mLivePushDuration * curProgress * 1.0f / maxProgress).toInt()
                if (mLivePushDuration > Companion.MAX_SHIFT_TIME) {
                    seekTime =
                        (mLivePushDuration - Companion.MAX_SHIFT_TIME * (maxProgress - curProgress) * 1.0f / maxProgress).toInt()
                }
                if (mControllerCallback != null) {
                    mControllerCallback!!.onSeekTo(seekTime)
                }
            }
        }
        postDelayed(mHideViewRunnable, 7000)
    }

    override fun onSeekBarPointClick(view: View, pos: Int) {
        if (mHideLockViewRunnable != null) {
            removeCallbacks(mHideViewRunnable)
            postDelayed(mHideViewRunnable, 7000)
        }
        if (mTXPlayKeyFrameDescInfoList != null) {
            mSelectedPos = pos
            view.post {
                val location = IntArray(2)
                view.getLocationInWindow(location)
                val viewX = location[0]
                val info = mTXPlayKeyFrameDescInfoList!![pos]
                val content = info.content
                mTvVttText!!.text = formattedTime(info.time.toLong()) + " " + content
                mTvVttText!!.visibility = VISIBLE
                adjustVttTextViewPos(viewX)
            }
        }
    }

    /**
     * 设置播放进度所对应的缩略图
     *
     * @param progress 播放进度
     */
    private fun setThumbnail(progress: Int) {
        val percentage = progress.toFloat() / mSeekBarProgress!!.max
        val seekTime = mDuration * percentage
        if (mTXImageSprite != null) {
            val bitmap = mTXImageSprite!!.getThumbnail(seekTime)
            if (bitmap != null) {
                mGestureVideoProgressLayout!!.setThumbnail(bitmap)
            }
        }
    }

    /**
     * 计算并设置关键帧打点信息文本显示的位置
     *
     * @param viewX 点击的打点view
     */
    private fun adjustVttTextViewPos(viewX: Int) {
        mTvVttText!!.post {
            val width = mTvVttText!!.width
            val marginLeft = viewX - width / 2
            val params = mTvVttText!!.layoutParams as LayoutParams
            params.leftMargin = marginLeft
            if (marginLeft < 0) {
                params.leftMargin = 0
            }
            val screenWidth = resources.displayMetrics.widthPixels
            if (marginLeft + width > screenWidth) {
                params.leftMargin = screenWidth - width
            }
            mTvVttText!!.layoutParams = params
        }
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


    override fun onQualitySelect(quality: VideoQuality) {
        if (mControllerCallback != null) {
            mControllerCallback!!.onQualityChange(quality)
        }
        showQualityView(false)
    }

    /**
     * 隐藏锁屏按钮的runnable
     */
    private class HideLockViewRunnable(controller: FullScreenPlayer?) : Runnable {
        private val mWefControllerFullScreen: WeakReference<FullScreenPlayer?>?
        override fun run() {
            mWefControllerFullScreen?.get()?.mIvLock?.isVisible = false
        }

        init {
            mWefControllerFullScreen = WeakReference(controller)
        }
    }

    fun onDestroyCallBack() {
        mVodMoreView?.onDestroyTimeCallBack()
    }

    fun hideTV(isShow: Boolean = false) {
        TransitionManager.beginDelayedTransition(this)
        mIvTV?.isVisible = isShow
    }

    fun hideQualities() {
        TransitionManager.beginDelayedTransition(this)
        mTvQuality?.isVisible = false
    }

    override fun setPlayConfig(config: PlayerConfig) {

    }

    override fun setUIConfig(uiConfig: UIConfig) {
        this.uiConfig = uiConfig
        WinSpeedHelper.showSpeedImage(playerConfig, mIvSpeed!!)
        mIvTV?.isVisible = uiConfig.showTV
        mIvSpeed?.isVisible = uiConfig.showSpeed
        mTopShare?.isVisible = uiConfig.showShare
        mIvMore?.isVisible = uiConfig.showMore
        mIvLock?.isVisible = uiConfig.showLock
        mLayoutTop?.isVisible = uiConfig.showTop || uiConfig.keepTop
        mLayoutBottom?.isVisible = uiConfig.showBottom
    }

}