package com.shetj.sdk.video.viedoview.ui

import me.shetj.sdk.video.PlayerDef.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.view.isVisible
import com.shetj.sdk.video.casehelper.KeyListListener
import com.shetj.sdk.video.casehelper.PlayKeyListHelper
import com.shetj.sdk.video.casehelper.WinSpeedHelper
import com.shetj.sdk.video.casehelper.onNext
import com.shetj.sdk.video.ui.R
import com.shetj.sdk.video.ui.databinding.SuperplayerVodPlayerFullscreenBinding
import com.shetj.sdk.video.viedoview.AbBaseUIPlayer
import com.shetj.sdk.video.kit.VideoGestureDetector
import com.shetj.sdk.video.ui.view.PointSeekBar
import com.shetj.sdk.video.ui.view.VodMoreView
import com.shetj.sdk.video.ui.view.VodQualityView
import me.shetj.sdk.video.base.BaseKitAdapter
import me.shetj.sdk.video.base.GlobalConfig
import me.shetj.sdk.video.base.PlayerConfig
import me.shetj.sdk.video.base.UIConfig
import me.shetj.sdk.video.model.PlayImageSpriteInfo
import me.shetj.sdk.video.model.PlayKeyFrameDescInfo
import me.shetj.sdk.video.model.VideoQuality
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
 * 5、切换画质监听[.onQualitySelect]
 *
 * 6、倍速播放监听[.onSpeedChange]
 *
 * 7、镜像播放监听[.onMirrorChange]
 *
 * 8、硬件加速监听[.onHWAcceleration]
 *
 */
