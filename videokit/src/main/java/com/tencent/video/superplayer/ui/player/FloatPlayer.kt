package com.tencent.video.superplayer.ui.player

import android.content.*
import android.util.AttributeSet
import android.view.*
import android.widget.*
import com.tencent.liteav.superplayer.*
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.video.superplayer.SuperPlayerDef.PlayerMode
import com.tencent.video.superplayer.base.PlayerConfig

/**
 * 悬浮窗模式播放控件
 *
 *
 * 1、滑动以移动悬浮窗，点击悬浮窗回到窗口模式[.onTouchEvent]
 *
 *
 * 2、关闭悬浮窗[.onClick]
 */
class FloatPlayer : AbsPlayer, View.OnClickListener {
    /**
     * 获取悬浮窗中的视频播放view
     */
    var floatVideoView // 悬浮窗中的视频播放view
            : TXCloudVideoView? = null
        private set
    private var mStatusBarHeight // 系统状态栏的高度
            = 0
    private var mXDownInScreen // 按下事件距离屏幕左边界的距离
            = 0f
    private var mYDownInScreen // 按下事件距离屏幕上边界的距离
            = 0f
    private var mXInScreen // 滑动事件距离屏幕左边界的距离
            = 0f
    private var mYInScreen // 滑动事件距离屏幕上边界的距离
            = 0f
    private var mXInView // 滑动事件距离自身左边界的距离
            = 0f
    private var mYInView // 滑动事件距离自身上边界的距离
            = 0f

    constructor(context: Context?) : super(context) {
        initView(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    /**
     * 初始化view
     */
    private fun initView(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.superplayer_vod_player_float, this)
        floatVideoView =
            findViewById<View>(R.id.superplayer_float_cloud_video_view) as TXCloudVideoView
        val ivClose = findViewById<View>(R.id.superplayer_iv_close) as ImageView
        ivClose.setOnClickListener(this)
    }

    /**
     * 设置点击事件监听，实现点击关闭按钮后关闭悬浮窗
     */
    override fun onClick(view: View) {
        val i = view.id
        if (i == R.id.superplayer_iv_close) {
            if (mControllerCallback != null) {
                mControllerCallback!!.onBackPressed(PlayerMode.FLOAT)
            }
        }
    }

    /**
     * 重写触摸事件监听，实现悬浮窗随手指移动
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mXInView = event.x
                mYInView = event.y
                mXDownInScreen = event.rawX
                mYDownInScreen = event.rawY - statusBarHeight
                mXInScreen = event.rawX
                mYInScreen = event.rawY - statusBarHeight
            }
            MotionEvent.ACTION_MOVE -> {
                mXInScreen = event.rawX
                mYInScreen = event.rawY - statusBarHeight
                updateViewPosition()
            }
            MotionEvent.ACTION_UP -> if (mXDownInScreen == mXInScreen && mYDownInScreen == mYInScreen) { //手指没有滑动视为点击，回到窗口模式
                if (mControllerCallback != null) {
                    mControllerCallback!!.onSwitchPlayMode(PlayerMode.WINDOW)
                }
            }
            else -> {
            }
        }
        return true
    }

    /**
     * 获取系统状态栏高度
     */
    private val statusBarHeight: Int
        private get() {
            if (mStatusBarHeight == 0) {
                try {
                    val c = Class.forName("com.android.internal.R\$dimen")
                    val o = c.newInstance()
                    val field = c.getField("status_bar_height")
                    val x = field[o] as Int
                    mStatusBarHeight = resources.getDimensionPixelSize(x)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return mStatusBarHeight
        }

    /**
     * 更新悬浮窗的位置信息，在回调[Callback.onFloatPositionChange]中实现悬浮窗移动
     */
    private fun updateViewPosition() {
        val x = (mXInScreen - mXInView).toInt()
        val y = (mYInScreen - mYInView).toInt()
        val rect: PlayerConfig.TXRect = PlayerConfig.ofDef().floatViewRect
        rect.x = x
        rect.y = y
        if (mControllerCallback != null) {
            mControllerCallback!!.onFloatPositionChange(x, y)
        }
    }
}