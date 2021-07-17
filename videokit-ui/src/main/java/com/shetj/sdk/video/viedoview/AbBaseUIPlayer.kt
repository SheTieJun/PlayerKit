package com.shetj.sdk.video.viedoview

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.shetj.sdk.video.casehelper.KeyListListener
import com.shetj.sdk.video.casehelper.onNext
import me.shetj.sdk.video.PlayerDef
import me.shetj.sdk.video.UIPlayer
import me.shetj.sdk.video.base.BaseKitAdapter
import me.shetj.sdk.video.base.ConfigInterface
import me.shetj.sdk.video.base.PlayerConfig
import me.shetj.sdk.video.base.UIConfig
import me.shetj.sdk.video.base.timer.TimerConfigure
import me.shetj.sdk.video.model.PlayImageSpriteInfo
import me.shetj.sdk.video.model.PlayKeyFrameDescInfo
import me.shetj.sdk.video.model.VideoQuality

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
    override fun updatePlayState(playState: PlayerDef.PlayerState?) {}
    override fun setVideoQualityList(list: ArrayList<VideoQuality>?) {}
    override fun updateTitle(title: String?) {}
    override fun updateVideoProgress(current: Long, duration: Long) {}
    override fun updatePlayType(type: PlayerDef.PlayerType?) {}
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