open class FullScreenPlayer :  AbBaseUIPlayer, View.OnClickListener, VodMoreView.Callback,
    VodQualityView.Callback, PointSeekBar.OnSeekBarChangeListener, PointSeekBar.PointDrag,
    PointSeekBar.OnSeekBarPointClickListener, KeyListListener {

    private lateinit var mViewBinding: SuperplayerVodPlayerFullscreenBinding
    // 隐藏锁屏按钮子线程
    private var mHideLockViewRunnable: HideLockViewRunnable? = null
    // 手势检测监听器
    private var mGestureDetector: GestureDetector? = null
    // 手势控制工具
    private var mVideoGestureDetector: VideoGestureDetector? = null
    private var isShowing = false

    // 进度条是否正在拖动，避免SeekBar由于视频播放的update而跳动
    private var mIsChangingSeekBarProgress = false
    private var mPlayType: PlayerType? = null
    private var mCurrentPlayState: PlayerState? = PlayerState.END // 当前播放状态
    private var mDuration  : Long = 0// 视频总时长
    private var mLivePushDuration    : Long = 0// 直播推流总时长
    private var mProgress        : Long = 0// 当前播放进度
    private var mWaterMarkBmp : Bitmap? = null// 水印图
    private var mWaterMarkBmpX   = 0f// 水印x坐标
    private var mWaterMarkBmpY = 0f // 水印y坐标

    private var mLockScreen = false// 是否锁屏
    private var mTXPlayKeyFrameDescInfoList: ArrayList<PlayKeyFrameDescInfo>? = null // 关键帧信息
    private var mSelectedPos = -1 // 点击的关键帧时间点
    private var mDefaultVideoQuality: VideoQuality? = null // 默认画质
    private var mVideoQualityList: ArrayList<VideoQuality>? = null// 画质列表
    private var mFirstShowQuality = false // 是都是首次显示画质信息
    private val keyListHelper: PlayKeyListHelper by lazy { PlayKeyListHelper(this) }

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
                    if (mVideoGestureDetector != null) {
                        mVideoGestureDetector!!.check(
                            mViewBinding.superplayerGestureProgress.height,
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
                        mVideoGestureDetector!!.reset(
                            width,
                            mViewBinding.superplayerSeekbarProgress.progress
                        )
                    }
                    return true
                }
            })
        mGestureDetector!!.setIsLongpressEnabled(false)
        mVideoGestureDetector = VideoGestureDetector(getContext())
        mVideoGestureDetector!!.setVideoGestureListener(object :
            VideoGestureDetector.VideoGestureListener {
            override fun onBrightnessGesture(newBrightness: Float) {
                mViewBinding.superplayerGestureProgress.setProgress((newBrightness * 100).toInt())
                mViewBinding.superplayerGestureProgress.setImageResource(R.drawable.superplayer_ic_light_max)
                mViewBinding.superplayerGestureProgress.show()
            }

            override fun onVolumeGesture(volumeProgress: Float) {
                mViewBinding.superplayerGestureProgress.setImageResource(R.drawable.superplayer_ic_volume_max)
                mViewBinding.superplayerGestureProgress.setProgress(volumeProgress.toInt())
                mViewBinding.superplayerGestureProgress.show()
            }

            override fun onSeekGesture(seekProgress: Int) {
                if (isLive()) return
                var progress = seekProgress
                mIsChangingSeekBarProgress = true
                if (progress > mViewBinding.superplayerSeekbarProgress.max) {
                    progress = mViewBinding.superplayerSeekbarProgress.max
                }
                if (progress < 0) {
                    progress = 0
                }
                mViewBinding.superplayerVideoProgressLayout.setProgress(progress)
                mViewBinding.superplayerVideoProgressLayout.show()
                val percentage =
                    progress.toFloat() / mViewBinding.superplayerSeekbarProgress.max
                var currentTime = mDuration * percentage
                if (mPlayType == PlayerType.LIVE || mPlayType == PlayerType.LIVE_SHIFT) {
                    (if (mLivePushDuration > MAX_SHIFT_TIME) {
                        (mLivePushDuration - MAX_SHIFT_TIME * (1 - percentage))
                    } else {
                        mLivePushDuration * percentage
                    }).also { currentTime = it }
                    mViewBinding.superplayerVideoProgressLayout.setTimeText(
                        formattedTime(
                            currentTime.toLong()
                        )
                    )
                } else {
                    mViewBinding.superplayerVideoProgressLayout.setTimeText(
                        formattedTime(currentTime.toLong()) + " / " + formattedTime(
                            mDuration
                        )
                    )
                    updateVideoProgress(currentTime.toLong(), mDuration)
                }
                setThumbnail(progress)
                mViewBinding.superplayerSeekbarProgress.progress = (progress)
            }
        })
    }

    fun isLive() = (mPlayType == PlayerType.LIVE || mPlayType == PlayerType.LIVE_SHIFT)

    /**
     * 初始化view
     */
    private fun initView(context: Context) {
        mViewBinding =
            SuperplayerVodPlayerFullscreenBinding.inflate(LayoutInflater.from(context), this, true)
        mHideLockViewRunnable = HideLockViewRunnable(this)
        mViewBinding.apply {
            superplayerRlTop.setOnClickListener(this@FullScreenPlayer)
            superplayerLlBottom.setOnClickListener(this@FullScreenPlayer)
            superplayerSeekbarProgress.apply {
                setOnPointClickListener(this@FullScreenPlayer)
                setOnSeekBarChangeListener(this@FullScreenPlayer)
            }.progress = 0
            superplayerVodQuality.setCallback(true, this@FullScreenPlayer)
            superplayerVodMore.setCallback(this@FullScreenPlayer)
            superplayerIvBack.setOnClickListener(this@FullScreenPlayer)
            superplayerIvMore.setOnClickListener(this@FullScreenPlayer)
            superplayerIvPause.setOnClickListener(this@FullScreenPlayer)
            superplayerTvQuality.setOnClickListener(this@FullScreenPlayer)
            superplayerTvBackToLive.setOnClickListener(this@FullScreenPlayer)
            superplayerLargeTvVttText.setOnClickListener(this@FullScreenPlayer)
            if (mDefaultVideoQuality != null) {
                superplayerTvQuality.text = mDefaultVideoQuality!!.title
            }
            superplayerIvLock.setOnClickListener(this@FullScreenPlayer)
            ivSpeed.setOnClickListener(this@FullScreenPlayer)
            topShare.setOnClickListener(this@FullScreenPlayer)
            ivTv.setOnClickListener(this@FullScreenPlayer)
        }
    }

    /**
     * 切换播放状态
     *
     * 双击和点击播放/暂停按钮会触发此方法
     */
    private fun togglePlayState() {
        when (mCurrentPlayState) {
            PlayerState.PAUSE, PlayerState.END -> mControllerCallback?.onResume()
            PlayerState.PLAYING, PlayerState.LOADING -> mControllerCallback?.onPause()
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
            mControllerCallback?.onControlViewToggle(isShowing)
        } else {
            mViewBinding.superplayerIvLock.visibility = VISIBLE
            if (mHideLockViewRunnable != null) {
                delayHide()
            }
        }
        moreView(false)
    }

    private fun moreView(isShow: Boolean) {
        if (isShow) {
            if (mViewBinding.superplayerVodMore.visibility == View.GONE) {
                mViewBinding.superplayerVodMore.visibility = View.VISIBLE
                mViewBinding.superplayerVodMore.animation =
                    AnimationUtils.loadAnimation(context, R.anim.slide_right_in)
            }
        } else {
            if (mViewBinding.superplayerVodMore.visibility == View.VISIBLE) {
                mViewBinding.superplayerVodMore.visibility = View.GONE
                mViewBinding.superplayerVodMore.animation =
                    AnimationUtils.loadAnimation(context, R.anim.slide_right_exit)
            }
        }
    }

    private fun showQualityView(isShow: Boolean) {
        mViewBinding.superplayerVodQuality.showQualityView(isShow)
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
            mViewBinding.superplayerIvLock.visibility = VISIBLE
        }
        if (mPlayType == PlayerType.LIVE_SHIFT) {
            if (mViewBinding.superplayerLlBottom.visibility == VISIBLE) {
                mViewBinding.superplayerTvBackToLive.visibility = VISIBLE
            }
        }
        val pointParams: MutableList<PointSeekBar.PointParams> = ArrayList()
        if (mTXPlayKeyFrameDescInfoList != null) for (info in mTXPlayKeyFrameDescInfoList!!) {
            val progress =
                (info.time / mDuration * mViewBinding.superplayerSeekbarProgress.max).toInt()
            pointParams.add(PointSeekBar.PointParams(progress, Color.WHITE))
        }
        mViewBinding.superplayerSeekbarProgress.setPointList(pointParams)
        removeCallbacks(mHideViewRunnable)
        postDelayed(mHideViewRunnable, 7000)
    }

    /**
     * 隐藏控件
     */
    override fun hide() {
        isShowing = false
        isShowControl(false)
        showQualityView(false)
        moreView(false)
        mViewBinding.superplayerLargeTvVttText.visibility = GONE
        mViewBinding.superplayerIvLock.visibility = GONE
        if (mPlayType == PlayerType.LIVE_SHIFT) {
            mViewBinding.superplayerTvBackToLive.visibility = GONE
        }
    }

    private fun isShowControl(isShow: Boolean) {
        if (mPlayType == PlayerType.LIVE_SHIFT) {
            mViewBinding.superplayerTvBackToLive.isVisible = isShow
        }
        if (isShow) {
            if (mViewBinding.superplayerRlTop.visibility != View.VISIBLE) {
                if (uiConfig.showTop) {
                    mViewBinding.superplayerRlTop.animation =
                        AnimationUtils.loadAnimation(context, R.anim.push_top_in)
                    mViewBinding.superplayerRlTop.isVisible = isShow
                }
            }
            if (mViewBinding.superplayerLlBottom.visibility != View.VISIBLE) {
                if (uiConfig.showBottom) {
                    mViewBinding.superplayerLlBottom.animation =
                        AnimationUtils.loadAnimation(context, R.anim.push_bottom_in)
                    mViewBinding.superplayerLlBottom.isVisible = isShow
                }
            }
        } else {
            if (mViewBinding.superplayerRlTop.visibility == View.VISIBLE) {
                if (uiConfig.showTop && !uiConfig.keepTop) {
                    mViewBinding.superplayerRlTop.animation =
                        AnimationUtils.loadAnimation(context, R.anim.push_top_out)
                    mViewBinding.superplayerRlTop.isVisible = isShow
                }

            }
            if (mViewBinding.superplayerLlBottom.visibility == View.VISIBLE) {
                if (uiConfig.showBottom && !uiConfig.keepTop) {
                    mViewBinding.superplayerLlBottom.animation =
                        AnimationUtils.loadAnimation(context, R.anim.push_bottom_out)
                    mViewBinding.superplayerLlBottom.isVisible = isShow
                }
            }
        }
    }


    /**
     * 释放控件的内存
     */
    override fun release() {
    }

    override fun updatePlayState(playState: PlayerState?) {
        when (playState) {
            PlayerState.PLAYING -> {
                mViewBinding.superplayerIvPause.setImageResource(R.drawable.superplayer_ic_vod_pause_normal)
            }
            PlayerState.LOADING -> {
                mViewBinding.superplayerIvPause.setImageResource(R.drawable.superplayer_ic_vod_pause_normal)
            }
            PlayerState.PAUSE -> {
                mViewBinding.superplayerIvPause.setImageResource(R.drawable.superplayer_ic_vod_play_normal)
            }
            PlayerState.END -> {
                mViewBinding.superplayerIvPause.setImageResource(R.drawable.superplayer_ic_vod_play_normal)
            }
        }
        mCurrentPlayState = playState
    }

    /**
     * 设置视频画质信息
     *
     * @param list 画质列表
     */
    override fun setVideoQualityList(list: ArrayList<VideoQuality>?) {
        if (list?.isEmpty() == true) {
            mViewBinding.superplayerTvQuality.isVisible = false
        } else {
            mVideoQualityList = list
            mFirstShowQuality = false
            mViewBinding.superplayerTvQuality.isVisible = true
        }
    }

    /**
     * 更新视频名称
     *
     * @param title 视频名称
     */
    override fun updateTitle(title: String?) {
        mViewBinding.superplayerTvTitle.text = title
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
        mViewBinding.superplayerTvCurrent.text = formattedTime(mProgress)
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
            val progress = (percentage * mViewBinding.superplayerSeekbarProgress.max).roundToInt()
            if (!mIsChangingSeekBarProgress) mViewBinding.superplayerSeekbarProgress.progress =
                (progress)
            mViewBinding.superplayerTvDuration.text = formattedTime(mDuration)
        }
    }

    override fun isDrag() = mViewBinding.superplayerSeekbarProgress.mIsOnDrag


    override fun updatePlayType(type: PlayerType?) {
        mPlayType = type
        when (type) {
            PlayerType.VOD -> {
                mViewBinding.superplayerTvBackToLive.visibility = GONE
                mViewBinding.superplayerTvDuration.visibility = VISIBLE
            }
            PlayerType.LIVE -> {
                mViewBinding.superplayerTvBackToLive.visibility = GONE
                mViewBinding.superplayerTvDuration.visibility = GONE
                mViewBinding.superplayerSeekbarProgress.progress = (100)
            }
            PlayerType.LIVE_SHIFT -> {
                if (mViewBinding.superplayerLlBottom.visibility == VISIBLE) {
                    mViewBinding.superplayerTvBackToLive.visibility = VISIBLE
                }
                mViewBinding.superplayerTvDuration.visibility = GONE
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
            mViewBinding.superplayerTvQuality.text = ""
            return
        }
        mDefaultVideoQuality = videoQuality
        mViewBinding.superplayerTvQuality.text = videoQuality.title
        if (mVideoQualityList != null && mVideoQualityList!!.isNotEmpty()) {
            for (i in mVideoQualityList!!.indices) {
                val quality = mVideoQualityList!![i]
                if (quality.title != null && quality.title == mDefaultVideoQuality!!.title) {
                    mViewBinding.superplayerVodQuality.setDefaultSelectedQuality(i)
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

    }

    /**
     * 更新关键帧信息
     *
     * @param list 关键帧信息列表
     */
    override fun updateKeyFrameDescInfo(list: ArrayList<PlayKeyFrameDescInfo>?) {
        mTXPlayKeyFrameDescInfoList = list
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mGestureDetector != null) mGestureDetector!!.onTouchEvent(event)
        if (!mLockScreen) {
            if (event?.action == MotionEvent.ACTION_UP && mVideoGestureDetector != null && mVideoGestureDetector!!.isVideoProgressModel) {
                var progress = mVideoGestureDetector!!.videoProgress
                if (progress > mViewBinding.superplayerSeekbarProgress.max) {
                    progress = mViewBinding.superplayerSeekbarProgress.max
                }
                if (progress < 0) {
                    progress = 0
                }
                mViewBinding.superplayerSeekbarProgress.progress = (progress)
                var seekTime: Int
                val percentage = progress * 1.0f / mViewBinding.superplayerSeekbarProgress.max
                seekTime = if (mPlayType == PlayerType.LIVE || mPlayType == PlayerType.LIVE_SHIFT) {
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
        }
        if (event?.action == MotionEvent.ACTION_DOWN) {
            removeCallbacks(mHideViewRunnable)
        } else if (event?.action == MotionEvent.ACTION_UP) {
            postDelayed(mHideViewRunnable, 7000)
        }
        return true
    }

    override val playerMode: PlayerMode
        get() = PlayerMode.FULLSCREEN

    override fun updateSpeedChange(speedLevel: Float) {
        mViewBinding.superplayerVodMore.updateSpeedChange(speedLevel)
        mViewBinding.ivSpeed.let { WinSpeedHelper.showSpeedImage(GlobalConfig.speed, it) }
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
        } else if (i == R.id.superplayer_tv_back_to_live) {     //返回直播按钮
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
        mControllerCallback?.onShotScreen()
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
                    mViewBinding.superplayerVodQuality.setDefaultSelectedQuality(i)
                    break
                }
            }
            mFirstShowQuality = true
        }
        mViewBinding.superplayerVodQuality.setVideoQualityList(mVideoQualityList)
    }

    override fun showLoading() {
        mViewBinding.superplayerPbLive.let { toggleView(it, true) }
    }

    override fun hideLoading() {
        mViewBinding.superplayerPbLive.let { toggleView(it, false) }
    }

    /**
     * 切换锁屏状态
     */
    private fun toggleLockState() {
        mLockScreen = !mLockScreen
        if (mHideLockViewRunnable != null) {
            delayHide()
        }
        if (mLockScreen) {
            mViewBinding.superplayerIvLock.setImageResource(R.drawable.superplayer_ic_player_lock)
            hide()
            mViewBinding.superplayerIvLock.visibility = VISIBLE
        } else {
            mViewBinding.superplayerIvLock.setImageResource(R.drawable.superplayer_ic_player_unlock)
            show()
        }
    }

    private fun delayHide() {
        removeCallbacks(mHideLockViewRunnable)
        postDelayed(mHideLockViewRunnable, 7000)
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
        mViewBinding.superplayerLargeTvVttText.visibility = GONE
    }

    override fun onProgressChanged(seekBar: PointSeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            mViewBinding.superplayerVideoProgressLayout.show()
            val percentage = progress.toFloat() / seekBar.max
            var currentTime = mDuration * percentage
            if (mPlayType == PlayerType.LIVE || mPlayType == PlayerType.LIVE_SHIFT) {
                currentTime = if (mLivePushDuration > Companion.MAX_SHIFT_TIME) {
                    (mLivePushDuration - Companion.MAX_SHIFT_TIME * (1 - percentage))
                } else {
                    mLivePushDuration * percentage
                }
                mViewBinding.superplayerVideoProgressLayout.setTimeText(formattedTime(currentTime.toLong()))
            } else {
                mViewBinding.superplayerVideoProgressLayout.setTimeText(
                    formattedTime(currentTime.toLong()) + " / " + formattedTime(
                        mDuration
                    )
                )
                updateVideoProgress(currentTime.toLong(), mDuration)
            }
            mViewBinding.superplayerVideoProgressLayout.setProgress(progress)
        }
        // 加载点播缩略图
        if (fromUser && mPlayType == PlayerType.VOD) {
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

    @SuppressLint("SetTextI18n")
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
                mViewBinding.superplayerLargeTvVttText.text =
                    formattedTime(info.time.toLong()) + " " + content
                mViewBinding.superplayerLargeTvVttText.visibility = VISIBLE
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
        val percentage = progress.toFloat() / mViewBinding.superplayerSeekbarProgress.max
        val seekTime = mDuration * percentage
    }

    /**
     * 计算并设置关键帧打点信息文本显示的位置
     *
     * @param viewX 点击的打点view
     */
    private fun adjustVttTextViewPos(viewX: Int) {
        mViewBinding.superplayerLargeTvVttText.post {
            val width = mViewBinding.superplayerLargeTvVttText.width
            val marginLeft = viewX - width / 2
            val params = mViewBinding.superplayerLargeTvVttText.layoutParams as LayoutParams
            params.leftMargin = marginLeft
            if (marginLeft < 0) {
                params.leftMargin = 0
            }
            val screenWidth = resources.displayMetrics.widthPixels
            if (marginLeft + width > screenWidth) {
                params.leftMargin = screenWidth - width
            }
            mViewBinding.superplayerLargeTvVttText.layoutParams = params
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
        private val mWefControllerFullScreen: WeakReference<FullScreenPlayer>?
        override fun run() {
            mWefControllerFullScreen?.get()?.mViewBinding?.superplayerIvLock?.isVisible = false
        }

        init {
            mWefControllerFullScreen = WeakReference(controller)
        }
    }

    private fun onDestroyCallBack() {
        mViewBinding.superplayerVodMore.onDestroyTimeCallBack()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeCallbacks(mHideViewRunnable)
        onDestroyCallBack()
    }


    override fun updatePlayConfig(config: PlayerConfig) {
        super.updatePlayConfig(config)
        this.playerConfig = config
        WinSpeedHelper.showSpeedImage(GlobalConfig.speed, mViewBinding.ivSpeed)
        mViewBinding.superplayerVodMore.updatePlayConfig(config)
    }

    override fun updateUIConfig(uiConfig: UIConfig) {
        super.updateUIConfig(uiConfig)
        mViewBinding.apply {
            superplayerVodMore.updateUIConfig(uiConfig)
            ivTv.isVisible = uiConfig.showTV
            ivSpeed.isVisible = uiConfig.showSpeed
            topShare.isVisible = uiConfig.showShare
            superplayerIvMore.isVisible = uiConfig.showMore
            mViewBinding.superplayerIvLock.isVisible = uiConfig.showLock
            mViewBinding.superplayerRlTop.isVisible = uiConfig.showTop || uiConfig.keepTop
            mViewBinding.superplayerLlBottom.isVisible = uiConfig.showBottom || uiConfig.keepBottom
            isShowing = uiConfig.showTop || uiConfig.showBottom
            delayHide()
        }
    }

    override fun nextOneKey() {
        keyListHelper.nextOneKey()
    }

    override fun updateListPosition(position: Int) {
        keyListHelper.updateListPosition(position)
    }

    override fun setKeyList(
        name: String?,
        adapter: BaseKitAdapter<*>?,
        position: Int,
        onNext: onNext?
    ) {
        keyListHelper.setKeyList(name, adapter, position, onNext)
    }

}