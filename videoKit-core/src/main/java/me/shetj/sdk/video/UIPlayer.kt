package me.shetj.sdk.video

import android.graphics.Bitmap
import me.shetj.sdk.video.model.PlayImageSpriteInfo
import me.shetj.sdk.video.model.PlayKeyFrameDescInfo
import me.shetj.sdk.video.model.VideoQuality

/**
 * 播放控制接口
 */
interface UIPlayer {
    /**
     * 设置回调
     *
     * @param callback 回调接口实现对象
     */
    fun setUICallback(callback: VideoViewCallback?)

    val playerMode: PlayerDef.PlayerMode

    /**
     * 设置水印
     *
     * @param bmp 水印图
     * @param x   水印的x坐标
     * @param y   水印的y坐标
     */
    fun setWatermark(bmp: Bitmap?, x: Float, y: Float)

    /**
     * 加载中
     */
    fun showLoading()


    /*隐藏中
     */
    fun hideLoading()

    /**
     * 显示控件
     */
    fun show()

    /**
     * 隐藏控件
     */
    fun hide()

    /**
     * 释放控件的内存
     */
    fun release()

    /**
     * 更新播放状态
     *
     * @param playState 正在播放[PlayerDef.PlayerState.PLAYING]
     * 正在加载[PlayerDef.PlayerState.LOADING]
     * 暂停   [PlayerDef.PlayerState.PAUSE]
     * 播放结束[PlayerDef.PlayerState.END]
     */
    fun updatePlayState(playState: PlayerDef.PlayerState?)

    /**
     * 设置视频画质信息
     *
     * @param list 画质列表
     */
    fun setVideoQualityList(list: ArrayList<VideoQuality>?)

    /**
     * 更新倍数
     */
    fun updateSpeedChange(speedLevel:Float)
    /**
     * 更新视频名称
     *
     * @param title 视频名称
     */
    fun updateTitle(title: String?)

    /**
     * 更新是屁播放进度
     *
     * @param current  当前进度(秒)
     * @param duration 视频总时长(秒)
     */
    fun updateVideoProgress(current: Long, duration: Long)

    /**
     * 更新播放类型
     *
     * @param type 点播     [PlayerDef.PlayerType.VOD]
     * 点播     [PlayerDef.PlayerType.LIVE]
     * 直播回看  [PlayerDef.PlayerType.LIVE_SHIFT]
     */
    fun updatePlayType(type: PlayerDef.PlayerType?)

    /**
     * 设置背景
     *
     * @param bitmap 背景图
     */
    fun setBackground(bitmap: Bitmap?)

    /**
     * 显示背景
     */
    fun showBackground()

    /**
     * 隐藏背景
     */
    fun hideBackground()

    /**
     * 更新视频播放画质
     *
     * @param videoQuality 画质
     */
    fun updateVideoQuality(videoQuality: VideoQuality?)

    /**
     * 更新雪碧图信息
     *
     * @param info 雪碧图信息
     */
    fun updateImageSpriteInfo(info: PlayImageSpriteInfo?)

    /**
     * 更新关键帧信息
     *
     * @param list 关键帧信息列表
     */
    fun updateKeyFrameDescInfo(list: ArrayList<PlayKeyFrameDescInfo>?)

    /**
     * 释放操作
     */
    fun onDestroy()


    /**
     * 界面：播放控制回调接口
     */
    interface VideoViewCallback {
        /**
         * 切换播放模式回调
         *
         * @param playMode 切换后的播放模式：
         * 窗口模式      [PlayerDef.PlayerMode.WINDOW]
         * 全屏模式      [PlayerDef.PlayerMode.FULLSCREEN]
         * 悬浮窗模式    [PlayerDef.PlayerMode.FLOAT]
         */
        fun onSwitchPlayMode(playMode: PlayerDef.PlayerMode)


        /**
         * 返回点击事件回调
         *
         * @param playMode 当前播放模式：
         * 窗口模式      [PlayerDef.PlayerMode.WINDOW]
         * 全屏模式      [PlayerDef.PlayerMode.FULLSCREEN]
         * 悬浮窗模式    [PlayerDef.PlayerMode.FLOAT]
         */
        fun onBackPressed(playMode: PlayerDef.PlayerMode)

        /**
         * 悬浮窗位置更新回调
         *
         * @param x 悬浮窗x坐标
         * @param y 悬浮窗y坐标
         */
        fun onFloatPositionChange(x: Int, y: Int)

        /**
         * 播放暂停回调
         */
        fun onPause()

        /**
         * 播放继续回调
         */
        fun onResume()

        /**
         * 播放跳转回调
         *
         * @param position 跳转的位置(秒)
         */
        fun onSeekTo(position: Int)

        /**
         * 恢复直播回调
         */
        fun onResumeLive()

        /**
         * 分享按钮
         */
        fun onShare()

        /**
         * 投屏
         */
        fun onShotScreen()

        /**
         * 屏幕截图回调
         */
        fun onSnapshot()

        /**
         * 更新画质回调
         *
         * @param quality 画质
         */
        fun onQualityChange(quality: VideoQuality)

        /**
         * 更新播放速度回调
         *
         * @param speedLevel 播放速度
         */
        fun onSpeedChange(speedLevel: Float)

        /**
         * 镜像开关回调
         *
         * @param isMirror 开启：true 关闭：close
         */
        fun onMirrorToggle(isMirror: Boolean)

        /**
         * 硬件加速开关回调
         *
         * @param isAccelerate 开启：true 关闭：false
         */
        fun onHWAccelerationToggle(isAccelerate: Boolean)


        /**
         * 控制view的隐藏和显示
         */
        fun onControlViewToggle(showing: Boolean)
    }
}