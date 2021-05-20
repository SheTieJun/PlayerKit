package com.tencent.video.superplayer.model.utils

import android.app.Activity
import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager

/**
 * 手势控制视频播放进度、调节亮度音量的工具
 */
class VideoGestureDetector(context: Context) {
    private var mScrollMode = NONE // 手势类型
    private var mVideoGestureListener // 回调
            : VideoGestureListener? = null
    private var mVideoWidth // 视频宽度px
            = 0

    // 亮度相关
    private var mBrightness = 1f // 当前亮度(0.0~1.0)
    private var mWindow // 当前window
            : Window? = null
    private var mLayoutParams // 用于获取和设置屏幕亮度
            : WindowManager.LayoutParams? = null
    private val mResolver // 用于获取当前屏幕亮度
            : ContentResolver?

    // 音量相关
    // 音频管理器，用于设置音量
    private val mAudioManager: AudioManager
    private var mMaxVolume = 0 // 最大音量值
    private var mOldVolume = 0 // 记录调节音量之前的旧音量值

    /**
     * 获取滑动后对应的视频进度
     *
     * @return
     */
    // 视频进度相关
    var videoProgress // 记录滑动后的进度，在回调中抛出
            = 0
        private set
    private var mDownProgress // 滑动开始时的视频播放进度
            = 0

    /**
     * 手势临界值，当两滑动事件坐标的水平差值>20时判定为[.VIDEO_PROGRESS], 否则判定为[.VOLUME]或者[.BRIGHTNESS]
     */
    private val offsetX = 20

    //手势灵敏度 0.0~1.0
    private val mSensitivity = 0.3f // 调节音量、亮度的灵敏度

    /**
     * 设置回调
     *
     * @param videoGestureListener
     */
    fun setVideoGestureListener(videoGestureListener: VideoGestureListener?) {
        mVideoGestureListener = videoGestureListener
    }

    /**
     * 重置数据以开始新的一次滑动
     *
     * @param videoWidth   视频宽度px
     * @param downProgress 手势按下时视频的播放进度(秒)
     */
    fun reset(videoWidth: Int, downProgress: Int) {
        videoProgress = 0
        mVideoWidth = videoWidth
        mScrollMode = NONE
        mOldVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        mBrightness = mLayoutParams?.screenBrightness?:1.0f
        if (mBrightness == -1f) {
            //一开始是默认亮度的时候，获取系统亮度，计算比例值
            mBrightness = brightness / 255.0f
        }
        mDownProgress = downProgress
    }

    /**
     * 获取当前是否是视频进度滑动手势
     *
     * @return
     */
    val isVideoProgressModel: Boolean
        get() = mScrollMode == VIDEO_PROGRESS

    /**
     * 滑动手势操控类别判定
     *
     * @param height    滑动事件的高度px
     * @param downEvent 按下事件
     * @param moveEvent 滑动事件
     * @param distanceX 滑动水平距离
     * @param distanceY 滑动竖直距离
     */
    fun check(
        height: Int,
        downEvent: MotionEvent,
        moveEvent: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ) {
        when (mScrollMode) {
            NONE ->                 //offset是让快进快退不要那么敏感的值
                mScrollMode = if (Math.abs(downEvent.x - moveEvent.x) > offsetX) {
                    VIDEO_PROGRESS
                } else {
                    val halfVideoWidth = mVideoWidth / 2
                    if (downEvent.x < halfVideoWidth) {
                        BRIGHTNESS
                    } else {
                        VOLUME
                    }
                }
            VOLUME -> {
                val value = height / mMaxVolume
                val newVolume =
                    ((downEvent.y - moveEvent.y) / value * mSensitivity + mOldVolume).toInt()
                mAudioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    newVolume,
                    AudioManager.FLAG_PLAY_SOUND
                )
                val volumeProgress = newVolume / java.lang.Float.valueOf(mMaxVolume.toFloat()) * 100
                if (mVideoGestureListener != null) {
                    mVideoGestureListener!!.onVolumeGesture(volumeProgress)
                }
            }
            BRIGHTNESS -> {
                var newBrightness: Float =
                    if (height == 0) 0f else (downEvent.y - moveEvent.y) / height * mSensitivity
                newBrightness += mBrightness
                if (newBrightness < 0) {
                    newBrightness = 0f
                } else if (newBrightness > 1) {
                    newBrightness = 1f
                }
                mLayoutParams?.screenBrightness = newBrightness
                mWindow?.attributes = mLayoutParams
                mVideoGestureListener?.onBrightnessGesture(newBrightness)
            }
            VIDEO_PROGRESS -> {
                val dis = moveEvent.x - downEvent.x
                val percent = dis / mVideoWidth
                videoProgress = (mDownProgress + percent * 100).toInt()
                if (mVideoGestureListener != null) {
                    mVideoGestureListener!!.onSeekGesture(videoProgress)
                }
            }
        }
    }

    /**
     * 获取当前亮度
     *
     * @return
     */
    private val brightness: Int
        private get() = if (mResolver != null) {
            Settings.System.getInt(
                mResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                255
            )
        } else {
            255
        }

    /**
     * 回调
     */
    interface VideoGestureListener {
        /**
         * 亮度调节回调
         *
         * @param newBrightness 滑动后的新亮度值
         */
        fun onBrightnessGesture(newBrightness: Float)

        /**
         * 音量调节回调
         *
         * @param volumeProgress 滑动后的新音量值
         */
        fun onVolumeGesture(volumeProgress: Float)

        /**
         * 播放进度调节回调
         *
         * @param seekProgress 滑动后的新视频进度
         */
        fun onSeekGesture(seekProgress: Int)
    }

    companion object {
        // 手势类型
        private const val NONE = 0 // 无效果
        private const val VOLUME = 1 // 音量
        private const val BRIGHTNESS = 2 // 亮度
        private const val VIDEO_PROGRESS = 3 // 播放进度
    }

    init {
        mAudioManager = context.getSystemService(Service.AUDIO_SERVICE) as AudioManager
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        (context as? Activity)?.apply {
            mWindow = window
            mLayoutParams = mWindow!!.attributes
            mBrightness = mLayoutParams!!.screenBrightness
        }
        mResolver = context.contentResolver
    }
}