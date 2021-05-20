package com.tencent.video.superplayer.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.tencent.video.superplayer.SuperPlayerDef.PlayerState
import com.tencent.video.superplayer.SuperPlayerDef.PlayerType
import com.tencent.video.superplayer.model.entity.PlayImageSpriteInfo
import com.tencent.video.superplayer.model.entity.PlayKeyFrameDescInfo
import com.tencent.video.superplayer.model.entity.VideoQuality

/**
 * 播放器公共逻辑
 */
abstract class AbsPlayer : RelativeLayout, Player {
    protected var mControllerCallback // 播放控制回调
            : Player.Callback? = null
    protected var mHideViewRunnable = Runnable { hide() }

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    override fun setCallback(callback: Player.Callback?) {
        mControllerCallback = callback
    }

    override fun setWatermark(bmp: Bitmap?, x: Float, y: Float) {}
    override fun show() {}
    override fun hide() {}
    override fun release() {}
    override fun updatePlayState(playState: PlayerState?) {}
    override fun setVideoQualityList(list: ArrayList<VideoQuality>?) {}
    override fun updateTitle(title: String?) {}
    override fun updateVideoProgress(current: Long, duration: Long) {}
    override fun updatePlayType(type: PlayerType?) {}
    override fun setBackground(bitmap: Bitmap?) {}
    override fun showBackground() {}
    override fun hideBackground() {}
    override fun updateVideoQuality(videoQuality: VideoQuality?) {}
    override fun updateImageSpriteInfo(info: PlayImageSpriteInfo?) {}
    override fun updateKeyFrameDescInfo(list: ArrayList<PlayKeyFrameDescInfo>?) {}
    override fun updateSpeedChange(speedLevel: Float) {}
    /**
     * 设置控件的可见性
     *
     * @param view      目标控件
     * @param isVisible 显示：true 隐藏：false
     */
    protected fun toggleView(view: View, isVisible: Boolean) {
        view.isVisible = isVisible
    }

    /**
     * 将秒数转换为hh:mm:ss的格式
     *
     * @param second
     * @return
     */
    protected fun formattedTime(second: Long): String {
        val formatTime: String
        val h: Long = second / 3600
        val m: Long = second % 3600 / 60
        val s: Long = second % 3600 % 60
        formatTime = if (h == 0L) {
            asTwoDigit(m) + ":" + asTwoDigit(s)
        } else {
            asTwoDigit(h) + ":" + asTwoDigit(m) + ":" + asTwoDigit(s)
        }
        return formatTime
    }

    protected fun asTwoDigit(digit: Long): String {
        var value = ""
        if (digit < 10) {
            value = "0"
        }
        value += digit.toString()
        return value
    }

    companion object {
        const val MAX_SHIFT_TIME = 7200 // demo演示直播时移是MAX_SHIFT_TIMEs，即2小时
    }
}