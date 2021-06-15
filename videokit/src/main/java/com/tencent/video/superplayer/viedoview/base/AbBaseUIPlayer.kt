package com.tencent.video.superplayer.viedoview.base

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.tencent.video.superplayer.base.BaseKitAdapter
import com.tencent.video.superplayer.viedoview.base.SuperPlayerDef.PlayerState
import com.tencent.video.superplayer.viedoview.base.SuperPlayerDef.PlayerType
import com.tencent.video.superplayer.base.ConfigInterface
import com.tencent.video.superplayer.base.PlayerConfig
import com.tencent.video.superplayer.base.UIConfig
import com.tencent.video.superplayer.base.timer.TimerConfigure
import com.tencent.video.superplayer.casehelper.KeyListListener
import com.tencent.video.superplayer.casehelper.onNext
import com.tencent.video.superplayer.model.entity.PlayImageSpriteInfo
import com.tencent.video.superplayer.model.entity.PlayKeyFrameDescInfo
import com.tencent.video.superplayer.model.entity.VideoQuality

/**
 * 播放器公共逻辑
 */
abstract class AbBaseUIPlayer : FrameLayout, UIPlayer, ConfigInterface, TimerConfigure.CallBack ,
    KeyListListener {
    protected var playerConfig: PlayerConfig = PlayerConfig.playerConfig
    protected var uiConfig: UIConfig = UIConfig.uiConfig
    protected var mControllerCallback: UIPlayer.VideoViewCallback? = null
    protected var mHideViewRunnable = Runnable { hide() }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setUICallback(callback: UIPlayer.VideoViewCallback?) {
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
    override fun onDestroy() {}
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

    override fun updatePlayConfig(config: PlayerConfig) {
        this.playerConfig = config
    }

    override fun updateUIConfig(uiConfig: UIConfig) {
        this.uiConfig = uiConfig
    }

    override fun onTick(progress: Long) {
    }

    override fun onStateChange(state: Int) {
    }

    override fun onChangeModel(repeatMode: Int) {
    }


    override fun nextOneKey() {

    }

    override fun updateListPosition(position: Int) {
    }

    override fun setKeyList(
        name: String?,
        adapter: BaseKitAdapter<*>?,
        position: Int,
        onNext: onNext?
    ) {

    }

    companion object {
        const val MAX_SHIFT_TIME = 7200 // demo演示直播时移是MAX_SHIFT_TIMEs，即2小时
    }
}