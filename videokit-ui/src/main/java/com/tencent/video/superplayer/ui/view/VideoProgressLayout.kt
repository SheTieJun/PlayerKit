package com.tencent.video.superplayer.ui.view

import android.content.*
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import com.tencent.liteav.superplayer.*

/**
 * 滑动手势控制播放进度时显示的进度提示view
 */
class VideoProgressLayout : FrameLayout {
    private var mIvThumbnail : ImageView? = null// 视频缩略图
    private var mTvTime   : TextView? = null// 视频进度文本
    private var mProgressBar  : ProgressBar? = null// 进度条
    private var mHideRunnable  : HideRunnable? = null// 隐藏自身的线程
    private var duration = 1000 // 自身消失的延迟事件ms

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.superplayer_video_progress_layout, this)
        mIvThumbnail = findViewById<View>(R.id.superplayer_iv_progress_thumbnail) as ImageView
        mProgressBar = findViewById<View>(R.id.superplayer_pb_progress_bar) as ProgressBar
        mTvTime = findViewById<View>(R.id.superplayer_tv_progress_time) as TextView
        isVisible = false
        mHideRunnable = HideRunnable()
    }

    /**
     * 显示view
     */
    fun show() {
        isVisible = true
        removeCallbacks(mHideRunnable)
        postDelayed(mHideRunnable, duration.toLong())
    }

    /**
     * 设置视频进度事件文本
     *
     * @param text
     */
    fun setTimeText(text: String?) {
        mTvTime!!.text = text
    }

    /**
     * 设置progressbar的进度值
     *
     * @param progress
     */
    fun setProgress(progress: Int) {
        mProgressBar!!.progress = progress
    }

    /**
     * 设置view消失延迟的时间
     *
     * @param duration
     */
    fun setDuration(duration: Int) {
        this.duration = duration
    }

    /**
     * 设置缩略图图片
     *
     * @param bitmap
     */
    fun setThumbnail(bitmap: Bitmap?) {
        mIvThumbnail!!.visibility = VISIBLE
        mIvThumbnail!!.setImageBitmap(bitmap)
    }

    /**
     * 设置progressbar的可见性
     *
     * @param enable
     */
    fun setProgressVisibility(enable: Boolean) {
        mProgressBar!!.isVisible = enable
    }

    /**
     * 隐藏view的线程
     */
    private inner class HideRunnable : Runnable {
        override fun run() {
            mIvThumbnail!!.setImageBitmap(null)
            mIvThumbnail!!.visibility = GONE
            this@VideoProgressLayout.visibility = GONE
        }
    }
}