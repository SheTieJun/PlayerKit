package com.shetj.sdk.video.view

import android.content.*
import android.util.AttributeSet
import android.view.*
import android.widget.*
import com.shetj.sdk.video.ui.databinding.SuperplayerVideoVolumeBrightnessProgressLayoutBinding

/**
 * 滑动手势设置音量、亮度时显示的提示view
 */
class VolumeBrightnessProgressLayout : FrameLayout {
    private lateinit var mViewBinding: SuperplayerVideoVolumeBrightnessProgressLayoutBinding
    private var mHideRunnable: HideRunnable? = null
    private var mDuration = 1000 // view消失延迟时间(秒)

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        mViewBinding =SuperplayerVideoVolumeBrightnessProgressLayoutBinding.inflate( LayoutInflater.from(context),this,true)
        mHideRunnable = HideRunnable()
        visibility = GONE
    }

    /**
     * 显示
     */
    fun show() {
        visibility = VISIBLE
        removeCallbacks(mHideRunnable)
        postDelayed(mHideRunnable, mDuration.toLong())
    }

    /**
     * 设置progressBar的进度值
     *
     * @param progress
     */
    fun setProgress(progress: Int) {
        mViewBinding.superplayerPbProgressBar.progress = progress
    }

    /**
     * 设置view消失的延迟时间
     *
     * @param duration
     */
    fun setDuration(duration: Int) {
        mDuration = duration
    }

    /**
     * 设置显示的图片，亮度提示图片或者音量提示图片
     *
     * @param resource
     */
    fun setImageResource(resource: Int) {
        mViewBinding.superplayerIvCenter.setImageResource(resource)
    }

    /**
     * 隐藏view的runnable
     */
    private inner class HideRunnable : Runnable {
        override fun run() {
            this@VolumeBrightnessProgressLayout.visibility = GONE
        }
    }
}