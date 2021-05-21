package com.tencent.video.superplayer.base


/**
 * 界面配置显示
 */
class UIConfig private constructor(
    val showTV: Boolean, //展示投屏按钮
    val showShare: Boolean, //展示分享按钮
    val showTop: Boolean, //展示顶部条
    val showBottom: Boolean,//展示底部条
    val showSpeed: Boolean,//展示倍数
    val showMore: Boolean,//展示更多
    val showLock: Boolean, //展示Lock
    val keepTop: Boolean, //顶部不隐藏
    val keepBottom: Boolean,//底部不隐藏
    val isShowAccelerate: Boolean,//展示已经加速功能
    val isShowPlayStyle: Boolean,//展示播放模式
    val isShowTimeStyle: Boolean, //展示定时
    val isShowMirror: Boolean,//镜像功能
) {


    companion object {

        val uiConfig = ofDef()

        /**
         * 带接收者的函数类型,这意味着我们需要向函数传递一个Builder类型的实例
         */
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()


        fun ofDef(): UIConfig {
            return build {
                keepBottom = false
                keepTop = false
            }
        }
    }


    class Builder {
        var showTV: Boolean = true
        var showShare: Boolean = true
        var showTop: Boolean = true
        var showBottom: Boolean = true
        var showSpeed: Boolean = true
        var showMore: Boolean = true
        var showLock: Boolean = true
        var keepTop: Boolean = false
        var keepBottom: Boolean = false
        var isShowAccelerate: Boolean = true
        var isShowPlayStyle: Boolean = true
        var isShowTimeStyle: Boolean = true
        var isShowMirror = false

        fun build(): UIConfig {

            return UIConfig(
                showTV, showShare, showTop, showBottom, showSpeed, showMore,
                showLock, keepTop, keepBottom, isShowAccelerate, isShowPlayStyle,
                isShowTimeStyle,isShowMirror
            )
        }
    }
}