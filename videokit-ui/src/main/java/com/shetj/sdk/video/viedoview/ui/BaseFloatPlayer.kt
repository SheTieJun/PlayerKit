package com.shetj.sdk.video.viedoview.ui

import android.content.*
import android.util.AttributeSet
import android.view.*
import com.shetj.sdk.video.viedoview.AbBaseUIPlayer
import me.shetj.sdk.video.player.PlayerDef
import me.shetj.sdk.video.base.PlayerConfig
import me.shetj.sdk.video.ui.IUIPlayer
import me.shetj.sdk.video.base.IPlayerView

/**
 * 悬浮窗模式播放控件
 *
 *
 * 1、滑动以移动悬浮窗，点击悬浮窗回到窗口模式[.onTouchEvent]
 *
 *
 * 2、关闭悬浮窗[.onClick]
 */
abstract class BaseFloatPlayer : AbBaseUIPlayer, IPlayerView,View.OnClickListener {
    /**
     * 获取悬浮窗中的视频播放view
     */
    private var mStatusBarHeight = 0 // 系统状态栏的高度
    private var mXDownInScreen = 0f// 按下事件距离屏幕左边界的距离
    private var mYDownInScreen = 0f // 按下事件距离屏幕上边界的距离
    private var mXInScreen = 0f// 滑动事件距离屏幕左边界的距离
    private var mYInScreen = 0f // 滑动事件距离屏幕上边界的距离
    private var mXInView = 0f// 滑动事件距离自身左边界的距离
    private var mYInView = 0f// 滑动事件距离自身上边界的距离

    private var viewView:View ?= null


    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }


    /**
     * 初始化view
     */
    abstract fun initView(context: Context)


    /**
     * 设置点击事件监听，实现点击关闭按钮后关闭悬浮窗
     */
    override fun onClick(view: View) {

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
                    mControllerCallback!!.onSwitchPlayMode(PlayerDef.PlayerMode.WINDOW)
                }
            }
            else -> {
            }
        }
        return true
    }

    override val playerMode: PlayerDef.PlayerMode
        get() = PlayerDef.PlayerMode.FLOAT

    override fun showLoading() {

    }

    override fun hideLoading() {
    }

    /**
     * 获取系统状态栏高度
     */
    private val statusBarHeight: Int
        get() {
            if (mStatusBarHeight == 0) {
                try {
                    val c = Class.forName("com.android.internal.R\$dimen")
                    val o = c.newInstance()
                    val fields = c.getField("status_bar_height")
                    val x = fields[o] as Int
                    mStatusBarHeight = resources.getDimensionPixelSize(x)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return mStatusBarHeight
        }

    /**
     * 更新悬浮窗的位置信息，在回调[IUIPlayer.VideoViewCallback.onFloatPositionChange]中实现悬浮窗移动
     */
    private fun updateViewPosition() {
        val x = (mXInScreen - mXInView).toInt()
        val y = (mYInScreen - mYInView).toInt()
        val rect: PlayerConfig.TXRect = PlayerConfig.playerConfig.floatViewRect
        rect.x = x
        rect.y = y
        if (mControllerCallback != null) {
            mControllerCallback!!.onFloatPositionChange(x, y)
        }
    }
}